import AsyncStorage from "@react-native-async-storage/async-storage";
import { getLocales } from "expo-localization";
import {
  createContext,
  Fragment,
  type PropsWithChildren,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { AppState } from "react-native";
import { Platform } from "react-native";

import {
  DEFAULT_LOCALE,
  LOCALE_STORAGE_KEY,
  LOCALE_TAGS,
  isLocale,
  type Locale,
  type LocalePreference,
  type LocaleTag,
} from "./config";
import { setCurrentLocale } from "./current-locale";
import { createTranslator, type Translate } from "./translator";

export type LocaleContextValue = {
  isReady: boolean;
  locale: Locale;
  localeTag: LocaleTag;
  preference: LocalePreference;
  setLocalePreference: (value: LocalePreference) => Promise<void>;
  t: Translate;
};

const LocaleContext = createContext<LocaleContextValue | null>(null);

export function resolveDeviceLocale(): Locale {
  const languageCode = getLocales()[0]?.languageCode?.toLowerCase();
  return languageCode === "vi" ? "vi" : "en";
}

export function LocaleProvider({ children }: PropsWithChildren) {
  const [preference, setPreference] =
    useState<LocalePreference>(DEFAULT_LOCALE);
  const [locale, setLocale] = useState<Locale>(DEFAULT_LOCALE);
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    let isActive = true;

    AsyncStorage.getItem(LOCALE_STORAGE_KEY)
      .then((storedPreference) => {
        if (!isActive) {
          return;
        }

        if (storedPreference === "system") {
          setPreference("system");
          setLocale(resolveDeviceLocale());
          return;
        }

        if (isLocale(storedPreference)) {
          setPreference(storedPreference);
          setLocale(storedPreference);
          return;
        }

        setPreference(DEFAULT_LOCALE);
        setLocale(DEFAULT_LOCALE);
      })
      .catch(() => {
        if (isActive) {
          setPreference(DEFAULT_LOCALE);
          setLocale(DEFAULT_LOCALE);
        }
      })
      .finally(() => {
        if (isActive) {
          setIsReady(true);
        }
      });

    return () => {
      isActive = false;
    };
  }, []);

  useEffect(() => {
    if (preference !== "system") {
      return;
    }

    const subscription = AppState.addEventListener("change", (state) => {
      if (state === "active") {
        setLocale(resolveDeviceLocale());
      }
    });

    return () => subscription.remove();
  }, [preference]);

  useEffect(() => {
    if (Platform.OS === "web" && typeof document !== "undefined") {
      document.documentElement.lang = locale;
    }
  }, [locale]);

  const setLocalePreference = useCallback(
    async (nextPreference: LocalePreference) => {
      setPreference(nextPreference);
      setLocale(
        nextPreference === "system" ? resolveDeviceLocale() : nextPreference,
      );

      await AsyncStorage.setItem(LOCALE_STORAGE_KEY, nextPreference);
    },
    [],
  );

  setCurrentLocale(locale);

  const value = useMemo<LocaleContextValue>(
    () => ({
      isReady,
      locale,
      localeTag: LOCALE_TAGS[locale],
      preference,
      setLocalePreference,
      t: createTranslator(locale),
    }),
    [isReady, locale, preference, setLocalePreference],
  );

  return (
    <LocaleContext.Provider value={value}>
      <Fragment key={locale}>{children}</Fragment>
    </LocaleContext.Provider>
  );
}

export function useI18n(): LocaleContextValue {
  const context = useContext(LocaleContext);

  if (!context) {
    throw new Error("useI18n must be used within LocaleProvider");
  }

  return context;
}

export function useTranslate(): Translate {
  return useI18n().t;
}
