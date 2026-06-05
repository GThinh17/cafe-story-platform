type CloudinaryUploadResponse = {
  secure_url?: unknown;
};

export async function uploadAvatarToCloudinary(file: File) {
  const cloudName = process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME;
  const uploadPreset = process.env.NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET;

  if (!cloudName || !uploadPreset) {
    throw new Error("Cloudinary config is missing.");
  }

  const formData = new FormData();
  formData.append("file", file);
  formData.append("upload_preset", uploadPreset);
  formData.append("folder", "cafestory/avatars");

  const response = await fetch(
    `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`,
    {
      method: "POST",
      body: formData,
    },
  );

  if (!response.ok) {
    throw new Error("Unable to upload avatar.");
  }

  const data = (await response.json()) as CloudinaryUploadResponse;

  if (typeof data.secure_url !== "string" || !data.secure_url.trim()) {
    throw new Error("Cloudinary upload response is missing secure_url.");
  }

  return data.secure_url;
}
