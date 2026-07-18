const CLOUDINARY_HOST = "res.cloudinary.com";
const UPLOAD_SEGMENT = "/upload/";
const TRANSFORMATION_PATTERN = /^(?:[a-z]+_[^/]+,?)+$/;

export const imageWidths = {
  avatar: 160,
  story: 200,
  cafeAvatar: 400,
  gridThumb: 600,
  postMedia: 1200,
  cover: 1600,
} as const;

type OptimizeImageOptions = {
  width?: number;
};

/**
 * Rewrites a Cloudinary delivery URL to request an auto-format, auto-quality,
 * width-capped rendition. Non-Cloudinary URLs are returned unchanged.
 */
export function optimizeImageUrl(
  url: string | null | undefined,
  options: OptimizeImageOptions = {},
): string {
  if (!url) {
    return "";
  }

  const trimmed = url.trim();
  if (!trimmed.includes(CLOUDINARY_HOST)) {
    return trimmed;
  }

  const uploadIndex = trimmed.indexOf(UPLOAD_SEGMENT);
  if (uploadIndex === -1) {
    return trimmed;
  }

  const prefix = trimmed.slice(0, uploadIndex + UPLOAD_SEGMENT.length);
  const rest = trimmed.slice(uploadIndex + UPLOAD_SEGMENT.length);

  const firstSegment = rest.split("/", 1)[0] ?? "";
  if (TRANSFORMATION_PATTERN.test(firstSegment) && firstSegment.includes(",")) {
    return trimmed;
  }

  const transformations = ["f_auto", "q_auto"];
  if (options.width && options.width > 0) {
    transformations.push(`w_${Math.round(options.width)}`, "c_limit");
  }

  return `${prefix}${transformations.join(",")}/${rest}`;
}
