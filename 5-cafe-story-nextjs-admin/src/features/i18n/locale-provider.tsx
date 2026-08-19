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
import { BrandIcon } from "@/components/ui/brand-icon";
import {
  LOCALE_COOKIE_MAX_AGE,
  LOCALE_PREFERENCE_KEY,
  LOCALE_TAGS,
  isLocalePreference,
  resolveLocaleFromLanguages,
  resolveLocalePreference,
  type Locale,
  type LocalePreference,
  type LocaleTag,
} from "./config";
import { createTranslator, type Translate } from "./translator";

export type LocaleContextValue = {
  locale: Locale;
  localeTag: LocaleTag;
  preference: LocalePreference;
  isReady: boolean;
  setLocalePreference: (value: LocalePreference) => void;
  t: Translate;
};

type LocaleProviderProps = {
  children: ReactNode;
  initialLocale: Locale;
  initialPreference: LocalePreference;
  hasPersistedPreference: boolean;
};

const LocaleContext = createContext<LocaleContextValue | null>(null);

function browserLocale(): Locale {
  if (typeof navigator === "undefined") return "vi";
  const languages = navigator.languages?.length
    ? navigator.languages
    : [navigator.language];
  return resolveLocaleFromLanguages(languages);
}

function persistPreference(preference: LocalePreference) {
  try {
    window.localStorage.setItem(LOCALE_PREFERENCE_KEY, preference);
  } catch {
    // The cookie remains the SSR persistence channel when localStorage is blocked.
  }

  document.cookie = `${LOCALE_PREFERENCE_KEY}=${preference}; path=/; max-age=${LOCALE_COOKIE_MAX_AGE}; samesite=lax`;
}

function LocaleBootGate() {
  return (
    <div className="grid min-h-screen place-items-center bg-background" aria-busy="true">
      <BrandIcon className="size-12 animate-pulse motion-reduce:animate-none" />
    </div>
  );
}

export function LocaleProvider({
  children,
  initialLocale,
  initialPreference,
  hasPersistedPreference,
}: LocaleProviderProps) {
  const [locale, setLocale] = useState(initialLocale);
  const [preference, setPreference] =
    useState<LocalePreference>(initialPreference);
  const [isReady, setIsReady] = useState(hasPersistedPreference);

  const applyPreference = useCallback((nextPreference: LocalePreference) => {
    const nextLocale = resolveLocalePreference(nextPreference, browserLocale());
    setPreference(nextPreference);
    setLocale(nextLocale);
    document.documentElement.lang = LOCALE_TAGS[nextLocale];
    persistPreference(nextPreference);
  }, []);

  useEffect(() => {
    if (hasPersistedPreference) {
      try {
        window.localStorage.setItem(LOCALE_PREFERENCE_KEY, initialPreference);
      } catch {
        // Cookie already supplied the SSR-safe preference.
      }
      document.documentElement.lang = LOCALE_TAGS[initialLocale];
      setIsReady(true);
      return;
    }

    let storedPreference: string | null = null;
    try {
      storedPreference = window.localStorage.getItem(LOCALE_PREFERENCE_KEY);
    } catch {
      storedPreference = null;
    }

    applyPreference(
      isLocalePreference(storedPreference) ? storedPreference : "vi",
    );
    setIsReady(true);
  }, [applyPreference, hasPersistedPreference, initialLocale, initialPreference]);

  useEffect(() => {
    if (preference !== "system" || !isReady) return;

    const refreshSystemLocale = () => {
      const nextLocale = browserLocale();
      setLocale(nextLocale);
      document.documentElement.lang = LOCALE_TAGS[nextLocale];
    };
    const handleVisibilityChange = () => {
      if (document.visibilityState === "visible") refreshSystemLocale();
    };

    window.addEventListener("languagechange", refreshSystemLocale);
    window.addEventListener("pageshow", refreshSystemLocale);
    document.addEventListener("visibilitychange", handleVisibilityChange);
    return () => {
      window.removeEventListener("languagechange", refreshSystemLocale);
      window.removeEventListener("pageshow", refreshSystemLocale);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [isReady, preference]);

  const value = useMemo<LocaleContextValue>(
    () => ({
      locale,
      localeTag: LOCALE_TAGS[locale],
      preference,
      isReady,
      setLocalePreference: applyPreference,
      t: createTranslator(locale),
    }),
    [applyPreference, isReady, locale, preference],
  );

  return (
    <LocaleContext.Provider value={value}>
      {isReady ? children : <LocaleBootGate />}
    </LocaleContext.Provider>
  );
}

export function useI18n(): LocaleContextValue {
  const context = useContext(LocaleContext);
  if (!context) {
    throw new Error("useI18n must be used inside LocaleProvider.");
  }
  return context;
}

export function useTranslate(): Translate {
  return useI18n().t;
}
