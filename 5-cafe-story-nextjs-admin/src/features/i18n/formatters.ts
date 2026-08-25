import { LOCALE_TAGS, type Locale, type LocaleTag } from "./config";

function localeTag(value: Locale | LocaleTag): LocaleTag {
  return value === "en" || value === "vi" ? LOCALE_TAGS[value] : value;
}

function validDate(value: string | number | Date | null | undefined): Date | null {
  if (value === null || value === undefined || value === "") return null;
  const date = value instanceof Date ? value : new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function formatDateTime(
  value: string | number | Date | null | undefined,
  locale: Locale | LocaleTag,
): string {
  const date = validDate(value);
  if (!date) return "—";
  return new Intl.DateTimeFormat(localeTag(locale), {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export function formatDate(
  value: string | number | Date | null | undefined,
  locale: Locale | LocaleTag,
): string {
  const date = validDate(value);
  if (!date) return "—";
  return new Intl.DateTimeFormat(localeTag(locale), {
    dateStyle: "medium",
  }).format(date);
}

export function formatTime(
  value: string | number | Date | null | undefined,
  locale: Locale | LocaleTag,
): string {
  const date = validDate(value);
  if (!date) return "—";
  return new Intl.DateTimeFormat(localeTag(locale), {
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

export function formatNumber(
  value: number,
  locale: Locale | LocaleTag,
  options?: Intl.NumberFormatOptions,
): string {
  return new Intl.NumberFormat(localeTag(locale), options).format(value);
}

export function formatCompactNumber(
  value: number,
  locale: Locale | LocaleTag,
  options?: Intl.NumberFormatOptions,
): string {
  return formatNumber(value, locale, {
    notation: "compact",
    maximumFractionDigits: 1,
    ...options,
  });
}

export function formatCurrency(
  value: number,
  currency: string | null | undefined,
  locale: Locale | LocaleTag,
  options?: Intl.NumberFormatOptions,
): string {
  return formatNumber(value, locale, {
    style: "currency",
    currency: currency?.trim().toUpperCase() || "VND",
    maximumFractionDigits: 0,
    ...options,
  });
}
