"use client";

import { useEffect, useRef } from "react";
import { useRouter } from "next/navigation";

const BFCACHE_RESTORE_EVENT = "cafestory:bfcache-restore";

type BfcacheRestoreOptions = {
  onRestore?: () => void | Promise<void>;
  refreshRouter?: boolean;
};

export function useBfcacheRestore(options: BfcacheRestoreOptions = {}) {
  const router = useRouter();
  const onRestoreRef = useRef(options.onRestore);
  const refreshRouter = options.refreshRouter ?? true;

  useEffect(() => {
    onRestoreRef.current = options.onRestore;
  }, [options.onRestore]);

  useEffect(() => {
    function handlePageShow(event: PageTransitionEvent) {
      if (!event.persisted) {
        return;
      }

      if (refreshRouter) {
        router.refresh();
      }

      window.dispatchEvent(new Event(BFCACHE_RESTORE_EVENT));
      void onRestoreRef.current?.();
    }

    window.addEventListener("pageshow", handlePageShow);

    return () => {
      window.removeEventListener("pageshow", handlePageShow);
    };
  }, [refreshRouter, router]);
}

export function useBfcacheRestoreEffect(
  onRestore?: () => void | Promise<void>,
) {
  const onRestoreRef = useRef(onRestore);

  useEffect(() => {
    onRestoreRef.current = onRestore;
  }, [onRestore]);

  useEffect(() => {
    function handleBfcacheRestore() {
      void onRestoreRef.current?.();
    }

    window.addEventListener(BFCACHE_RESTORE_EVENT, handleBfcacheRestore);

    return () => {
      window.removeEventListener(BFCACHE_RESTORE_EVENT, handleBfcacheRestore);
    };
  }, []);
}
