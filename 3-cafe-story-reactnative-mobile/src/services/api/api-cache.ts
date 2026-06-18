type CacheEntry<T> = {
  expiresAt: number;
  value: T;
};

const apiCache = new Map<string, CacheEntry<unknown>>();

export async function cachedApiCall<T>(
  key: string,
  ttlMs: number,
  loader: () => Promise<T>,
): Promise<T> {
  const now = Date.now();
  const cached = apiCache.get(key) as CacheEntry<T> | undefined;

  if (cached && cached.expiresAt > now) {
    return cached.value;
  }

  const value = await loader();
  apiCache.set(key, {
    expiresAt: now + ttlMs,
    value,
  });
  return value;
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
  chatActive: 5_000,
  chatHistory: 60_000,
  comments: 10_000,
  dynamic: 15_000,
  lookup: 24 * 60 * 60 * 1000,
  reportReasons: 30 * 60 * 1000,
  shortUser: 15_000,
} as const;
