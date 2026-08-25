import { DEFAULT_LOCALE, type Locale } from "@/lib/i18n/config";
import { en, type Dictionary, type TranslationKey } from "@/lib/i18n/en";
import { vi } from "@/lib/i18n/vi";

export type { Dictionary, TranslationKey };
export { en, vi };

export const dictionaries: Record<Locale, Dictionary> = { en, vi };

export type TranslationValues = Record<string, string | number>;

const PLACEHOLDER_PATTERN = /\{(\w+)\}/g;

export function interpolate(template: string, values?: TranslationValues) {
  if (!values) {
    return template;
  }

  return template.replace(PLACEHOLDER_PATTERN, (match, name: string) => {
    const value = values[name];
    return value === undefined ? match : String(value);
  });
}

/**
 * Resolves a key for `locale`, falling back to English when an entry is empty.
 * The key itself is the last resort so a typo surfaces loudly instead of
 * rendering an empty node.
 */
export function translate(
  locale: Locale,
  key: TranslationKey,
  values?: TranslationValues,
) {
  const template =
    dictionaries[locale]?.[key] || dictionaries[DEFAULT_LOCALE][key] || key;

  return interpolate(template, values);
}

export type Translate = (key: TranslationKey, values?: TranslationValues) => string;

export function createTranslator(locale: Locale): Translate {
  return (key, values) => translate(locale, key, values);
}

export * from "@/lib/i18n/config";
