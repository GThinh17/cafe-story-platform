import { DEFAULT_LOCALE, type Locale } from "./config";
import { en, type Dictionary, type TranslationKey } from "./dictionaries/en";
import { vi } from "./dictionaries/vi";

export type TranslationValues = Record<string, number | string>;
export type Translate = (
  key: TranslationKey,
  values?: TranslationValues,
) => string;

const dictionaries: Record<Locale, Dictionary> = { en, vi };
const PLACEHOLDER_PATTERN = /\{(\w+)\}/g;

export function interpolate(
  template: string,
  values?: TranslationValues,
): string {
  if (!values) {
    return template;
  }

  return template.replace(PLACEHOLDER_PATTERN, (match, name: string) => {
    const value = values[name];
    return value === undefined ? match : String(value);
  });
}

export function translate(
  locale: Locale,
  key: TranslationKey,
  values?: TranslationValues,
): string {
  const template =
    dictionaries[locale][key] || dictionaries[DEFAULT_LOCALE][key] || key;

  return interpolate(template, values);
}

export function createTranslator(locale: Locale): Translate {
  return (key, values) => translate(locale, key, values);
}
