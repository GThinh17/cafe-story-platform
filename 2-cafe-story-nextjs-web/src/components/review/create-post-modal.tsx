"use client";

import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type ChangeEvent,
  type FormEvent,
} from "react";
import {
  ImageIcon,
  LoaderCircleIcon,
  PlusIcon,
  XIcon,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { createBlog } from "@/lib/api/blogs";
import { uploadPostImageToCloudinary } from "@/lib/api/cloudinary";
import type { BlogResponse } from "@/types/blog";
import type { ReviewComposerModel, ReviewDraftHint } from "@/types/review";

type SelectedImage = {
  id: string;
  file: File;
  name: string;
  previewUrl: string;
};

type CreatePostModalProps = {
  composer: ReviewComposerModel;
  hints: ReviewDraftHint[];
  isOpen: boolean;
  onClose: () => void;
  onCreated?: (post: BlogResponse) => void;
};

function createImageId(file: File) {
  const randomId =
    globalThis.crypto?.randomUUID?.() ?? Math.random().toString(36).slice(2);

  return `${file.name}-${file.lastModified}-${randomId}`;
}

function revokeImagePreview(image: SelectedImage) {
  URL.revokeObjectURL(image.previewUrl);
}

export function CreatePostModal({
  composer,
  hints: _hints,
  isOpen,
  onClose,
  onCreated,
}: CreatePostModalProps) {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const selectedImagesRef = useRef<SelectedImage[]>([]);
  const [selectedImages, setSelectedImages] = useState<SelectedImage[]>([]);
  const [caption, setCaption] = useState("");
  const [turnOffCommenting, setTurnOffCommenting] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const trimmedCaption = caption.trim();
  const isPostDisabled = isSubmitting || !trimmedCaption;

  useEffect(() => {
    selectedImagesRef.current = selectedImages;
  }, [selectedImages]);

  const resetForm = useCallback(() => {
    setSelectedImages((currentImages) => {
      currentImages.forEach(revokeImagePreview);
      return [];
    });
    setCaption("");
    setTurnOffCommenting(false);
    setErrorMessage(null);

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  }, []);

  useEffect(() => {
    if (!isOpen) {
      resetForm();
    }
  }, [isOpen, resetForm]);

  useEffect(() => {
    return () => {
      selectedImagesRef.current.forEach(revokeImagePreview);
    };
  }, []);

  const handleClose = useCallback(() => {
    if (isSubmitting) {
      return;
    }

    resetForm();
    onClose();
  }, [isSubmitting, onClose, resetForm]);

  const handleSelectImages = useCallback(
    (event: ChangeEvent<HTMLInputElement>) => {
      const files = Array.from(event.target.files ?? []).filter((file) =>
        file.type.startsWith("image/"),
      );

      if (files.length > 0) {
        setSelectedImages((currentImages) => [
          ...currentImages,
          ...files.map((file) => ({
            id: createImageId(file),
            file,
            name: file.name,
            previewUrl: URL.createObjectURL(file),
          })),
        ]);
      }

      event.target.value = "";
      setErrorMessage(null);
    },
    [],
  );

  const handleRemoveImage = useCallback((imageId: string) => {
    setSelectedImages((currentImages) => {
      const removedImage = currentImages.find((image) => image.id === imageId);

      if (removedImage) {
        revokeImagePreview(removedImage);
      }

      return currentImages.filter((image) => image.id !== imageId);
    });
  }, []);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!trimmedCaption) {
      setErrorMessage("Please enter a caption before posting.");
      return;
    }

    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      const imageUrls = await Promise.all(
        selectedImages.map((image) => uploadPostImageToCloudinary(image.file)),
      );
      const createdPost = await createBlog({
        allowComment: !turnOffCommenting,
        content: trimmedCaption,
        imageUrls,
        isPinned: false,
      });

      onCreated?.(createdPost);
      resetForm();
      onClose();
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Unable to create post. Please try again.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && handleClose()}>
      <DialogContent className="flex h-[80vh] flex-col p-0">
        <form
          className="flex h-full w-full flex-col overflow-hidden rounded-md border border-line-soft bg-surface shadow-[0_26px_80px_rgba(39,19,16,0.22)]"
          onSubmit={handleSubmit}
        >
          <header className="flex h-[72px] items-center justify-between border-b border-line-soft px-6">
            <DialogTitle className="font-serif text-xl font-semibold text-espresso">
              {composer.title}
            </DialogTitle>

            <Button
              className="h-10 bg-espresso px-6 text-sm font-black hover:bg-primary-container"
              disabled={isPostDisabled}
              type="submit"
            >
              {isSubmitting ? (
                <>
                  <LoaderCircleIcon data-icon="inline-start" />
                  Posting
                </>
              ) : (
                "Post"
              )}
            </Button>
          </header>

          <div className="flex flex-1 flex-col gap-8 overflow-y-auto px-6 py-6">
            <section className="flex flex-col gap-4">
              <div className="flex items-center justify-between gap-4 text-sm text-espresso">
                <h3>Selected Photos ({selectedImages.length})</h3>
                <Button
                  className="h-auto p-0 text-sm font-medium text-espresso"
                  disabled={isSubmitting}
                  onClick={() => fileInputRef.current?.click()}
                  type="button"
                  variant="link"
                >
                  {selectedImages.length > 0 ? (
                    <>
                      <PlusIcon data-icon="inline-start" />
                      Add more
                    </>
                  ) : (
                    <>
                      <ImageIcon data-icon="inline-start" />
                      Add photos
                    </>
                  )}
                </Button>
              </div>

              <input
                accept="image/*"
                className="hidden"
                multiple
                onChange={handleSelectImages}
                ref={fileInputRef}
                type="file"
              />

              {selectedImages.length > 0 ? (
                <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
                  {selectedImages.map((image) => (
                    <div
                      className="relative aspect-square overflow-hidden rounded-md bg-surface-muted"
                      key={image.id}
                    >
                      <img
                        alt={image.name}
                        className="h-full w-full object-cover"
                        decoding="async"
                        src={image.previewUrl}
                      />
                      <Button
                        aria-label={`Remove ${image.name}`}
                        className="absolute right-2 top-2 bg-background/90 text-foreground shadow-sm hover:bg-surface-muted"
                        disabled={isSubmitting}
                        onClick={() => handleRemoveImage(image.id)}
                        size="icon-xs"
                        type="button"
                        variant="secondary"
                      >
                        <XIcon aria-hidden="true" />
                      </Button>
                    </div>
                  ))}
                </div>
              ) : (
                <Button
                  className="min-h-48 w-full flex-col border-dashed border-line-soft bg-surface-muted px-4 py-8 text-center text-sm font-medium text-muted whitespace-normal hover:border-espresso hover:text-espresso"
                  disabled={isSubmitting}
                  onClick={() => fileInputRef.current?.click()}
                  type="button"
                  variant="outline"
                >
                  <ImageIcon aria-hidden="true" />
                  Choose photos from your device
                </Button>
              )}
            </section>

            <label className="flex flex-col gap-4">
              <span className="block text-sm text-espresso">Caption</span>
              <Textarea
                className="min-h-32 resize-none rounded-none border-line-soft bg-surface-muted px-4 py-4 outline-none focus:border-line-soft focus:outline-none focus:ring-0 focus-visible:border-line-soft focus-visible:outline-none focus-visible:ring-0 focus-visible:ring-offset-0"
                disabled={isSubmitting}
                onChange={(event) => {
                  setCaption(event.target.value);
                  setErrorMessage(null);
                }}
                placeholder="Share your experience..."
                value={caption}
              />
            </label>

            <section className="flex flex-col gap-2 rounded-md bg-surface-muted px-4 py-4">
              <div className="flex items-center justify-between gap-4">
                <label
                  className="text-sm font-medium text-espresso"
                  htmlFor="turn-off-commenting"
                >
                  Turn off commenting
                </label>
                <Switch
                  checked={turnOffCommenting}
                  disabled={isSubmitting}
                  id="turn-off-commenting"
                  onCheckedChange={setTurnOffCommenting}
                />
              </div>
              <p className="max-w-[520px] text-xs leading-5 text-muted">
                You can change this later by going to the ... menu at the top of
                your post.
              </p>
            </section>

            {errorMessage ? (
              <p
                className="rounded-md border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm font-medium text-destructive"
                role="alert"
              >
                {errorMessage}
              </p>
            ) : null}

            <footer className="flex items-center justify-between border-t border-line-soft pt-5 text-xs text-muted">
              <p className="flex items-center gap-2">
                <span className="grid size-4 place-items-center rounded-full border border-muted text-[10px]">
                  i
                </span>
                Posts are visible to your followers immediately.
              </p>
              <p className="flex items-center gap-2">
                <span aria-hidden="true">o</span>
                Public
              </p>
            </footer>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
