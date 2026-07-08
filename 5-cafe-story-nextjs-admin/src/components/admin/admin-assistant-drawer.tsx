"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { BotIcon, CheckCircle2Icon, MessageSquareTextIcon, SendIcon, XIcon } from "lucide-react";
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

  if (!user) return null;

  return (
    <>
      <Button
        type="button"
        className="fixed bottom-5 right-5 z-50 h-11 px-4 shadow-[0_8px_18px_rgba(15,118,110,0.2)]"
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
            className="absolute inset-0 bg-black/25"
            onClick={() => setOpen(false)}
          />
          <aside className="absolute bottom-0 right-0 top-0 flex w-full max-w-5xl flex-col border-l border-border bg-surface shadow-[0_20px_70px_rgba(39,19,16,0.22)] sm:right-4 sm:top-4 sm:bottom-4 sm:rounded-md sm:border">
            <header className="flex items-center justify-between border-b border-border px-4 py-3">
              <div className="flex items-center gap-3">
                <div className="grid size-9 place-items-center rounded-md bg-primary/10 text-primary">
                  <BotIcon className="size-4" />
                </div>
                <div>
                  <h2 className="text-sm font-bold text-espresso">Admin assistant</h2>
                  <p className="text-xs font-medium text-muted">
                    Core ops support with tool-audited answers.
                  </p>
                </div>
              </div>
              <Button type="button" variant="ghost" size="icon-sm" onClick={() => setOpen(false)}>
                <XIcon className="size-4" />
              </Button>
            </header>

            <div className="grid min-h-0 flex-1 grid-cols-1 lg:grid-cols-[260px_1fr]">
              <div className="hidden border-r border-border bg-background/60 p-3 lg:block">
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
                <div className="space-y-2">
                  {conversations.map((conversation) => (
                    <button
                      type="button"
                      key={conversation.id}
                      className={cn(
                        "w-full rounded-md border px-3 py-2 text-left transition",
                        activeConversation?.id === conversation.id
                          ? "border-primary bg-primary/10"
                          : "border-border bg-surface hover:bg-surface-muted",
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

              <div className="flex min-h-0 flex-col">
                <main className="min-h-0 flex-1 overflow-y-auto px-4 py-4">
                  {messages.length === 0 ? (
                    <div className="mx-auto grid min-h-72 max-w-lg place-items-center text-center">
                      <div>
                        <div className="mx-auto mb-4 grid size-11 place-items-center rounded-md bg-primary/10 text-primary">
                          <BotIcon className="size-5" />
                        </div>
                        <h3 className="text-base font-bold text-espresso">Ask about admin operations</h3>
                        <p className="mt-2 text-sm text-muted">
                          Try report queues, suspicious users, blog status, comments, or cafe pages.
                        </p>
                      </div>
                    </div>
                  ) : null}

                  <div className="space-y-3">
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
                            "max-w-[78ch] rounded-md border px-3 py-2 text-sm leading-6",
                            message.role === "USER"
                              ? "border-primary bg-primary text-primary-foreground"
                              : "border-border bg-background text-foreground",
                          )}
                        >
                          <p className="whitespace-pre-wrap">{message.content}</p>
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
                      <div className="rounded-md border border-border bg-background px-3 py-2 text-sm text-muted">
                        Assistant is checking tools and policies...
                      </div>
                    ) : null}
                    <div ref={messagesEndRef} />
                  </div>
                </main>

                {draftAction ? (
                  <div className="border-t border-border bg-background px-4 py-3">
                    <div className="rounded-md border border-line-soft bg-surface p-3">
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div>
                          <p className="text-xs font-bold uppercase text-muted">Draft action</p>
                          <p className="text-sm font-bold text-espresso">{draftAction.actionType}</p>
                          <p className="mt-1 text-sm text-muted">
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
                  <div className="border-t border-border px-4 py-2 text-xs text-muted">
                    Tool-audited response. Citations:{" "}
                    {Array.isArray(latestAssistantMeta.citations)
                      ? latestAssistantMeta.citations.length
                      : 0}
                  </div>
                ) : null}

                {error ? (
                  <div className="mx-4 mb-3 rounded-md border border-accent/30 bg-accent/10 px-3 py-2 text-sm font-medium text-accent">
                    {error}
                  </div>
                ) : null}

                <footer className="border-t border-border p-4">
                  <div className="flex gap-2">
                    <Textarea
                      value={input}
                      onChange={(event) => setInput(event.target.value)}
                      placeholder="Ask about reports, moderation, users, blogs, comments, or cafe pages..."
                      className="min-h-11 resize-none"
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
