const CLOUDINARY_BASE_URL = "https://res.cloudinary.com";

type CloudinaryResourceType = "image" | "video" | "raw";
type CloudinaryDeliveryType = "upload" | "fetch" | "private" | "authenticated";

export type CloudinaryAssetOptions = {
  publicId: string;
  cloudName?: string;
  transformations?: string | string[];
  resourceType?: CloudinaryResourceType;
  deliveryType?: CloudinaryDeliveryType;
  format?: string;
};

export function getCloudinaryCloudName() {
  return process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME ?? "";
}

export function buildCloudinaryAssetUrl({
  publicId,
  cloudName = getCloudinaryCloudName(),
  transformations = "f_auto,q_auto",
  resourceType = "image",
  deliveryType = "upload",
  format,
}: CloudinaryAssetOptions) {
  if (!cloudName) {
    throw new Error("Missing NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME.");
  }

  const normalizedPublicId = publicId.replace(/^\/+/, "");
  const normalizedFormat = format ? `.${format.replace(/^\./, "")}` : "";
  const transformationList = Array.isArray(transformations)
    ? transformations
    : [transformations];
  const transformationPath = transformationList.filter(Boolean).join("/");
  const deliveryPath = transformationPath
    ? `${deliveryType}/${transformationPath}`
    : deliveryType;

  return `${CLOUDINARY_BASE_URL}/${cloudName}/${resourceType}/${deliveryPath}/${normalizedPublicId}${normalizedFormat}`;
}
