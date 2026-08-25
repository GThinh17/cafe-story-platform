// Server-side proxy tới Python RAG service.
// Đọc access_token cookie (httpOnly, client không truy cập được) và forward
// bằng header Authorization: Bearer để Python route E fetch được user context.
// Plan §15.8: giữ token khỏi client, KHÔNG dùng NEXT_PUBLIC_ prefix.
import { cookies } from "next/headers";
import { NextResponse } from "next/server";

const AI_BASE_URL = process.env.AI_BASE_URL ?? "http://localhost:8036";
const ACCESS_TOKEN_COOKIE = "access_token";
const ASK_TIMEOUT_MS = 60_000;

export async function POST(request: Request) {
  let payload: unknown;
  try {
    payload = await request.json();
  } catch {
    return NextResponse.json({ message: "Invalid JSON body" }, { status: 400 });
  }

  const cookieStore = await cookies();
  const accessToken = cookieStore.get(ACCESS_TOKEN_COOKIE)?.value;

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };
  if (accessToken) {
    headers.Authorization = `Bearer ${accessToken}`;
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), ASK_TIMEOUT_MS);

  try {
    const upstream = await fetch(`${AI_BASE_URL}/api/ai/chat/ask`, {
      method: "POST",
      headers,
      body: JSON.stringify(payload),
      signal: controller.signal,
      cache: "no-store",
    });

    const text = await upstream.text();
    const contentType = upstream.headers.get("content-type") ?? "application/json";

    return new NextResponse(text, {
      status: upstream.status,
      headers: { "Content-Type": contentType, "Cache-Control": "no-store" },
    });
  } catch (error) {
    const isAbort = error instanceof Error && error.name === "AbortError";
    return NextResponse.json(
      {
        message: isAbort
          ? "AI service timed out. Please try again."
          : "Unable to reach AI service.",
      },
      { status: 502 },
    );
  } finally {
    clearTimeout(timeout);
  }
}
