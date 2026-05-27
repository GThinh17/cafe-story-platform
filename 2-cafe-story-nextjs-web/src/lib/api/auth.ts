import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import { clearStoredAuthTokens } from "@/lib/auth";
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UsernameSuggestionResponse,
} from "@/types/auth";

export async function login(request: LoginRequest) {
  const response = await apiFetch<AuthResponse>(apiEndpoints.auth.login, {
    method: "POST",
    body: request,
  });

  clearStoredAuthTokens();

  return response;
}

export function register(request: RegisterRequest) {
  return apiFetch<AuthResponse>(apiEndpoints.auth.register, {
    method: "POST",
    body: request,
  });
}

export function getMe() {
  return apiFetch<AuthResponse>(apiEndpoints.auth.me, {
    method: "GET",
  });
}

export function refreshSession() {
  return apiFetch<AuthResponse>(apiEndpoints.auth.refresh, {
    method: "POST",
  });
}

export function suggestUserNames(fullName: string, signal?: AbortSignal) {
  return apiFetch<UsernameSuggestionResponse>(
    apiEndpoints.auth.usernameSuggestions(fullName),
    {
      method: "GET",
      signal,
    },
  );
}

export function logout() {
  return apiFetch<void>(apiEndpoints.auth.logout, {
    method: "POST",
  }).finally(clearStoredAuthTokens);
}
