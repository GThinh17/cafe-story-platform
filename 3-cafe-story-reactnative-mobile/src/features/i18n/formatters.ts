import { LOCALE_TAGS, type Locale, type LocaleTag } from "./config";
import { getCurrentLocale } from "./current-locale";

type DateInput = Date | number | string;

function toDate(value: DateInput): Date {
  return value instanceof Date ? value : new Date(value);
}

export function localeTagFor(locale: Locale): LocaleTag {
  return LOCALE_TAGS[locale];
}

export function formatDate(
  value: DateInput,
  locale: Locale,
  options: Intl.DateTimeFormatOptions = { dateStyle: "medium" },
): string {
  return new Intl.DateTimeFormat(localeTagFor(locale), options).format(
    toDate(value),
  );
}

export function formatTime(
  value: DateInput,
  locale: Locale,
  options: Intl.DateTimeFormatOptions = {
    hour: "2-digit",
    minute: "2-digit",
  },
): string {
  return new Intl.DateTimeFormat(localeTagFor(locale), options).format(
    toDate(value),
  );
}

export function formatNumber(
  value: number,
  locale: Locale,
  options?: Intl.NumberFormatOptions,
): string {
  return new Intl.NumberFormat(localeTagFor(locale), options).format(value);
}

export function formatCompactNumber(value: number, locale: Locale): string {
  return formatNumber(value, locale, {
    maximumFractionDigits: 1,
    notation: "compact",
  });
}

export function formatCurrency(
  value: number,
  locale: Locale,
  currency = "VND",
): string {
  return formatNumber(value, locale, {
    currency,
    currencyDisplay: "symbol",
    maximumFractionDigits: currency === "VND" ? 0 : 2,
    style: "currency",
  });
}

export function formatCurrentDate(
  value: DateInput,
  options?: Intl.DateTimeFormatOptions,
): string {
  return formatDate(value, getCurrentLocale(), options);
}

export function formatCurrentTime(
  value: DateInput,
  options?: Intl.DateTimeFormatOptions,
): string {
  return formatTime(value, getCurrentLocale(), options);
}

export function formatCurrentNumber(
  value: number,
  options?: Intl.NumberFormatOptions,
): string {
  return formatNumber(value, getCurrentLocale(), options);
}

export function formatCurrentCompactNumber(value: number): string {
  return formatCompactNumber(value, getCurrentLocale());
}

export function formatCurrentCurrency(
  value: number,
  currency = "VND",
): string {
  return formatCurrency(value, getCurrentLocale(), currency);
}
