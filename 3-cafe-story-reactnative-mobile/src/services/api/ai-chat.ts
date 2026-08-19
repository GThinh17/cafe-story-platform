import { getAiBaseUrl } from "../../config";
import { getCurrentLocale, translate } from "../../features/i18n";
import type { AskAssistantRequest, AskAssistantResponse } from "../../types";
import { apiFetch, ApiError } from "./client";
import { apiEndpoints } from "./endpoints";

const AI_CHAT_TIMEOUT_MS = 60_000;

type RawAskAssistantResponse = {
  answer?: unknown;
  sources?: unknown;
  cached?: unknown;
};

export class AiChatError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "AiChatError";
    this.status = status;
  }
}

export async function askAssistant(
  request: AskAssistantRequest,
): Promise<AskAssistantResponse> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), AI_CHAT_TIMEOUT_MS);

  try {
    const response = await apiFetch<RawAskAssistantResponse>(
      apiEndpoints.aiChat.ask(getAiBaseUrl()),
      {
        body: request,
        method: "POST",
        signal: controller.signal,
      },
    );

    return {
      answer: String(response.answer ?? ""),
      sources: normalizeSources(response.sources),
      cached: Boolean(response.cached),
    };
  } catch (error) {
    if (error instanceof ApiError) {
      throw new AiChatError(
        error.message || "Unable to get answer from assistant.",
        error.statusCode,
      );
    }
    if (error instanceof Error && error.name === "AbortError") {
      throw new AiChatError(
        translate(getCurrentLocale(), "ai.error.timeout"),
        408,
      );
    }
    throw new AiChatError(
      getCurrentLocale() === "en" && error instanceof Error
        ? error.message
        : translate(getCurrentLocale(), "ai.error.unavailable"),
      0,
    );
  } finally {
    clearTimeout(timeout);
  }
}

function normalizeSources(raw: unknown): AskAssistantResponse["sources"] {
  if (!Array.isArray(raw)) {
    return [];
  }

  return raw
    .map((item) => {
      if (!item || typeof item !== "object") {
        return null;
      }

      const record = item as Record<string, unknown>;
      const imageUrls = record.image_urls ?? record.imageUrls;

      return {
        sourceType: String(record.source_type ?? record.sourceType ?? ""),
        sourceId: String(record.source_id ?? record.sourceId ?? ""),
        title: String(record.title ?? ""),
        imageUrls: Array.isArray(imageUrls)
          ? imageUrls.filter(
              (url): url is string =>
                typeof url === "string" && url.length > 0,
            )
          : [],
      };
    })
    .filter((item): item is NonNullable<typeof item> => item !== null);
}
