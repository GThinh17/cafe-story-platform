export const env = {
  apiBaseUrl: process.env.EXPO_PUBLIC_API_BASE_URL,
} as const;

export function getApiBaseUrl() {
  if (!env.apiBaseUrl) {
    throw new Error("EXPO_PUBLIC_API_BASE_URL is not configured");
  }

  return env.apiBaseUrl;
}
