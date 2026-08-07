"use client";

import { CheckIcon } from "lucide-react";
import { useI18n } from "@/components/providers/locale-provider";
import { LOCALES, type Locale } from "@/lib/i18n";
import { cn } from "@/lib/utils";

const localeCopy: Record<
  Locale,
  { nameKey: "language.enNative" | "language.viNative"; hintKey: "language.enHint" | "language.viHint" }
> = {
  en: { nameKey: "language.enNative", hintKey: "language.enHint" },
  vi: { nameKey: "language.viNative", hintKey: "language.viHint" },
};

export function LanguageSettings() {
  const { locale, setLocale, t } = useI18n();

  return (
    <section className="space-y-5 rounded-md border border-border bg-surface p-4 sm:p-5">
      <div className="space-y-1">
        <h2 className="text-base font-bold text-foreground">{t("language.title")}</h2>
        <p className="text-xs text-muted">{t("language.description")}</p>
      </div>

      <fieldset className="grid gap-3 sm:grid-cols-2">
        <legend className="sr-only">{t("language.legend")}</legend>
        {LOCALES.map((option) => {
          const isSelected = option === locale;
          const copy = localeCopy[option];

          return (
            <label
              className={cn(
                "flex cursor-pointer items-start gap-3 rounded-md border p-3 transition-colors",
                isSelected
                  ? "border-primary bg-surface-muted"
                  : "border-border hover:bg-surface-muted",
              )}
              key={option}
            >
              <input
                checked={isSelected}
                className="sr-only"
                name="interface-language"
                onChange={() => setLocale(option)}
                type="radio"
                value={option}
              />
              <span
                aria-hidden
                className={cn(
                  "mt-0.5 grid size-5 shrink-0 place-items-center rounded-full border",
                  isSelected
                    ? "border-primary bg-primary text-primary-foreground"
                    : "border-border bg-surface",
                )}
              >
                {isSelected ? <CheckIcon className="size-3" strokeWidth={3} /> : null}
              </span>
              <span className="flex min-w-0 flex-col gap-1">
                <span className="text-sm font-medium text-foreground">
                  {t(copy.nameKey)}
                </span>
                <span className="text-xs text-muted">{t(copy.hintKey)}</span>
              </span>
            </label>
          );
        })}
      </fieldset>
    </section>
  );
}
