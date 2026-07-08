"use client";

import type { ReactNode } from "react";
import { AdminAssistantDrawer } from "@/components/admin/admin-assistant-drawer";
import { AuthProvider } from "@/components/providers/auth-provider";

export function AppProviders({ children }: { children: ReactNode }) {
  return (
    <AuthProvider>
      {children}
      <AdminAssistantDrawer />
    </AuthProvider>
  );
}
