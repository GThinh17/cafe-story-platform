import { v2 as cloudinary } from "cloudinary";

const cloudName =
  process.env.CLOUDINARY_CLOUD_NAME ??
  process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME;
const apiKey = process.env.CLOUDINARY_API_KEY;
const apiSecret = process.env.CLOUDINARY_API_SECRET;

if (cloudName && apiKey && apiSecret) {
  cloudinary.config({
    cloud_name: cloudName,
    api_key: apiKey,
    api_secret: apiSecret,
    secure: true,
  });
} else {
  cloudinary.config({ secure: true });
}

export const cloudinaryUploadFolder =
  process.env.CLOUDINARY_UPLOAD_FOLDER ?? "cafe-story";

export function assertCloudinaryServerConfig() {
  const hasCloudinaryUrl = Boolean(process.env.CLOUDINARY_URL);
  const hasExplicitCredentials = Boolean(cloudName && apiKey && apiSecret);

  if (!hasCloudinaryUrl && !hasExplicitCredentials) {
    throw new Error(
      "Missing Cloudinary server config. Set CLOUDINARY_URL or NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET.",
    );
  }
}

export { cloudinary };
