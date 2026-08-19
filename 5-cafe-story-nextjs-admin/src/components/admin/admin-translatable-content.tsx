"use client";

import { Languages, LoaderCircle, RotateCcw } from "lucide-react";
import { useEffect, useRef, useState } from "react";

import { Button } from "@/components/ui/button";
import { useI18n, useUiText } from "@/features/i18n";
import { translateAdminContent } from "@/lib/api/admin";
import { cn } from "@/lib/utils";
import type {
  AdminContentTranslationResponse,
  AdminTranslationContentKind,
} from "@/types/admin";

type AdminTranslatableContentProps = {
  className?: string;
  contentKind: AdminTranslationContentKind;
  text: string | null | undefined;
  textClassName?: string;
};

const sessionTranslationCache = new Map<string, AdminContentTranslationResponse>();

async function sha256(value: string) {
  if (globalThis.crypto?.subtle) {
    const bytes = new TextEncoder().encode(value);
    const digest = await globalThis.crypto.subtle.digest("SHA-256", bytes);
    return [...new Uint8Array(digest)]
      .map((byte) => byte.toString(16).padStart(2, "0"))
      .join("");
  }

  let fallback = 2166136261;
  for (let index = 0; index < value.length; index += 1) {
    fallback ^= value.charCodeAt(index);
    fallback = Math.imul(fallback, 16777619);
  }
  return `fnv-${(fallback >>> 0).toString(16)}`;
}

async function cacheKey(
  text: string,
  targetLocale: "en" | "vi",
  contentKind: AdminTranslationContentKind,
) {
  return `${targetLocale}:${contentKind}:${await sha256(text)}`;
}

export function AdminTranslatableContent({
  className,
  contentKind,
  text,
  textClassName,
}: AdminTranslatableContentProps) {
  const { locale } = useI18n();
  const ui = useUiText();
  const requestSequence = useRef(0);
  const normalizedText = text?.trim() ?? "";
  const [translation, setTranslation] =
    useState<AdminContentTranslationResponse | null>(null);
  const [showTranslation, setShowTranslation] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    requestSequence.current += 1;
    setTranslation(null);
    setShowTranslation(false);
    setIsLoading(false);
    setError(false);
  }, [contentKind, locale, normalizedText]);

  async function translate() {
    if (!normalizedText || isLoading) return;

    const sequence = requestSequence.current + 1;
    requestSequence.current = sequence;
    setIsLoading(true);
    setError(false);

    try {
      const key = await cacheKey(normalizedText, locale, contentKind);
      const cached = sessionTranslationCache.get(key);
      const response =
        cached ??
        (await translateAdminContent({
          contentKind,
          targetLocale: locale,
          text: normalizedText,
        }));

      if (sequence !== requestSequence.current) return;
      sessionTranslationCache.set(key, response);
      setTranslation(response);
      setShowTranslation(true);
    } catch {
      if (sequence === requestSequence.current) {
        setError(true);
        setShowTranslation(false);
      }
    } finally {
      if (sequence === requestSequence.current) setIsLoading(false);
    }
  }

  const displayedText =
    showTranslation && translation ? translation.translatedText : normalizedText || "—";

  return (
    <div className={cn("space-y-2", className)} data-testid="translatable-content">
      <p className={cn("whitespace-pre-wrap break-words", textClassName)}>
        {displayedText}
      </p>
      {normalizedText ? (
        <div className="flex flex-wrap items-center gap-2">
          {!translation ? (
            <Button
              disabled={isLoading}
              onClick={(event) => {
                event.stopPropagation();
                void translate();
              }}
              size="xs"
              type="button"
              variant="outline"
            >
              {isLoading ? (
                <LoaderCircle className="animate-spin motion-reduce:animate-none" />
              ) : (
                <Languages />
              )}
              {ui(isLoading ? "Translating..." : "Translate")}
            </Button>
          ) : (
            <Button
              onClick={(event) => {
                event.stopPropagation();
                setShowTranslation((value) => !value);
              }}
              size="xs"
              type="button"
              variant="outline"
            >
              <Languages />
              {ui(showTranslation ? "Show original" : "Show translation")}
            </Button>
          )}
          {translation && showTranslation ? (
            <span className="text-xs text-muted">
              {ui("Machine translation for review only")}
            </span>
          ) : null}
        </div>
      ) : null}
      {error ? (
        <div className="flex flex-wrap items-center gap-2" role="alert">
          <span className="text-xs text-destructive">
            {ui("Translation failed. The original content is still available.")}
          </span>
          <Button
            onClick={(event) => {
              event.stopPropagation();
              void translate();
            }}
            size="xs"
            type="button"
            variant="ghost"
          >
            <RotateCcw />
            {ui("Retry translation")}
          </Button>
        </div>
      ) : null}
    </div>
  );
}
