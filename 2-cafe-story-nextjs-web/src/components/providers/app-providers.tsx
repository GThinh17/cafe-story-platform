"use client";

import type { ReactNode } from "react";
import { AuthGate } from "@/components/providers/auth-gate";
import { AuthProvider } from "@/components/providers/auth-provider";
import { BfcacheRestoreProvider } from "@/components/providers/bfcache-restore-provider";
import { LocaleProvider } from "@/components/providers/locale-provider";
import { TooltipProvider } from "@/components/ui/tooltip";
import type { Locale } from "@/lib/i18n";

type AppProvidersProps = {
  children: ReactNode;
  initialLocale?: Locale;
};

export function AppProviders({ children, initialLocale }: AppProvidersProps) {
  return (
    <LocaleProvider initialLocale={initialLocale}>
      <TooltipProvider>
        <AuthProvider>
          <BfcacheRestoreProvider />
          <AuthGate>{children}</AuthGate>
        </AuthProvider>
      </TooltipProvider>
    </LocaleProvider>
  );
}
