"use client";

import type { ReactNode } from "react";
import { BfcacheRestoreProvider } from "@/components/providers/bfcache-restore-provider";
import { TooltipProvider } from "@/components/ui/tooltip";

type AppProvidersProps = {
  children: ReactNode;
};

export function AppProviders({ children }: AppProvidersProps) {
  return (
    <TooltipProvider>
      <BfcacheRestoreProvider />
      {children}
    </TooltipProvider>
  );
}
