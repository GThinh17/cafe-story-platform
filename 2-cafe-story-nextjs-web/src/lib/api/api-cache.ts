// The promise is stored, not the resolved value, so callers that arrive while a
// request is still in flight join that request instead of starting their own.
// Screens that fan out over a list (mention picker, notification actors, chat
// participants) ask for the same key many times within the same tick.
type CacheEntry<T> = {
  expiresAt: number;
  promise: Promise<T>;
};

const apiCache = new Map<string, CacheEntry<unknown>>();

// Never cache during SSR: this module-level Map lives for the whole Next
// server process and would be shared across requests from different users.
function isBrowser() {
  return typeof window !== "undefined";
}

export function cachedApiCall<T>(
  key: string,
  ttlMs: number,
  loader: () => Promise<T>,
): Promise<T> {
  if (!isBrowser()) {
    return loader();
  }

  const now = Date.now();
  const cached = apiCache.get(key) as CacheEntry<T> | undefined;

  if (cached && cached.expiresAt > now) {
    return cached.promise;
  }

  const promise = loader();
  const entry: CacheEntry<T> = { expiresAt: now + ttlMs, promise };
  apiCache.set(key, entry);

  // A failed request must not be served for the rest of the TTL. Only drop the
  // entry if it is still the one this call installed — a later invalidate or a
  // newer request may already have replaced it.
  promise.catch(() => {
    if (apiCache.get(key) === entry) {
      apiCache.delete(key);
    }
  });

  return promise;
}

export function invalidateApiCache(keyOrPrefix?: string) {
  if (!keyOrPrefix) {
    apiCache.clear();
    return;
  }

  for (const key of apiCache.keys()) {
    if (key === keyOrPrefix || key.startsWith(keyOrPrefix)) {
      apiCache.delete(key);
    }
  }
}

export const apiCacheTtl = {
  dynamic: 15_000,
  lookup: 24 * 60 * 60 * 1000,
  shortUser: 15_000,
} as const;
