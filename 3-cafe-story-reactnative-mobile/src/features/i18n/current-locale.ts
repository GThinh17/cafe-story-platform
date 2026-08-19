import { DEFAULT_LOCALE, LOCALE_TAGS, type Locale } from "./config";
import { en, type TranslationKey } from "./dictionaries/en";
import { translate, type TranslationValues } from "./translator";
import { translateUiText, type UiPhraseKey } from "./ui-phrases";

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

/**
 * Translate explicit UI keys outside hooks. LocaleProvider remounts its child
 * tree when the locale changes, so render-time calls always use the active
 * locale. Backend/user content must never be passed to this function.
 */
export function t(
  key: TranslationKey | UiPhraseKey,
  values?: TranslationValues,
): string {
  if (Object.prototype.hasOwnProperty.call(en, key)) {
    return translate(currentLocale, key as TranslationKey, values);
  }
  return translateUiText(currentLocale, key);
}
