"use client";

import { useBfcacheRestore } from "@/hooks/use-bfcache-restore";

export function BfcacheRestoreProvider() {
  useBfcacheRestore({ refreshRouter: false });

  return null;
}
