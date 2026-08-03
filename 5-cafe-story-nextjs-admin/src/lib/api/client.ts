import type { ApiEnvelope, ApiErrorPayload } from "@/types/api";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type ApiFetchOptions = Omit<RequestInit, "body"> & {
  body?: BodyInit | Record<string, unknown> | null;
};

export class ApiError extends Error {
  statusCode: number;
  payload: ApiErrorPayload | null;
  code: string | null;
  correlationId: string | null;
  retryable: boolean | null;
  stage: string | null;

  constructor(message: string, statusCode: number, payload: ApiErrorPayload | null) {
    super(message);
    this.name = "ApiError";
    this.statusCode = statusCode;
    this.payload = payload;
    const structuredPayload: Record<string, unknown> = isRecord(payload)
      ? (payload as Record<string, unknown>)
      : {};
    this.code = typeof structuredPayload.code === "string" ? structuredPayload.code : null;
    this.correlationId =
      typeof structuredPayload.correlationId === "string"
        ? structuredPayload.correlationId
        : null;
    this.retryable =
      typeof structuredPayload.retryable === "boolean"
        ? structuredPayload.retryable
        : null;
    this.stage = typeof structuredPayload.stage === "string" ? structuredPayload.stage : null;
  }
}

export function buildApiUrl(path: string) {
  if (path.startsWith("http://") || path.startsWith("https://")) {
    return path;
  }

  const baseUrl = API_BASE_URL.replace(/\/$/, "");
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;

  return `${baseUrl}${normalizedPath}`;
}

function isJsonBody(body: ApiFetchOptions["body"]) {
  if (!body || typeof body !== "object") {
    return false;
  }

  const prototype = Object.getPrototypeOf(body);

  return prototype === Object.prototype || prototype === null;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

async function parseJson<T>(response: Response): Promise<T | null> {
  const text = await response.text();

  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text) as T;
  } catch {
    return null;
  }
}

function getErrorMessage(payload: ApiErrorPayload | null) {
  if (!payload) {
    return "API request failed";
  }

  if (isRecord(payload)) {
    const errorPayload = payload as Record<string, unknown>;
    const message = errorPayload.message;
    const detail = errorPayload.detail;
    const error = errorPayload.error;

    if (typeof message === "string" && message.trim()) {
      return message;
    }

    if (typeof detail === "string" && detail.trim()) {
      return detail;
    }

    if (typeof error === "string" && error.trim()) {
      return error;
    }
  }

  return "API request failed";
}

export async function apiFetch<T>(
  path: string,
  options: ApiFetchOptions = {},
): Promise<T> {
  const { body, headers, ...restOptions } = options;
  const shouldSerializeBody = isJsonBody(body);
  const requestHeaders = new Headers(headers);
  requestHeaders.set("Accept", "application/json");

  if (shouldSerializeBody && !requestHeaders.has("Content-Type")) {
    requestHeaders.set("Content-Type", "application/json");
  }

  const requestBody = shouldSerializeBody
    ? JSON.stringify(body)
    : (body as BodyInit | null | undefined);

  const response = await fetch(buildApiUrl(path), {
    cache: "no-store",
    credentials: "include",
    ...restOptions,
    body: requestBody,
    headers: requestHeaders,
  });

  const payload = await parseJson<ApiEnvelope<T> | ApiErrorPayload>(response);

  if (!response.ok) {
    throw new ApiError(getErrorMessage(payload), response.status, payload);
  }

  if (isRecord(payload) && "data" in payload) {
    return payload.data as T;
  }

  return payload as T;
}
