"use client";

import { useAuth } from "@/components/providers/auth-provider";
import type { AuthState } from "@/types/auth";

export function useCurrentUser(): AuthState {
  return useAuth();
}
