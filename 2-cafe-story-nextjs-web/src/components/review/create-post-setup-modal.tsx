"use client";

import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ChangeEvent,
} from "react";
import {
  ImageIcon,
  LoaderCircleIcon,
  PlusIcon,
  XIcon,
} from "lucide-react";
import type { Area } from "react-easy-crop";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { CreatePostImageCropper } from "@/components/review/create-post-image-cropper";
import { getCroppedImageFile } from "@/lib/image/crop-image";
import { cn } from "@/lib/utils";
import { useI18n } from "@/components/providers/locale-provider";

const MAX_IMAGES = 10;

const RATIO_OPTIONS = [
  { id: "1:1", label: "1:1", value: 1 },
  { id: "4:3", label: "4:3", value: 4 / 3 },
  { id: "16:9", label: "16:9", value: 16 / 9 },
  { id: "10:16", label: "10:16", value: 10 / 16 },
] as const;

type RatioId = (typeof RATIO_OPTIONS)[number]["id"];

type SetupImage = {
  id: string;
  file: File;
  name: string;
  previewUrl: string;
  crop: { x: number; y: number };
  zoom: number;
  cropAreaPixels: Area | null;
};

export type CroppedImage = {
  id: string;
  file: File;
  name: string;
};

type CreatePostSetupModalProps = {
  isOpen: boolean;
  onClose: () => void;
  onNext: (images: CroppedImage[]) => void;
};

function createImageId(file: File) {
  const randomId =
    globalThis.crypto?.randomUUID?.() ?? Math.random().toString(36).slice(2);
  return `${file.name}-${file.lastModified}-${randomId}`;
}

function revokePreview(image: SetupImage) {
  URL.revokeObjectURL(image.previewUrl);
}

function defaultCropState(): Pick<SetupImage, "crop" | "zoom" | "cropAreaPixels"> {
  return { crop: { x: 0, y: 0 }, zoom: 1, cropAreaPixels: null };
}

export function CreatePostSetupModal({
  isOpen,
  onClose,
  onNext,
}: CreatePostSetupModalProps) {
  const { t } = useI18n();
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const imagesRef = useRef<SetupImage[]>([]);
  const [ratioId, setRatioId] = useState<RatioId>("1:1");
  const [images, setImages] = useState<SetupImage[]>([]);
  const [activeIndex, setActiveIndex] = useState(0);
  const [isProcessing, setIsProcessing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const ratioValue = useMemo(
    () => RATIO_OPTIONS.find((option) => option.id === ratioId)?.value ?? 1,
    [ratioId],
  );
  const isImageLimitReached = images.length >= MAX_IMAGES;
  const isNextDisabled = images.length === 0 || isProcessing;
  const activeImage = images[Math.min(activeIndex, Math.max(images.length - 1, 0))];

  useEffect(() => {
    imagesRef.current = images;
  }, [images]);

  useEffect(() => {
    if (!isOpen) {
      setIsProcessing(false);
    }
  }, [isOpen]);

  useEffect(() => {
    return () => {
      imagesRef.current.forEach(revokePreview);
    };
  }, []);

  const handleClose = useCallback(() => {
    if (isProcessing) return;
    onClose();
  }, [isProcessing, onClose]);

  const handleSelectFiles = useCallback(
    (event: ChangeEvent<HTMLInputElement>) => {
      const files = Array.from(event.target.files ?? []).filter((file) =>
        file.type.startsWith("image/"),
      );

      if (files.length > 0) {
        setImages((current) => {
          const remaining = MAX_IMAGES - current.length;
          if (remaining <= 0) return current;
          const allowed = files.slice(0, remaining);
          const additions: SetupImage[] = allowed.map((file) => ({
            id: createImageId(file),
            file,
            name: file.name,
            previewUrl: URL.createObjectURL(file),
            ...defaultCropState(),
          }));
          const next = [...current, ...additions];
          if (current.length === 0 && additions.length > 0) {
            setActiveIndex(0);
          }
          return next;
        });
      }

      event.target.value = "";
      setErrorMessage(null);
    },
    [],
  );

  const handleRemoveImage = useCallback((imageId: string) => {
    setImages((current) => {
      const removed = current.find((img) => img.id === imageId);
      if (removed) revokePreview(removed);
      const next = current.filter((img) => img.id !== imageId);
      setActiveIndex((idx) => Math.min(idx, Math.max(next.length - 1, 0)));
      return next;
    });
  }, []);

  const handleRatioChange = useCallback((nextRatio: RatioId) => {
    setRatioId(nextRatio);
    setImages((current) =>
      current.map((img) => ({ ...img, ...defaultCropState() })),
    );
  }, []);

  const updateActiveImage = useCallback(
    (patch: Partial<SetupImage>) => {
      setImages((current) => {
        const idx = Math.min(activeIndex, Math.max(current.length - 1, 0));
        if (idx < 0 || idx >= current.length) return current;
        const next = [...current];
        next[idx] = { ...next[idx], ...patch };
        return next;
      });
    },
    [activeIndex],
  );

  async function handleNext() {
    if (images.length === 0) return;
    setIsProcessing(true);
    setErrorMessage(null);

    try {
      const cropped: CroppedImage[] = await Promise.all(
        images.map(async (img) => {
          if (!img.cropAreaPixels) {
            return { id: img.id, file: img.file, name: img.name };
          }
          const file = await getCroppedImageFile(img.file, img.cropAreaPixels);
          return { id: img.id, file, name: img.name };
        }),
      );
      onNext(cropped);
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : t("createPost.setup.cropError"),
      );
      setIsProcessing(false);
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && handleClose()}>
      <DialogContent className="flex h-[80vh] flex-col p-0">
        <header className="flex h-[72px] items-center justify-between border-b border-line-soft px-6">
          <DialogTitle className="font-sans text-xl font-bold text-espresso">
            {t("createPost.setup.title")}
          </DialogTitle>

          <Button
            className="h-10 bg-primary px-6 text-sm font-bold text-primary-foreground hover:bg-primary-strong"
            disabled={isNextDisabled}
            onClick={handleNext}
            type="button"
          >
            {isProcessing ? (
              <>
                <LoaderCircleIcon data-icon="inline-start" className="animate-spin" />
                {t("createPost.setup.processing")}
              </>
            ) : (
              t("createPost.setup.next")
            )}
          </Button>
        </header>

        <div className="flex flex-1 flex-col gap-5 overflow-y-auto px-6 py-5">
          <section className="flex flex-col gap-2">
            <span className="text-xs font-medium text-muted">
              {t("createPost.setup.aspectRatio")}
            </span>
            <div className="inline-flex w-fit items-center gap-1 rounded-md bg-surface-muted p-1">
              {RATIO_OPTIONS.map((option) => {
                const isActive = option.id === ratioId;
                return (
                  <button
                    aria-pressed={isActive}
                    className={cn(
                      "h-8 rounded-md px-4 text-xs font-medium transition-colors",
                      isActive
                        ? "bg-primary text-primary-foreground"
                        : "text-foreground hover:text-primary",
                    )}
                    disabled={isProcessing}
                    key={option.id}
                    onClick={() => handleRatioChange(option.id)}
                    type="button"
                  >
                    {option.label}
                  </button>
                );
              })}
            </div>
          </section>

          <Input
            accept="image/*"
            className="hidden"
            multiple
            onChange={handleSelectFiles}
            ref={fileInputRef}
            type="file"
          />

          {activeImage ? (
            <CreatePostImageCropper
              aspect={ratioValue}
              crop={activeImage.crop}
              onCropChange={(next) => updateActiveImage({ crop: next })}
              onCropComplete={(area) =>
                updateActiveImage({ cropAreaPixels: area })
              }
              onZoomChange={(next) => updateActiveImage({ zoom: next })}
              src={activeImage.previewUrl}
              zoom={activeImage.zoom}
            />
          ) : (
            <Button
              className="min-h-48 w-full flex-col border-dashed border-line-soft bg-surface-muted px-4 py-8 text-center text-sm font-medium text-muted whitespace-normal hover:border-espresso hover:text-espresso"
              disabled={isProcessing}
              onClick={() => fileInputRef.current?.click()}
              type="button"
              variant="outline"
            >
              <ImageIcon aria-hidden="true" />
              {t("createPost.setup.choosePhotos")}
            </Button>
          )}

          {images.length > 0 ? (
            <section className="flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-muted">
                  {t("createPost.setup.selectedPhotos", {
                    count: images.length,
                    max: MAX_IMAGES,
                  })}
                </span>
                <span className="text-xs text-muted">
                  {t("createPost.setup.dragHint")}
                </span>
              </div>

              <div className="flex items-center gap-2 overflow-x-auto pb-1">
                {images.map((img, index) => {
                  const isActive = index === activeIndex;
                  return (
                    <div
                      className={cn(
                        "relative h-16 w-16 shrink-0 overflow-hidden rounded-md border bg-surface-muted",
                        isActive ? "border-primary" : "border-line-soft",
                      )}
                      key={img.id}
                    >
                      <button
                        aria-label={`Edit ${img.name}`}
                        className="absolute inset-0"
                        onClick={() => setActiveIndex(index)}
                        type="button"
                      >
                        <img
                          alt={img.name}
                          className="h-full w-full object-cover"
                          decoding="async"
                          src={img.previewUrl}
                        />
                      </button>
                      <Button
                        aria-label={`Remove ${img.name}`}
                        className="absolute right-1 top-1 size-5 bg-background/90 text-foreground shadow-sm hover:bg-surface-muted"
                        disabled={isProcessing}
                        onClick={() => handleRemoveImage(img.id)}
                        size="icon-xs"
                        type="button"
                        variant="secondary"
                      >
                        <XIcon aria-hidden="true" />
                      </Button>
                    </div>
                  );
                })}

                {!isImageLimitReached ? (
                  <Button
                    aria-label={t("createPost.setup.addMore")}
                    className="h-16 w-16 shrink-0 border-dashed border-line-soft bg-surface-muted text-muted hover:border-espresso hover:text-espresso"
                    disabled={isProcessing}
                    onClick={() => fileInputRef.current?.click()}
                    type="button"
                    variant="outline"
                  >
                    <PlusIcon aria-hidden="true" />
                  </Button>
                ) : null}
              </div>
            </section>
          ) : null}

          {errorMessage ? (
            <p
              className="rounded-md border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm font-medium text-destructive"
              role="alert"
            >
              {errorMessage}
            </p>
          ) : null}
        </div>
      </DialogContent>
    </Dialog>
  );
}
