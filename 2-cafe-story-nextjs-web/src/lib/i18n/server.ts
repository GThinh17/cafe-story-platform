import { cookies } from "next/headers";
import { LOCALE_STORAGE_KEY, normalizeLocale, type Locale } from "@/lib/i18n/config";
import { createTranslator, type Translate } from "@/lib/i18n";

/**
 * Locale for server components. Reads the same cookie the client provider
 * writes, so server-rendered copy matches what the user picked.
 */
export async function getServerLocale(): Promise<Locale> {
  const cookieStore = await cookies();

  return normalizeLocale(cookieStore.get(LOCALE_STORAGE_KEY)?.value);
}

export async function getServerTranslator(): Promise<Translate> {
  return createTranslator(await getServerLocale());
}
