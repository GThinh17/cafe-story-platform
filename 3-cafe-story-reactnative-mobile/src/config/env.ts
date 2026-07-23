export const env = {
  apiBaseUrl: process.env.EXPO_PUBLIC_API_BASE_URL,
  cloudinaryCloudName: process.env.EXPO_PUBLIC_CLOUDINARY_CLOUD_NAME,
  cloudinaryUploadPreset: process.env.EXPO_PUBLIC_CLOUDINARY_UPLOAD_PRESET,
  performanceLoggingEnabled:
    process.env.EXPO_PUBLIC_PERFORMANCE_LOGGING === "true",
} as const;

export function getApiBaseUrl() {
  if (!env.apiBaseUrl) {
    throw new Error("EXPO_PUBLIC_API_BASE_URL is not configured");
  }

  return env.apiBaseUrl;
}

export function getCloudinaryConfig() {
  if (!env.cloudinaryCloudName || !env.cloudinaryUploadPreset) {
    throw new Error("Cloudinary config is missing.");
  }

  return {
    cloudName: env.cloudinaryCloudName,
    uploadPreset: env.cloudinaryUploadPreset,
  };
}
