import { DEFAULT_LOCALE, LOCALE_TAGS, type Locale } from "./config";

let currentLocale: Locale = DEFAULT_LOCALE;

export function getCurrentLocale(): Locale {
  return currentLocale;
}

export function getCurrentLocaleTag() {
  return LOCALE_TAGS[currentLocale];
}

export function setCurrentLocale(locale: Locale): void {
  currentLocale = locale;
}
