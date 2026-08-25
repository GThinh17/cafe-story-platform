export const LOCALES = ["en", "vi"] as const;

export type Locale = (typeof LOCALES)[number];

export const DEFAULT_LOCALE: Locale = "en";

/** Shared by the cookie (read on the server) and localStorage (read on the client). */
export const LOCALE_STORAGE_KEY = "cafestory-locale";

/** One year, in seconds. */
export const LOCALE_COOKIE_MAX_AGE = 60 * 60 * 24 * 365;

export const LOCALE_LABELS: Record<Locale, string> = {
  en: "English",
  vi: "Tiếng Việt",
};

/** BCP 47 tags for `<html lang>` and `Intl.*` formatters. */
export const LOCALE_HTML_LANG: Record<Locale, string> = {
  en: "en",
  vi: "vi-VN",
};

export function isLocale(value: unknown): value is Locale {
  return typeof value === "string" && (LOCALES as readonly string[]).includes(value);
}

export function normalizeLocale(value: string | null | undefined): Locale {
  return isLocale(value) ? value : DEFAULT_LOCALE;
}
