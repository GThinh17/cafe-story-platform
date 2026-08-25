import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UsernameSuggestionResponse,
} from "../../types";
import { apiCacheTtl, cachedApiCall, invalidateApiCache } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export async function login(request: LoginRequest) {
  const response = await apiFetch<AuthResponse>(apiEndpoints.auth.login, {
    body: request,
    method: "POST",
  });
  invalidateApiCache();
  return response;
}

export async function register(request: RegisterRequest) {
  const response = await apiFetch<AuthResponse>(apiEndpoints.auth.register, {
    body: request,
    method: "POST",
  });
  invalidateApiCache();
  return response;
}

export function getMe() {
  return cachedApiCall("auth:me", apiCacheTtl.shortUser, () =>
    apiFetch<AuthResponse>(apiEndpoints.auth.me, {
      method: "GET",
    }),
  );
}

export function getUsernameSuggestions(fullName: string) {
  const normalizedFullName = fullName.trim();

  if (!normalizedFullName) {
    return Promise.resolve<UsernameSuggestionResponse>({ suggestions: [] });
  }

  return cachedApiCall(`auth:username-suggestions:${normalizedFullName}`, apiCacheTtl.dynamic, () =>
    apiFetch<UsernameSuggestionResponse>(apiEndpoints.auth.usernameSuggestions(normalizedFullName), {
      method: "GET",
    }),
  );
}

export async function logout() {
  const response = await apiFetch<void>(apiEndpoints.auth.logout, {
    method: "POST",
  });
  invalidateApiCache();
  return response;
}
