import { getCloudinaryConfig } from "../../config";
import { localizeApiErrorMessage } from "../../features/i18n";

type CloudinaryUploadResponse = {
  secure_url?: unknown;
};

export type CloudinaryUploadFile = {
  name?: string | null;
  type?: string | null;
  uri: string;
};

export function isRemoteImageUrl(uri: string) {
  return /^https?:\/\//i.test(uri.trim());
}

function inferMimeType(uri: string) {
  const normalizedUri = uri.toLowerCase();

  if (normalizedUri.endsWith(".png")) {
    return "image/png";
  }

  if (normalizedUri.endsWith(".webp")) {
    return "image/webp";
  }

  return "image/jpeg";
}

function extensionForMimeType(mimeType: string) {
  if (mimeType === "image/png") {
    return "png";
  }

  if (mimeType === "image/webp") {
    return "webp";
  }

  return "jpg";
}

function normalizeUploadFile(file: CloudinaryUploadFile, fallbackName: string) {
  const type = file.type?.trim() || inferMimeType(file.uri);
  const name =
    file.name?.trim() || `${fallbackName}.${extensionForMimeType(type)}`;

  return {
    name,
    type,
    uri: file.uri,
  };
}

async function uploadImageToCloudinary(
  file: CloudinaryUploadFile,
  folder: string,
  errorLabel: string,
) {
  const { cloudName, uploadPreset } = getCloudinaryConfig();
  const formData = new FormData();
  const uploadFile = normalizeUploadFile(file, `${errorLabel}-${Date.now()}`);

  formData.append("file", uploadFile as unknown as Blob);
  formData.append("upload_preset", uploadPreset);
  formData.append("folder", folder);

  let response: Response;

  try {
    response = await fetch(
      `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`,
      {
        body: formData,
        method: "POST",
      },
    );
  } catch (requestError) {
    const rawMessage =
      requestError instanceof Error
        ? requestError.message
        : `Unable to upload ${errorLabel}.`;
    throw new Error(
      localizeApiErrorMessage(0, rawMessage, "upload.error.image"),
    );
  }

  if (!response.ok) {
    throw new Error(
      localizeApiErrorMessage(
        response.status,
        `Unable to upload ${errorLabel}.`,
        "upload.error.image",
      ),
    );
  }

  const data = (await response.json()) as CloudinaryUploadResponse;

  if (typeof data.secure_url !== "string" || !data.secure_url.trim()) {
    throw new Error(
      localizeApiErrorMessage(
        500,
        "Cloudinary upload response is missing secure_url.",
        "upload.error.image",
      ),
    );
  }

  return data.secure_url;
}

export function uploadAvatarToCloudinary(file: CloudinaryUploadFile) {
  return uploadImageToCloudinary(file, "cafestory/avatars", "avatar");
}

export function uploadPostImageToCloudinary(file: CloudinaryUploadFile) {
  return uploadImageToCloudinary(file, "cafestory/posts", "post image");
}

export function uploadAdImageToCloudinary(file: CloudinaryUploadFile) {
  return uploadImageToCloudinary(file, "cafestory/ads", "ad image");
}

export function uploadChatImageToCloudinary(file: CloudinaryUploadFile) {
  return uploadImageToCloudinary(file, "cafestory/messages", "message image");
}

export function uploadCafeImageToCloudinary(
  file: CloudinaryUploadFile,
  imageType: "avatar" | "cover",
) {
  return uploadImageToCloudinary(file, "cafestory/cafes", `cafe ${imageType}`);
}
