"use client";

import { useCallback } from "react";
import { ZoomInIcon } from "lucide-react";
import Cropper, { type Area } from "react-easy-crop";
import { cn } from "@/lib/utils";
import { useI18n } from "@/components/providers/locale-provider";

type CreatePostImageCropperProps = {
  src: string;
  aspect: number;
  crop: { x: number; y: number };
  zoom: number;
  onCropChange: (next: { x: number; y: number }) => void;
  onZoomChange: (next: number) => void;
  onCropComplete: (area: Area) => void;
  className?: string;
};

const MAX_FRAME_HEIGHT = 420;

export function CreatePostImageCropper({
  src,
  aspect,
  crop,
  zoom,
  onCropChange,
  onZoomChange,
  onCropComplete,
  className,
}: CreatePostImageCropperProps) {
  const { t } = useI18n();
  const handleCropComplete = useCallback(
    (_croppedArea: Area, croppedAreaPixels: Area) => {
      onCropComplete(croppedAreaPixels);
    },
    [onCropComplete],
  );

  return (
    <div className={cn("flex flex-col gap-3", className)}>
      <div
        className="relative mx-auto w-full overflow-hidden rounded-md bg-espresso"
        style={{
          aspectRatio: aspect,
          maxWidth: MAX_FRAME_HEIGHT * aspect,
        }}
      >
        <Cropper
          aspect={aspect}
          crop={crop}
          image={src}
          objectFit="cover"
          onCropChange={onCropChange}
          onCropComplete={handleCropComplete}
          onZoomChange={onZoomChange}
          showGrid={true}
          zoom={zoom}
          zoomSpeed={0.5}
          minZoom={1}
          maxZoom={3}
        />
      </div>

      <div className="flex items-center gap-3 px-1">
        <ZoomInIcon aria-hidden="true" className="size-4 text-muted" />
        <input
          aria-label={t("createPost.setup.zoom")}
          className="h-1.5 w-full cursor-pointer appearance-none rounded-full bg-surface-muted accent-primary"
          max={3}
          min={1}
          onChange={(event) => onZoomChange(Number(event.target.value))}
          step={0.01}
          type="range"
          value={zoom}
        />
      </div>
    </div>
  );
}
