"use client";

import type { ReactNode } from "react";
import { AdminAssistantDrawer } from "@/components/admin/admin-assistant-drawer";
import { AuthProvider } from "@/components/providers/auth-provider";
import { LocaleProvider } from "@/features/i18n";
import type { ServerLocaleState } from "@/features/i18n/server";

export function AppProviders({
  children,
  initialLocaleState,
}: {
  children: ReactNode;
  initialLocaleState: ServerLocaleState;
}) {
  return (
    <LocaleProvider
      initialLocale={initialLocaleState.locale}
      initialPreference={initialLocaleState.preference}
      hasPersistedPreference={initialLocaleState.hasPersistedPreference}
    >
      <AuthProvider>
        {children}
        <AdminAssistantDrawer />
      </AuthProvider>
    </LocaleProvider>
  );
}
