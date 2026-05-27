"use client";

import { useState } from "react";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";

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

  return (
    <img
      alt={alt}
      className={className}
      decoding="async"
      height={height}
      loading={loading}
      onError={() => setImageSrc(DEFAULT_AVATAR_IMAGE)}
      src={imageSrc}
      width={width}
    />
  );
}
