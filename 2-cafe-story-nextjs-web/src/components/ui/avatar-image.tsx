"use client";

import { useEffect, useState } from "react";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import { cn } from "@/lib/utils";

type AvatarImageProps = {
  alt: string;
  className?: string;
  height?: number | string;
  loading?: "eager" | "lazy";
  src?: string | null;
  width?: number | string;
};

export function AvatarImage({
  alt,
  className,
  height,
  loading,
  src,
  width,
}: AvatarImageProps) {
  const [imageSrc, setImageSrc] = useState(src || DEFAULT_AVATAR_IMAGE);

  useEffect(() => {
    setImageSrc(src || DEFAULT_AVATAR_IMAGE);
  }, [src]);

  return (
    <img
      alt={alt}
      className={cn(
        "block aspect-square h-full w-full max-w-none rounded-full object-cover object-center",
        className,
      )}
      decoding="async"
      height={height}
      loading={loading}
      onError={() => setImageSrc(DEFAULT_AVATAR_IMAGE)}
      src={imageSrc}
      width={width}
    />
  );
}
