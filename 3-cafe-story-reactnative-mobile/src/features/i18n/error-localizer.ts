import { getCurrentLocale } from "./current-locale";
import { translate } from "./translator";
import type { TranslationKey } from "./dictionaries/en";

export function localizeApiErrorMessage(
  statusCode: number,
  rawMessage?: null | string,
  fallbackKey: TranslationKey = "common.error.generic",
): string {
  const locale = getCurrentLocale();

  if (locale === "en" && rawMessage?.trim()) {
    return rawMessage;
  }

  if (statusCode === 0) {
    return translate(locale, "common.error.network");
  }
  if (statusCode === 401) {
    return translate(locale, "common.error.sessionExpired");
  }
  if (statusCode === 403) {
    return translate(locale, "common.error.forbidden");
  }
  if (statusCode === 429) {
    return translate(locale, "common.error.rateLimited");
  }
  if (statusCode >= 500) {
    return translate(locale, "common.error.server");
  }

  return translate(locale, fallbackKey);
}
