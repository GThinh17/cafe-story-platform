import { ApiError } from "@/lib/api/client";
import type { Locale } from "./config";
import type { TranslationKey } from "./dictionaries/en";
import type { Translate } from "./translator";

export function localizeApiError(
  error: unknown,
  locale: Locale,
  t: Translate,
  fallbackKey: TranslationKey = "common.error.generic",
): string {
  if (locale === "en" && error instanceof Error && error.message.trim()) {
    return error.message;
  }

  if (error instanceof ApiError) {
    if (error.statusCode === 0) return t("common.error.network");
    if (error.statusCode === 401) return t("common.error.sessionExpired");
    if (error.statusCode === 403) return t("common.error.forbidden");
    if (error.statusCode === 429) return t("common.error.rateLimited");
    if (error.statusCode >= 500) return t("common.error.server");
  }

  return t(fallbackKey);
}
