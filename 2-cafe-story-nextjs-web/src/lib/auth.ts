const AUTH_STORAGE_KEYS = [
  "access_token",
  "auth_token",
  "refresh_token",
  "accessToken",
  "refreshToken",
  "authToken",
  "token",
  "jwt",
  "user",
  "auth_user",
  "cafestory_access_token",
  "cafestory_refresh_token",
  "cafestory_user",
];

export function clearStoredAuthTokens() {
  if (typeof window === "undefined") {
    return;
  }

  for (const key of AUTH_STORAGE_KEYS) {
    window.localStorage.removeItem(key);
  }
}
