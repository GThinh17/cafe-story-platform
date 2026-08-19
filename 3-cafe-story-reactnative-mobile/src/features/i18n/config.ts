export const LOCALES = ["en", "vi"] as const;

export type Locale = (typeof LOCALES)[number];
export type LocalePreference = "system" | Locale;
export type LocaleTag = "en-US" | "vi-VN";

export const DEFAULT_LOCALE: Locale = "vi";
export const LOCALE_STORAGE_KEY = "cafestory-locale-preference";

export const LOCALE_TAGS: Record<Locale, LocaleTag> = {
  en: "en-US",
  vi: "vi-VN",
};

export function isLocale(value: unknown): value is Locale {
  return typeof value === "string" && LOCALES.includes(value as Locale);
}

export function normalizeLocale(value: unknown): Locale {
  return isLocale(value) ? value : DEFAULT_LOCALE;
}
