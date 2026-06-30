"use client";

import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type FormEvent,
} from "react";
import {
  ArrowLeftIcon,
  LoaderCircleIcon,
  MapPinIcon,
  XIcon,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import {
  CreatePostLocationPicker,
  type PostLocation,
} from "@/components/review/create-post-form";
import { createModeratedBlog } from "@/lib/api/blogs";
import { uploadPostImageToCloudinary } from "@/lib/api/cloudinary";
import type { BlogCreateRequest, BlogResponse } from "@/types/blog";
import type { CafePageResponse } from "@/types/cafe";
import type { ReviewComposerModel, ReviewDraftHint } from "@/types/review";

type SelectedImage = {
  id: string;
  file: File;
  name: string;
  previewUrl: string;
};

export type CreatePostInitialImage = {
  id?: string;
  file: File;
  name: string;
};

type CreatePostModalProps = {
  composer: ReviewComposerModel;
  hints: ReviewDraftHint[];
  isOpen: boolean;
  ownedCafePage?: CafePageResponse | null;
  initialImages?: CreatePostInitialImage[];
  onClose: () => void;
  onBack?: () => void;
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

function buildSelectedImages(initial: CreatePostInitialImage[]): SelectedImage[] {
  return initial.map((item) => ({
    id: item.id ?? createImageId(item.file),
    file: item.file,
    name: item.name,
    previewUrl: URL.createObjectURL(item.file),
  }));
}

export function CreatePostModal({
  composer,
  hints: _hints,
  isOpen,
  ownedCafePage = null,
  initialImages,
  onClose,
  onBack,
  onCreated,
}: CreatePostModalProps) {
  const selectedImagesRef = useRef<SelectedImage[]>([]);
  const [selectedImages, setSelectedImages] = useState<SelectedImage[]>([]);
  const [caption, setCaption] = useState("");
  const [location, setLocation] = useState<PostLocation | null>(null);
  const [isLocationPickerOpen, setIsLocationPickerOpen] = useState(false);
  const [postAsCafePage, setPostAsCafePage] = useState(false);
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
    setLocation(null);
    setIsLocationPickerOpen(false);
    setPostAsCafePage(false);
    setTurnOffCommenting(false);
    setErrorMessage(null);
  }, []);

  useEffect(() => {
    if (!initialImages || initialImages.length === 0) return;
    setSelectedImages((current) => {
      current.forEach(revokeImagePreview);
      return buildSelectedImages(initialImages);
    });
  }, [initialImages]);

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

  const handleBack = useCallback(() => {
    if (isSubmitting || !onBack) return;
    onBack();
  }, [isSubmitting, onBack]);

  const handleRemoveImage = useCallback((imageId: string) => {
    setSelectedImages((currentImages) => {
      const removedImage = currentImages.find((image) => image.id === imageId);
      if (removedImage) revokeImagePreview(removedImage);
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
      const payload: BlogCreateRequest = {
        allowComment: !turnOffCommenting,
        content: trimmedCaption,
        imageUrls,
        isPinned: false,
        ...(postAsCafePage && ownedCafePage?.id
          ? { pageId: ownedCafePage.id }
          : {}),
        ...(location?.regionId ? { regionId: location.regionId } : {}),
      };
      const createdPost = await createModeratedBlog(payload);

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
            <div className="flex items-center gap-3">
              {onBack ? (
                <Button
                  aria-label="Back to image setup"
                  className="size-9 text-espresso"
                  disabled={isSubmitting}
                  onClick={handleBack}
                  size="icon-sm"
                  type="button"
                  variant="ghost"
                >
                  <ArrowLeftIcon aria-hidden="true" />
                </Button>
              ) : null}
              <DialogTitle className="font-sans text-xl font-bold text-espresso">
                {composer.title}
              </DialogTitle>
            </div>

            <Button
              className="h-10 bg-primary px-6 text-sm font-bold text-primary-foreground hover:bg-primary-strong"
              disabled={isPostDisabled}
              type="submit"
            >
              {isSubmitting ? (
                <>
                  <LoaderCircleIcon data-icon="inline-start" className="animate-spin" />
                  Posting
                </>
              ) : (
                "Post"
              )}
            </Button>
          </header>

          <div className="flex flex-1 flex-col gap-8 overflow-y-auto px-6 py-6">
            {selectedImages.length > 0 ? (
              <section className="flex flex-col gap-4">
                <div className="flex items-center justify-between gap-4 text-sm text-espresso">
                  <h3>Selected Photos ({selectedImages.length})</h3>
                  {onBack ? (
                    <button
                      className="text-sm font-medium text-primary hover:underline disabled:opacity-50"
                      disabled={isSubmitting}
                      onClick={handleBack}
                      type="button"
                    >
                      Edit photos
                    </button>
                  ) : null}
                </div>

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
              </section>
            ) : null}

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
                <label className="text-sm font-medium text-espresso">
                  Location
                </label>
                <Button
                  className="h-auto p-0 text-sm font-medium text-espresso"
                  disabled={isSubmitting}
                  onClick={() => setIsLocationPickerOpen(true)}
                  type="button"
                  variant="link"
                >
                  {location ? "Change" : "Add location"}
                </Button>
              </div>
              {location ? (
                <div className="flex items-center gap-2">
                  <MapPinIcon className="size-4 shrink-0 text-espresso" />
                  <p className="truncate text-sm font-bold text-espresso">
                    {location.name}
                  </p>
                  <Button
                    className="ml-auto h-auto p-0 text-xs text-muted hover:text-destructive"
                    disabled={isSubmitting}
                    onClick={() => setLocation(null)}
                    type="button"
                    variant="link"
                  >
                    Remove
                  </Button>
                </div>
              ) : (
                <p className="text-xs leading-5 text-muted">
                  Add a location to help others discover your post.
                </p>
              )}
            </section>

            {ownedCafePage ? (
              <section className="flex flex-col gap-2 rounded-md bg-surface-muted px-4 py-4">
                <div className="flex items-center justify-between gap-4">
                  <label
                    className="text-sm font-medium text-espresso"
                    htmlFor="post-as-cafe-page"
                  >
                    Post as cafe page
                  </label>
                  <Switch
                    checked={postAsCafePage}
                    disabled={isSubmitting}
                    id="post-as-cafe-page"
                    onCheckedChange={setPostAsCafePage}
                  />
                </div>
                <p className="max-w-[520px] text-xs leading-5 text-muted">
                  This post will appear under {ownedCafePage.name}.
                </p>
              </section>
            ) : null}

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

      <CreatePostLocationPicker
        isOpen={isLocationPickerOpen}
        onApply={(loc) => {
          setLocation(loc);
          setIsLocationPickerOpen(false);
        }}
        onClose={() => setIsLocationPickerOpen(false)}
      />
    </Dialog>
  );
}
