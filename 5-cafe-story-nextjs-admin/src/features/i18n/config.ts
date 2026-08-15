export const LOCALES = ["en", "vi"] as const;
export const LOCALE_PREFERENCES = ["system", ...LOCALES] as const;

export type Locale = (typeof LOCALES)[number];
export type LocalePreference = (typeof LOCALE_PREFERENCES)[number];
export type LocaleTag = "en-US" | "vi-VN";

export const DEFAULT_LOCALE: Locale = "en";
export const LOCALE_PREFERENCE_KEY = "cafestory-admin-locale-preference";
export const LOCALE_COOKIE_MAX_AGE = 60 * 60 * 24 * 365;

export const LOCALE_TAGS: Record<Locale, LocaleTag> = {
  en: "en-US",
  vi: "vi-VN",
};

export function isLocale(value: unknown): value is Locale {
  return typeof value === "string" && LOCALES.includes(value as Locale);
}

export function isLocalePreference(value: unknown): value is LocalePreference {
  return (
    typeof value === "string" &&
    LOCALE_PREFERENCES.includes(value as LocalePreference)
  );
}

export function resolveLocaleFromLanguages(
  languages: readonly string[] | null | undefined,
): Locale {
  for (const language of languages ?? []) {
    const languageCode = language.trim().toLowerCase().split(/[-_]/, 1)[0];
    if (languageCode === "vi") return "vi";
    if (languageCode === "en") return "en";
  }
  return DEFAULT_LOCALE;
}

export function resolveLocaleFromAcceptLanguage(
  acceptLanguage: string | null | undefined,
): Locale {
  if (!acceptLanguage?.trim()) return DEFAULT_LOCALE;

  const languages = acceptLanguage
    .split(",")
    .map((entry, index) => {
      const [language = "", ...parameters] = entry.trim().split(";");
      const qualityParameter = parameters.find((parameter) =>
        parameter.trim().startsWith("q="),
      );
      const parsedQuality = qualityParameter
        ? Number(qualityParameter.trim().slice(2))
        : 1;
      return {
        language,
        quality: Number.isFinite(parsedQuality) ? parsedQuality : 0,
        index,
      };
    })
    .filter((entry) => entry.language && entry.quality > 0)
    .sort((left, right) => right.quality - left.quality || left.index - right.index)
    .map((entry) => entry.language);

  return resolveLocaleFromLanguages(languages);
}

export function resolveLocalePreference(
  preference: LocalePreference,
  systemLocale: Locale,
): Locale {
  return isLocale(preference) ? preference : systemLocale;
}
