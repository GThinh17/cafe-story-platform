import type { ApiEnvelope, ApiErrorPayload } from "../../types";
import { getApiBaseUrl } from "../../config";
import { localizeApiErrorMessage } from "../../features/i18n";
import { invalidateApiCache } from "./api-cache";

type ApiFetchOptions = Omit<RequestInit, "body"> & {
  body?: BodyInit | Record<string, unknown> | null;
};

let authAccessToken: string | null = null;

export class ApiError extends Error {
  payload: ApiErrorPayload | null;
  rawMessage: string | null;
  statusCode: number;

  constructor(message: string, statusCode: number, payload: ApiErrorPayload | null) {
    super(localizeApiErrorMessage(statusCode, message));
    this.name = "ApiError";
    this.payload = payload;
    this.rawMessage = message || null;
    this.statusCode = statusCode;
  }
}

export function setAuthAccessToken(token: string | null | undefined) {
  if ((token ?? null) !== authAccessToken) {
    invalidateApiCache();
  }
  authAccessToken = token ?? null;
}

export function clearAuthAccessToken() {
  invalidateApiCache();
  authAccessToken = null;
}

function buildUrl(path: string) {
  if (path.startsWith("http://") || path.startsWith("https://")) {
    return path;
  }

  const baseUrl = getApiBaseUrl().replace(/\/$/, "");
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

async function parseJson<T>(response: Response): Promise<T | null> {
  const text = await response.text();

  if (!text) {
    return null;
  }

  return JSON.parse(text) as T;
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

  if (authAccessToken && !requestHeaders.has("Authorization")) {
    requestHeaders.set("Authorization", `Bearer ${authAccessToken}`);
  }

  const requestBody: BodyInit | null | undefined = shouldSerializeBody
    ? JSON.stringify(body)
    : (body as BodyInit | null | undefined);

  let response: Response;

  try {
    response = await fetch(buildUrl(path), {
      credentials: "include",
      ...restOptions,
      body: requestBody,
      headers: requestHeaders,
    });
  } catch (requestError) {
    const rawMessage =
      requestError instanceof Error ? requestError.message : "Network request failed";
    throw new ApiError(rawMessage, 0, null);
  }

  let payload: ApiEnvelope<T> | ApiErrorPayload | null;

  try {
    payload = await parseJson<ApiEnvelope<T> | ApiErrorPayload>(response);
  } catch (parseError) {
    const rawMessage =
      parseError instanceof Error
        ? parseError.message
        : "API returned an invalid response";
    throw new ApiError(rawMessage, response.status, null);
  }

  if (!response.ok) {
    const message =
      payload && "message" in payload && payload.message
        ? payload.message
        : "API request failed";

    throw new ApiError(message, response.status, payload);
  }

  if (payload && "data" in payload) {
    return payload.data as T;
  }

  return payload as T;
}
