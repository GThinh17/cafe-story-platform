import { apiEndpoints } from "@/lib/api/endpoints";
import type { AskAssistantRequest, AskAssistantResponse } from "@/types/ai-chat";

const AI_CHAT_TIMEOUT_MS = 60_000;

export class AiChatError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "AiChatError";
    this.status = status;
  }
}

// Gọi Next.js API proxy (KHÔNG gọi trực tiếp Python).
// Proxy tự đọc httpOnly cookie và forward Authorization: Bearer.
export async function askAssistant(
  request: AskAssistantRequest,
): Promise<AskAssistantResponse> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), AI_CHAT_TIMEOUT_MS);

  try {
    const response = await fetch(apiEndpoints.aiChat.ask, {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: JSON.stringify(request),
      signal: controller.signal,
    });

    const text = await response.text();
    const payload = text ? (JSON.parse(text) as Record<string, unknown>) : null;

    if (!response.ok) {
      const message =
        (payload?.message as string) || "Unable to get answer from assistant.";
      throw new AiChatError(message, response.status);
    }

    const data = (payload?.data ?? payload) as Record<string, unknown> | null;
    return {
      answer: (data?.answer as string) ?? "",
      sources: normalizeSources(data?.sources),
      cached: Boolean(data?.cached),
    };
  } catch (error) {
    if (error instanceof AiChatError) {
      throw error;
    }
    if (error instanceof Error && error.name === "AbortError") {
      throw new AiChatError("Assistant is taking too long to reply.", 408);
    }
    throw new AiChatError(
      "Unable to reach assistant. Please try again.",
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
      return {
        sourceType: String(record.source_type ?? record.sourceType ?? ""),
        sourceId: String(record.source_id ?? record.sourceId ?? ""),
        title: String(record.title ?? ""),
        imageUrls: Array.isArray(record.image_urls ?? record.imageUrls)
          ? ((record.image_urls ?? record.imageUrls) as unknown[])
              .filter((url): url is string => typeof url === "string" && url.length > 0)
          : [],
      };
    })
    .filter((item): item is NonNullable<typeof item> => item !== null);
}
