import "server-only";

import { cookies, headers } from "next/headers";
import {
  LOCALE_PREFERENCE_KEY,
  isLocalePreference,
  resolveLocaleFromAcceptLanguage,
  resolveLocalePreference,
  type Locale,
  type LocalePreference,
} from "./config";
import { createTranslator } from "./translator";

export type ServerLocaleState = {
  locale: Locale;
  preference: LocalePreference;
  hasPersistedPreference: boolean;
};

export async function getServerLocaleState(): Promise<ServerLocaleState> {
  const [cookieStore, headerStore] = await Promise.all([cookies(), headers()]);
  const storedPreference = cookieStore.get(LOCALE_PREFERENCE_KEY)?.value;
  const hasPersistedPreference = isLocalePreference(storedPreference);
  const preference = hasPersistedPreference ? storedPreference : "system";
  const systemLocale = resolveLocaleFromAcceptLanguage(
    headerStore.get("accept-language"),
  );

  return {
    locale: resolveLocalePreference(preference, systemLocale),
    preference,
    hasPersistedPreference,
  };
}

export async function getServerTranslator() {
  const { locale } = await getServerLocaleState();
  return createTranslator(locale);
}
