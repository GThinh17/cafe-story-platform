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
