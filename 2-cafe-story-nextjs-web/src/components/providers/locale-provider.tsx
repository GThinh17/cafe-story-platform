"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import {
  DEFAULT_LOCALE,
  LOCALE_COOKIE_MAX_AGE,
  LOCALE_HTML_LANG,
  LOCALE_STORAGE_KEY,
  createTranslator,
  normalizeLocale,
  type Locale,
  type Translate,
} from "@/lib/i18n";

type LocaleContextValue = {
  locale: Locale;
  setLocale: (locale: Locale) => void;
  t: Translate;
};

const LocaleContext = createContext<LocaleContextValue | null>(null);

function persistLocale(locale: Locale) {
  try {
    localStorage.setItem(LOCALE_STORAGE_KEY, locale);
  } catch {
    // Private mode / storage disabled: the cookie below still carries the choice.
  }

  // Cookie is what the server layout reads, so SSR renders the right <html lang>.
  document.cookie = `${LOCALE_STORAGE_KEY}=${locale}; path=/; max-age=${LOCALE_COOKIE_MAX_AGE}; samesite=lax`;
  document.documentElement.lang = LOCALE_HTML_LANG[locale];
}

type LocaleProviderProps = {
  children: ReactNode;
  /** Read from the cookie on the server so the first paint matches the choice. */
  initialLocale?: Locale;
};

export function LocaleProvider({
  children,
  initialLocale = DEFAULT_LOCALE,
}: LocaleProviderProps) {
  const [locale, setLocaleState] = useState<Locale>(initialLocale);

  // The cookie can be missing while localStorage still holds a choice (first
  // visit after the feature ships, or a cookie cleared by the browser).
  useEffect(() => {
    let stored: string | null = null;

    try {
      stored = localStorage.getItem(LOCALE_STORAGE_KEY);
    } catch {
      stored = null;
    }

    const resolved = normalizeLocale(stored);

    if (resolved !== locale) {
      setLocaleState(resolved);
    }

    persistLocale(resolved);
    // Runs once: later changes go through setLocale.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const setLocale = useCallback((next: Locale) => {
    setLocaleState(next);
    persistLocale(next);
  }, []);

  const value = useMemo<LocaleContextValue>(
    () => ({ locale, setLocale, t: createTranslator(locale) }),
    [locale, setLocale],
  );

  return <LocaleContext.Provider value={value}>{children}</LocaleContext.Provider>;
}

export function useI18n() {
  const context = useContext(LocaleContext);

  if (!context) {
    throw new Error("useI18n must be used inside <LocaleProvider>.");
  }

  return context;
}

/** Shorthand for components that only need the translate function. */
export function useTranslate() {
  return useI18n().t;
}
