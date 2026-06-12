import type { AuthResponse, LoginRequest, RegisterRequest } from "../../types";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function login(request: LoginRequest) {
  return apiFetch<AuthResponse>(apiEndpoints.auth.login, {
    body: request,
    method: "POST",
  });
}

export function register(request: RegisterRequest) {
  return apiFetch<AuthResponse>(apiEndpoints.auth.register, {
    body: request,
    method: "POST",
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
