import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { AuthResponse, LoginRequest, RegisterRequest } from "@/types/auth";

export function login(request: LoginRequest) {
  return apiFetch<AuthResponse>(apiEndpoints.auth.login, {
    method: "POST",
    body: request,
  });
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

export function logout() {
  return apiFetch<void>(apiEndpoints.auth.logout, {
    method: "POST",
  });
}
