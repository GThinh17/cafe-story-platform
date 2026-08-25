"use client";

import { useEffect, useState } from "react";
import { cn } from "@/lib/utils";
import {
  LOCALE_PREFERENCES,
  resolveLocaleFromLanguages,
  type LocalePreference,
} from "./config";
import { useI18n } from "./locale-provider";
import { createTranslator } from "./translator";

const SHORT_LABELS: Record<LocalePreference, string> = {
  system: "System",
  en: "EN",
  vi: "VI",
};

export function LanguageSelector({ className }: { className?: string }) {
  const { preference, setLocalePreference, t } = useI18n();
  const [announcement, setAnnouncement] = useState("");

  useEffect(() => {
    if (!announcement) return;
    const timer = window.setTimeout(() => setAnnouncement(""), 1_500);
    return () => window.clearTimeout(timer);
  }, [announcement]);

  const fullLabels: Record<LocalePreference, string> = {
    system: t("common.language.system"),
    en: t("common.language.english"),
    vi: t("common.language.vietnamese"),
  };

  function select(nextPreference: LocalePreference) {
    if (nextPreference === preference) return;
    setLocalePreference(nextPreference);
    const nextLocale =
      nextPreference === "system"
        ? resolveLocaleFromLanguages(navigator.languages)
        : nextPreference;
    const nextT = createTranslator(nextLocale);
    const languageKey =
      nextPreference === "system"
        ? "common.language.system"
        : nextPreference === "vi"
          ? "common.language.vietnamese"
          : "common.language.english";
    setAnnouncement(
      nextT("common.language.changed", { language: nextT(languageKey) }),
    );
  }

  function handleKeyDown(event: React.KeyboardEvent<HTMLDivElement>) {
    if (!["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key)) {
      return;
    }
    event.preventDefault();
    const currentIndex = LOCALE_PREFERENCES.indexOf(preference);
    let nextIndex = currentIndex;
    if (event.key === "Home") nextIndex = 0;
    if (event.key === "End") nextIndex = LOCALE_PREFERENCES.length - 1;
    if (event.key === "ArrowLeft") {
      nextIndex = (currentIndex - 1 + LOCALE_PREFERENCES.length) % LOCALE_PREFERENCES.length;
    }
    if (event.key === "ArrowRight") {
      nextIndex = (currentIndex + 1) % LOCALE_PREFERENCES.length;
    }
    const nextPreference = LOCALE_PREFERENCES[nextIndex];
    select(nextPreference);
    event.currentTarget
      .querySelector<HTMLButtonElement>(`[data-locale-preference="${nextPreference}"]`)
      ?.focus();
  }

  return (
    <div className={cn("min-w-0", className)}>
      <div
        role="radiogroup"
        aria-label={t("common.language.label")}
        className="grid grid-cols-3 gap-1 rounded-md border border-border bg-surface-muted p-1"
        onKeyDown={handleKeyDown}
      >
        {LOCALE_PREFERENCES.map((option) => {
          const selected = option === preference;
          return (
            <button
              key={option}
              type="button"
              role="radio"
              aria-checked={selected}
              aria-label={fullLabels[option]}
              data-locale-preference={option}
              tabIndex={selected ? 0 : -1}
              className={cn(
                "min-h-8 rounded-sm px-2 text-xs font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/30",
                selected
                  ? "bg-surface text-primary shadow-sm"
                  : "text-muted hover:bg-surface/70 hover:text-foreground",
              )}
              onClick={() => select(option)}
            >
              {SHORT_LABELS[option]}
            </button>
          );
        })}
      </div>
      <span className="sr-only" aria-live="polite" aria-atomic="true">
        {announcement}
      </span>
    </div>
  );
}
