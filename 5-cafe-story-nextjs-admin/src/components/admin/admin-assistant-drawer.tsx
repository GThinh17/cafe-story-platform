"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import {
  BotIcon,
  CheckCircle2Icon,
  MessageSquareTextIcon,
  SendIcon,
  SparklesIcon,
  XIcon,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import {
  createAssistantConversation,
  executeAssistantDraftAction,
  getAssistantConversations,
  getAssistantMessages,
  streamAssistantMessage,
} from "@/lib/api/admin";
import { cn } from "@/lib/utils";
import { useCurrentUser } from "@/hooks/use-current-user";
import type {
  AdminAssistantConversation,
  AdminAssistantDraftAction,
  AdminAssistantMessage,
} from "@/types/admin";

function formatTime(value: string | null | undefined) {
  if (!value) return "";
  return new Intl.DateTimeFormat("en", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}

function asText(value: unknown) {
  if (typeof value === "string") return value;
  if (value === null || value === undefined) return "";
  return JSON.stringify(value);
}

function pageContext() {
  if (typeof window === "undefined") return {};
  return {
    route: window.location.pathname,
    query: window.location.search,
  };
}

const assistantInputLabel = "Ask assistant message";
const assistantInputPlaceholder = "Ask in Vietnamese or English about reports and moderation...";
const assistantPromptChips = [
  "Cho tôi xem các report đang review",
  "Báo cáo bài viết lừa đảo nào cần xử lý?",
  "Tóm tắt moderation queue hiện tại",
  "What admin actions are allowed for reports?",
];

function isSafeLink(url: string) {
  try {
    const parsed = new URL(url);
    return parsed.protocol === "http:" || parsed.protocol === "https:";
  } catch {
    return false;
  }
}

function renderInlineMarkdown(text: string, keyPrefix: string) {
  const nodes: React.ReactNode[] = [];
  const tokenPattern = /(!?\[[^\]]*]\([^)]*\)|\*\*[^*]+\*\*)/g;
  let lastIndex = 0;
  let tokenIndex = 0;

  for (const match of text.matchAll(tokenPattern)) {
    if (match.index === undefined) continue;
    const token = match[0];
    if (match.index > lastIndex) {
      nodes.push(text.slice(lastIndex, match.index));
    }

    const imageMatch = token.match(/^!\[([^\]]*)]\(([^)]*)\)$/);
    const linkMatch = token.match(/^\[([^\]]*)]\(([^)]*)\)$/);
    const boldMatch = token.match(/^\*\*([^*]+)\*\*$/);
    const key = `${keyPrefix}-inline-${tokenIndex}`;

    if (imageMatch) {
      const [, alt, url] = imageMatch;
      nodes.push(
        isSafeLink(url) ? (
          <a
            key={key}
            href={url}
            target="_blank"
            rel="noreferrer"
            className="inline-flex max-w-full items-center rounded-sm bg-primary/10 px-1.5 py-0.5 text-xs font-semibold text-primary underline-offset-2 hover:underline"
          >
            {alt || "Image reference"}
          </a>
        ) : (
          <span key={key} className="text-muted">
            {alt || "Image reference"}
          </span>
        ),
      );
    } else if (linkMatch) {
      const [, label, url] = linkMatch;
      nodes.push(
        isSafeLink(url) ? (
          <a
            key={key}
            href={url}
            target="_blank"
            rel="noreferrer"
            className="font-medium text-primary underline underline-offset-2"
          >
            {label || "Link"}
          </a>
        ) : (
          <span key={key}>{label || "Link"}</span>
        ),
      );
    } else if (boldMatch) {
      nodes.push(
        <strong key={key} className="font-semibold text-espresso">
          {boldMatch[1]}
        </strong>,
      );
    } else {
      nodes.push(token);
    }

    lastIndex = match.index + token.length;
    tokenIndex += 1;
  }

  if (lastIndex < text.length) {
    nodes.push(text.slice(lastIndex));
  }

  return nodes;
}

function renderMessageContent(message: AdminAssistantMessage) {
  const lines = message.content.split(/\r?\n/);

  if (message.role === "USER") {
    return (
      <p className="whitespace-pre-wrap break-words [overflow-wrap:anywhere]">{message.content}</p>
    );
  }

  return (
    <div className="space-y-2">
      {lines.map((rawLine, index) => {
        const line = rawLine.trim();
        const key = `${message.id}-${index}`;

        if (!line) {
          return <div key={key} className="h-1" />;
        }

        const heading = line.match(/^#{1,4}\s+(.+)$/);
        if (heading) {
          return (
            <p key={key} className="whitespace-pre-wrap break-words font-bold text-espresso [overflow-wrap:anywhere]">
              {renderInlineMarkdown(heading[1], key)}
            </p>
          );
        }

        const bullet = line.match(/^[-*]\s+(.+)$/);
        if (bullet) {
          return (
            <p key={key} className="flex gap-2 whitespace-pre-wrap break-words [overflow-wrap:anywhere]">
              <span className="mt-[0.65em] size-1.5 shrink-0 rounded-full bg-primary" />
              <span className="min-w-0">{renderInlineMarkdown(bullet[1], key)}</span>
            </p>
          );
        }

        const numbered = line.match(/^(\d+)\.\s+(.+)$/);
        if (numbered) {
          return (
            <p key={key} className="flex gap-2 whitespace-pre-wrap break-words [overflow-wrap:anywhere]">
              <span className="shrink-0 font-semibold text-primary">{numbered[1]}.</span>
              <span className="min-w-0">{renderInlineMarkdown(numbered[2], key)}</span>
            </p>
          );
        }

        return (
          <p key={key} className="whitespace-pre-wrap break-words [overflow-wrap:anywhere]">
            {renderInlineMarkdown(line, key)}
          </p>
        );
      })}
    </div>
  );
}

function metadataArray(
  metadata: Record<string, unknown> | null,
  key: "citations" | "toolCalls",
) {
  const value = metadata?.[key];
  return Array.isArray(value) ? value.filter((item) => item && typeof item === "object") : [];
}

function metaString(value: unknown) {
  if (typeof value === "string" && value.trim()) return value;
  return null;
}

function toolCallName(value: unknown) {
  if (!value || typeof value !== "object") return "unknown_tool";
  return metaString((value as Record<string, unknown>).toolName) ?? "unknown_tool";
}

function maskedFieldCount(value: unknown) {
  if (!value || typeof value !== "object") return 0;
  const fields = (value as Record<string, unknown>).maskedFields;
  return Array.isArray(fields) ? fields.length : 0;
}

export function AdminAssistantDrawer() {
  const { user } = useCurrentUser();
  const [open, setOpen] = useState(false);
  const [conversations, setConversations] = useState<AdminAssistantConversation[]>([]);
  const [activeConversation, setActiveConversation] =
    useState<AdminAssistantConversation | null>(null);
  const [messages, setMessages] = useState<AdminAssistantMessage[]>([]);
  const [draftAction, setDraftAction] = useState<AdminAssistantDraftAction | null>(null);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const canSend = input.trim().length > 0 && !loading;

  useEffect(() => {
    if (!open || !user) return;
    void loadConversations();
  }, [open, user]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages, loading, draftAction]);

  async function loadConversations() {
    try {
      const page = await getAssistantConversations({ page: 0, size: 12 });
      setConversations(page.content);
      if (!activeConversation && page.content[0]) {
        await selectConversation(page.content[0]);
      }
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Unable to load assistant chats.");
    }
  }

  async function selectConversation(conversation: AdminAssistantConversation) {
    setActiveConversation(conversation);
    setDraftAction(null);
    try {
      const page = await getAssistantMessages(conversation.id, { page: 0, size: 80 });
      setMessages([...page.content].reverse());
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Unable to load messages.");
    }
  }

  async function ensureConversation() {
    if (activeConversation) return activeConversation;
    const created = await createAssistantConversation({ title: "Admin assistant" });
    setConversations((current) => [created, ...current]);
    setActiveConversation(created);
    return created;
  }

  async function handleSend() {
    if (!canSend) return;
    const text = input.trim();
    setInput("");
    setError(null);
    setLoading(true);
    setDraftAction(null);

    try {
      const conversation = await ensureConversation();
      const optimistic: AdminAssistantMessage = {
        id: `local-${Date.now()}`,
        conversationId: conversation.id,
        role: "USER",
        content: text,
        metadata: null,
        createdAt: new Date().toISOString(),
      };
      setMessages((current) => [...current, optimistic]);

      await streamAssistantMessage(
        conversation.id,
        { message: text, pageContext: pageContext() },
        {
          onProgress: () => setLoading(true),
          onMessage: (response) => {
            setMessages((current) => [
              ...current.filter((message) => !message.id.startsWith("local-")),
              response.message,
            ]);
            setDraftAction(response.draftAction);
          },
          onError: (payload) => {
            setError(asText(payload.message) || "Admin assistant service unavailable.");
          },
          onDone: () => setLoading(false),
        },
      );
      await loadConversations();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Admin assistant service unavailable.");
    } finally {
      setLoading(false);
    }
  }

  async function handleExecuteDraft() {
    if (!draftAction || draftAction.status !== "PENDING") return;
    setError(null);
    setLoading(true);
    try {
      const executed = await executeAssistantDraftAction(draftAction.id);
      setDraftAction(executed);
      if (activeConversation) {
        await selectConversation(activeConversation);
      }
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Draft action failed.");
    } finally {
      setLoading(false);
    }
  }

  const latestAssistantMeta = useMemo(() => {
    const latest = [...messages].reverse().find((message) => message.role === "ASSISTANT");
    return latest?.metadata ?? null;
  }, [messages]);
  const latestCitations = useMemo(
    () => metadataArray(latestAssistantMeta, "citations"),
    [latestAssistantMeta],
  );
  const latestToolCalls = useMemo(
    () => metadataArray(latestAssistantMeta, "toolCalls"),
    [latestAssistantMeta],
  );

  if (!user) return null;

  return (
    <>
      <Button
        type="button"
        className="fixed bottom-5 right-5 z-50 h-11 px-4 shadow-[0_6px_14px_rgba(15,118,110,0.18)]"
        onClick={() => setOpen(true)}
      >
        <BotIcon className="size-4" />
        Assistant
      </Button>

      {open ? (
        <div className="fixed inset-0 z-[65]">
          <button
            type="button"
            aria-label="Close assistant overlay"
            className="absolute inset-0 bg-black/30"
            onClick={() => setOpen(false)}
          />
          <aside className="absolute inset-x-2 bottom-2 top-2 flex min-h-0 flex-col overflow-hidden rounded-md border border-border bg-surface shadow-[0_18px_42px_rgba(39,19,16,0.18)] sm:inset-x-auto sm:right-4 sm:w-[min(940px,calc(100vw-2rem))]">
            <header className="flex min-h-16 items-center justify-between gap-3 border-b border-border bg-surface px-4 py-3">
              <div className="flex min-w-0 items-center gap-3">
                <div className="grid size-9 place-items-center rounded-md bg-primary/10 text-primary">
                  <BotIcon className="size-4" />
                </div>
                <div className="min-w-0">
                  <h2 className="truncate text-sm font-bold text-espresso">Admin assistant</h2>
                  <p className="truncate text-xs font-medium text-muted">
                    Core ops support with tool-audited answers.
                  </p>
                </div>
              </div>
              <div className="flex shrink-0 items-center gap-2">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  className="lg:hidden"
                  onClick={async () => {
                    const created = await createAssistantConversation({ title: "Admin assistant" });
                    setConversations((current) => [created, ...current]);
                    await selectConversation(created);
                  }}
                >
                  <MessageSquareTextIcon className="size-4" />
                  New chat
                </Button>
                <Button type="button" variant="ghost" size="icon-sm" onClick={() => setOpen(false)}>
                  <XIcon className="size-4" />
                </Button>
              </div>
            </header>

            <div className="grid min-h-0 flex-1 grid-cols-1 bg-background/45 lg:grid-cols-[248px_minmax(0,1fr)]">
              <div className="hidden min-h-0 border-r border-border bg-surface p-3 lg:flex lg:flex-col">
                <Button
                  type="button"
                  variant="outline"
                  className="mb-3 w-full justify-start"
                  onClick={async () => {
                    const created = await createAssistantConversation({ title: "Admin assistant" });
                    setConversations((current) => [created, ...current]);
                    await selectConversation(created);
                  }}
                >
                  <MessageSquareTextIcon className="size-4" />
                  New chat
                </Button>
                <div className="min-h-0 flex-1 space-y-2 overflow-y-auto pr-1">
                  {conversations.map((conversation) => (
                    <button
                      type="button"
                      key={conversation.id}
                      className={cn(
                        "w-full rounded-md border px-3 py-2 text-left transition focus-visible:border-primary focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-primary/20",
                        activeConversation?.id === conversation.id
                          ? "border-primary bg-primary/10 text-primary"
                          : "border-border bg-background hover:bg-surface-muted",
                      )}
                      onClick={() => void selectConversation(conversation)}
                    >
                      <p className="truncate text-sm font-semibold text-foreground">
                        {conversation.title || "Assistant chat"}
                      </p>
                      <p className="text-xs text-muted">{formatTime(conversation.updatedAt)}</p>
                    </button>
                  ))}
                </div>
              </div>

              <div className="flex min-h-0 min-w-0 flex-col">
                <main className="min-h-0 flex-1 overflow-y-auto px-3 py-4 sm:px-5">
                  {messages.length === 0 ? (
                    <div className="mx-auto grid min-h-72 max-w-lg place-items-center text-center">
                      <div>
                        <div className="mx-auto mb-4 grid size-11 place-items-center rounded-md border border-primary/15 bg-primary/10 text-primary">
                          <SparklesIcon className="size-5" />
                        </div>
                        <h3 className="text-base font-bold text-espresso">Ask about admin operations</h3>
                        <p className="mt-2 text-sm text-muted">
                          Start with report queues, moderation risk, suspicious blogs, comments, or user/page signals.
                        </p>
                        <div className="mt-4 flex flex-wrap justify-center gap-2">
                          {assistantPromptChips.map((prompt) => (
                            <button
                              key={prompt}
                              type="button"
                              className="rounded-md border border-border bg-surface px-2.5 py-1.5 text-xs font-semibold text-foreground transition hover:border-primary hover:text-primary focus-visible:border-primary focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-primary/20"
                              onClick={() => setInput(prompt)}
                            >
                              {prompt}
                            </button>
                          ))}
                        </div>
                      </div>
                    </div>
                  ) : null}

                  <div className="mx-auto flex w-full max-w-3xl flex-col gap-3">
                    {messages.map((message) => (
                      <div
                        key={message.id}
                        className={cn(
                          "flex",
                          message.role === "USER" ? "justify-end" : "justify-start",
                        )}
                      >
                        <div
                          className={cn(
                            "min-w-0 max-w-[min(78ch,92%)] rounded-md border px-3 py-2 text-sm leading-6 shadow-none",
                            message.role === "USER"
                              ? "border-primary bg-primary text-primary-foreground"
                              : "border-border bg-surface text-foreground",
                          )}
                        >
                          {renderMessageContent(message)}
                          <p
                            className={cn(
                              "mt-1 text-[11px]",
                              message.role === "USER" ? "text-primary-foreground/75" : "text-muted",
                            )}
                          >
                            {formatTime(message.createdAt)}
                          </p>
                        </div>
                      </div>
                    ))}
                    {loading ? (
                      <div className="flex w-fit max-w-[92%] items-center gap-2 rounded-md border border-border bg-surface px-3 py-2 text-sm text-muted">
                        <span className="size-2 rounded-full bg-primary" />
                        <span>Assistant is checking tools and policies...</span>
                      </div>
                    ) : null}
                    <div ref={messagesEndRef} />
                  </div>
                </main>

                {draftAction ? (
                  <div className="border-t border-border bg-surface px-3 py-3 sm:px-5">
                    <div className="mx-auto max-w-3xl rounded-md border border-line-soft bg-background p-3">
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div className="min-w-0">
                          <p className="text-xs font-bold uppercase text-muted">Draft action</p>
                          <p className="break-words text-sm font-bold text-espresso">
                            {draftAction.actionType}
                          </p>
                          <p className="mt-1 break-words text-sm text-muted">
                            {draftAction.explanation || "Assistant proposed an admin action."}
                          </p>
                        </div>
                        <div className="flex items-center gap-2">
                          <span className="rounded-md bg-surface-muted px-2 py-1 text-xs font-bold text-muted">
                            {draftAction.status}
                          </span>
                          {draftAction.status === "PENDING" ? (
                            <Button
                              type="button"
                              size="sm"
                              onClick={() => void handleExecuteDraft()}
                              disabled={loading}
                            >
                              <CheckCircle2Icon className="size-4" />
                              Execute
                            </Button>
                          ) : null}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}

                {latestAssistantMeta ? (
                  <div className="border-t border-border bg-surface px-3 py-3 sm:px-5">
                    <div className="mx-auto max-w-3xl rounded-md border border-line-soft bg-background px-3 py-2">
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <div>
                          <p className="text-xs font-bold uppercase text-muted">Tool evidence</p>
                          <p className="text-xs text-muted">
                            {latestToolCalls.length} tool call{latestToolCalls.length === 1 ? "" : "s"} ·{" "}
                            {latestCitations.length} citation{latestCitations.length === 1 ? "" : "s"}
                          </p>
                        </div>
                        <div className="flex max-w-full flex-wrap justify-end gap-1.5">
                          {latestToolCalls.length ? (
                            latestToolCalls.map((toolCall, index) => (
                              <span
                                key={`${toolCallName(toolCall)}-${index}`}
                                className="rounded-md border border-border bg-surface px-2 py-1 text-[11px] font-semibold text-foreground"
                                title={`${maskedFieldCount(toolCall)} masked field(s)`}
                              >
                                {toolCallName(toolCall)}
                              </span>
                            ))
                          ) : (
                            <span className="rounded-md border border-border bg-surface px-2 py-1 text-[11px] font-semibold text-muted">
                              no_tool_metadata
                            </span>
                          )}
                        </div>
                      </div>
                      {latestCitations.length ? (
                        <div className="mt-2 flex flex-wrap gap-1.5">
                          {latestCitations.slice(0, 4).map((citation, index) => {
                            const record = citation as Record<string, unknown>;
                            const label =
                              metaString(record.toolName) ??
                              metaString(record.type) ??
                              `citation_${index + 1}`;
                            return (
                              <span
                                key={`${label}-${index}`}
                                className="rounded-sm bg-primary/10 px-2 py-1 text-[11px] font-semibold text-primary"
                              >
                                {label}
                              </span>
                            );
                          })}
                        </div>
                      ) : null}
                    </div>
                  </div>
                ) : null}

                {error ? (
                  <div className="border-t border-border bg-surface px-3 py-3 sm:px-5">
                    <div className="mx-auto max-w-3xl rounded-md border border-accent/30 bg-accent/10 px-3 py-2 text-sm font-medium text-accent">
                      {error}
                    </div>
                  </div>
                ) : null}

                <footer className="border-t border-border bg-surface p-3 sm:p-4">
                  <div className="mx-auto max-w-3xl">
                    <div className="mb-2 flex gap-2 overflow-x-auto pb-1">
                      {assistantPromptChips.map((prompt) => (
                        <button
                          key={prompt}
                          type="button"
                          className="shrink-0 rounded-md border border-border bg-background px-2.5 py-1.5 text-xs font-semibold text-muted transition hover:border-primary hover:text-primary focus-visible:border-primary focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-primary/20"
                          onClick={() => setInput(prompt)}
                        >
                          {prompt}
                        </button>
                      ))}
                    </div>
                  </div>
                  <div className="mx-auto flex max-w-3xl items-end gap-2">
                    <Textarea
                      value={input}
                      onChange={(event) => setInput(event.target.value)}
                      aria-label={assistantInputLabel}
                      placeholder={assistantInputPlaceholder}
                      rows={1}
                      className="max-h-32 min-h-12 resize-none bg-background py-3"
                      onKeyDown={(event) => {
                        if (event.key === "Enter" && !event.shiftKey) {
                          event.preventDefault();
                          void handleSend();
                        }
                      }}
                    />
                    <Button
                      type="button"
                      size="icon"
                      className="size-12"
                      disabled={!canSend}
                      onClick={() => void handleSend()}
                      aria-label="Send assistant message"
                    >
                      <SendIcon className="size-4" />
                    </Button>
                  </div>
                </footer>
              </div>
            </div>
          </aside>
        </div>
      ) : null}
    </>
  );
}
