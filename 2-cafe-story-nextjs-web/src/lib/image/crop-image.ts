export type CropArea = {
  x: number;
  y: number;
  width: number;
  height: number;
};

function loadImageFromUrl(url: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const image = new Image();
    image.onload = () => resolve(image);
    image.onerror = () => reject(new Error("Failed to load image for cropping."));
    image.crossOrigin = "anonymous";
    image.src = url;
  });
}

/**
 * Centre crop filling the given aspect ratio, in natural source pixels.
 *
 * react-easy-crop only reports a crop area for the image currently mounted in
 * the cropper, so images the user never opened would otherwise upload at their
 * original aspect ratio. This gives those images the same ratio as the rest.
 */
export async function getCenteredCropArea(
  sourceFile: File,
  aspect: number,
): Promise<CropArea> {
  const objectUrl = URL.createObjectURL(sourceFile);

  try {
    const image = await loadImageFromUrl(objectUrl);
    const naturalWidth = image.naturalWidth || image.width;
    const naturalHeight = image.naturalHeight || image.height;
    const sourceAspect = naturalWidth / naturalHeight;

    // Wider than target -> trim the sides; taller -> trim top and bottom.
    const width = sourceAspect > aspect ? naturalHeight * aspect : naturalWidth;
    const height = sourceAspect > aspect ? naturalHeight : naturalWidth / aspect;

    return {
      x: (naturalWidth - width) / 2,
      y: (naturalHeight - height) / 2,
      width,
      height,
    };
  } finally {
    URL.revokeObjectURL(objectUrl);
  }
}

export async function getCroppedImageFile(
  sourceFile: File,
  croppedAreaPixels: CropArea,
  outputMime: "image/jpeg" | "image/webp" = "image/jpeg",
  quality = 0.92,
): Promise<File> {
  const objectUrl = URL.createObjectURL(sourceFile);

  try {
    const image = await loadImageFromUrl(objectUrl);
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");
    if (!ctx) {
      throw new Error("Canvas 2D context not available.");
    }

    const safeWidth = Math.max(1, Math.round(croppedAreaPixels.width));
    const safeHeight = Math.max(1, Math.round(croppedAreaPixels.height));
    canvas.width = safeWidth;
    canvas.height = safeHeight;

    ctx.drawImage(
      image,
      croppedAreaPixels.x,
      croppedAreaPixels.y,
      croppedAreaPixels.width,
      croppedAreaPixels.height,
      0,
      0,
      safeWidth,
      safeHeight,
    );

    const blob = await new Promise<Blob | null>((resolve) => {
      canvas.toBlob((result) => resolve(result), outputMime, quality);
    });

    if (!blob) {
      throw new Error("Failed to convert cropped canvas to blob.");
    }

    const extension = outputMime === "image/webp" ? "webp" : "jpg";
    const baseName = sourceFile.name.replace(/\.[^/.]+$/, "") || "image";
    const fileName = `${baseName}-cropped.${extension}`;

    return new File([blob], fileName, {
      type: outputMime,
      lastModified: Date.now(),
    });
  } finally {
    URL.revokeObjectURL(objectUrl);
  }
}
