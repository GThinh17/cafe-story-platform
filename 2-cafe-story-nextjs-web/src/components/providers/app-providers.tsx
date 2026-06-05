"use client";

import type { ReactNode } from "react";
import { AuthGate } from "@/components/providers/auth-gate";
import { AuthProvider } from "@/components/providers/auth-provider";
import { BfcacheRestoreProvider } from "@/components/providers/bfcache-restore-provider";
import { TooltipProvider } from "@/components/ui/tooltip";

type AppProvidersProps = {
  children: ReactNode;
};

export function AppProviders({ children }: AppProvidersProps) {
  return (
    <TooltipProvider>
      <AuthProvider>
        <BfcacheRestoreProvider />
        <AuthGate>{children}</AuthGate>
      </AuthProvider>
    </TooltipProvider>
  );
}
