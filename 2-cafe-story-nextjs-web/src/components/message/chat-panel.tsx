"use client";

import { ChangeEvent, FormEvent, useEffect, useRef, useState } from "react";
import {
  ImageIcon,
  InfoIcon,
  PhoneIcon,
  SendIcon,
  SmileIcon,
  SquareIcon,
  XIcon,
} from "lucide-react";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";
import { ChatBubble } from "@/components/message/chat-bubble";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import type { ChatMessage, Conversation, SendMessageDraft } from "@/types/message";
import { useI18n } from "@/components/providers/locale-provider";

const EMOJI_OPTIONS = [
  "\u{1F600}",
  "\u{1F602}",
  "\u{1F60D}",
  "\u{2615}",
  "\u{1F44D}",
  "\u{2764}\u{FE0F}",
  "\u{1F525}",
  "\u{1F389}",
];

type ChatPanelProps = {
  canSend: boolean;
  conversation: Conversation | null;
  isLoading: boolean;
  loadErrorMessage: string | null;
  messages: ChatMessage[];
  onRetryMessages: () => void;
  onSendMessage: (draft: SendMessageDraft) => boolean;
  sendErrorMessage: string | null;
};

function getMessageSpacingClass(
  message: ChatMessage,
  previousMessage: ChatMessage | undefined,
) {
  if (!previousMessage) {
    return "";
  }

  if (!message.timestamp || !previousMessage.timestamp) {
    return "mt-2";
  }

  const distanceInMs = Math.abs(message.timestamp - previousMessage.timestamp);

  if (distanceInMs < 60_000) {
    return "mt-1";
  }

  if (distanceInMs > 120_000) {
    return "mt-4";
  }

  return "mt-2";
}

function ChatPanelLoadingState() {
  return (
    <section
      aria-busy="true"
      className="flex h-full min-h-0 flex-col bg-background"
    >
      <header className="flex h-[78px] shrink-0 items-center justify-between gap-4 border-b border-border bg-surface px-4 sm:h-[94px] sm:px-6">
        <div className="flex min-w-0 items-center gap-3">
          <Skeleton className="size-12 rounded-full sm:size-14" />
          <div className="min-w-0 space-y-2">
            <Skeleton className="h-4 w-36" />
            <Skeleton className="hidden h-3 w-24 sm:block" />
          </div>
        </div>
        <div className="flex shrink-0 gap-1 sm:gap-2">
          <Skeleton className="size-10 rounded-full" />
          <Skeleton className="size-10 rounded-full" />
          <Skeleton className="size-10 rounded-full" />
        </div>
      </header>

      <ScrollArea className="min-h-0 flex-1 px-4 py-5 sm:px-6 sm:py-6">
        <div className="flex flex-col gap-3">
          <div className="flex justify-start">
            <Skeleton className="h-16 w-[68%] max-w-[360px] rounded-2xl rounded-bl-md" />
          </div>
          <div className="flex justify-end">
            <Skeleton className="h-12 w-[56%] max-w-[300px] rounded-2xl rounded-br-md" />
          </div>
          <div className="flex justify-start">
            <Skeleton className="h-24 w-[74%] max-w-[420px] rounded-2xl rounded-bl-md" />
          </div>
          <div className="flex justify-end">
            <Skeleton className="h-14 w-[62%] max-w-[340px] rounded-2xl rounded-br-md" />
          </div>
        </div>
      </ScrollArea>

      <div className="flex shrink-0 items-center gap-2 border-t border-border bg-surface px-4 py-3 sm:gap-3 sm:px-6 sm:py-4">
        <Skeleton className="size-11 shrink-0 rounded-full" />
        <Skeleton className="size-11 shrink-0 rounded-full" />
        <Skeleton className="h-14 min-w-0 flex-1 rounded-full" />
        <Skeleton className="h-11 w-14 shrink-0 rounded-full" />
      </div>
    </section>
  );
}

export function ChatPanel({
  canSend,
  conversation,
  isLoading,
  loadErrorMessage,
  messages,
  onRetryMessages,
  onSendMessage,
  sendErrorMessage,
}: ChatPanelProps) {
  const { t } = useI18n();
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const messageEndRef = useRef<HTMLDivElement | null>(null);
  const [text, setText] = useState("");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [isEmojiOpen, setIsEmojiOpen] = useState(false);

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ block: "end" });
  }, [messages.length]);

  function clearSelectedFile() {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }

    setSelectedFile(null);
    setPreviewUrl(null);
  }

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;

    clearSelectedFile();

    if (file?.type.startsWith("image/")) {
      setSelectedFile(file);
      setPreviewUrl(URL.createObjectURL(file));
    }

    event.target.value = "";
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const trimmedText = text.trim();

    if (!canSend || (!trimmedText && !selectedFile)) {
      return;
    }

    const didCreateOptimisticMessage = onSendMessage({
      file: selectedFile,
      text: trimmedText,
    });

    if (!didCreateOptimisticMessage) {
      return;
    }

    setText("");
    clearSelectedFile();
    setIsEmojiOpen(false);
  }

  if (!conversation) {
    return (
      <section className="flex h-full min-h-0 items-center justify-center bg-background px-6 text-center">
        <div>
          <h2 className="text-xl font-black">{t("messages.emptyTitle")}</h2>
          <p className="mt-2 text-sm font-semibold text-muted">
            {t("messages.emptyDescription")}
          </p>
        </div>
      </section>
    );
  }

  if (isLoading) {
    return <ChatPanelLoadingState />;
  }

  return (
    <section className="flex h-full min-h-0 flex-col bg-background">
      <header className="flex h-[78px] shrink-0 items-center justify-between gap-4 border-b border-border bg-surface px-4 sm:h-[94px] sm:px-6">
        <div className="flex min-w-0 items-center gap-3">
          <Avatar className="size-12 sm:size-14">
            <AvatarImage alt="" src={conversation.avatarImage} />
            <AvatarFallback>
              {conversation.initials ?? conversation.name.slice(0, 1)}
            </AvatarFallback>
          </Avatar>
          <div className="min-w-0">
            <h2 className="truncate text-base font-black">
              {conversation.name}
            </h2>
            <p className="hidden truncate text-sm text-muted sm:block">
              @{conversation.username}
            </p>
          </div>
        </div>

        <div className="flex shrink-0 gap-1 sm:gap-2">
          {[
            { label: t("messages.action.call"), icon: PhoneIcon },
            { label: t("messages.action.openMedia"), icon: SquareIcon },
            { label: t("messages.action.details"), icon: InfoIcon },
          ].map(({ icon: Icon, label }) => (
            <Button
              aria-label={label}
              key={label}
              size="icon"
              type="button"
              variant="ghost"
            >
              <Icon />
            </Button>
          ))}
        </div>
      </header>

      <ScrollArea className="min-h-0 flex-1 px-4 py-5 sm:px-6 sm:py-6">
        {loadErrorMessage ? (
          <div className="mx-auto max-w-sm rounded-md border border-border bg-surface-muted px-4 py-3 text-center text-sm font-semibold text-muted">
            <p>{loadErrorMessage}</p>
            <Button
              className="mt-3"
              onClick={onRetryMessages}
              type="button"
              variant="secondary"
            >
              Retry
            </Button>
          </div>
        ) : messages.length === 0 ? (
          <div className="py-8 text-center text-sm font-semibold text-muted">
            No messages yet.
          </div>
        ) : (
          <div>
            {messages.map((message, index) => (
              <div
                className={getMessageSpacingClass(message, messages[index - 1])}
                key={message.id}
              >
                <ChatBubble
                  isAssistant={conversation?.isAssistant ?? false}
                  message={message}
                />
              </div>
            ))}
            <div ref={messageEndRef} />
          </div>
        )}
      </ScrollArea>

      {sendErrorMessage ? (
        <div className="shrink-0 border-t border-border bg-surface px-4 py-2 text-sm font-semibold text-destructive sm:px-6">
          {sendErrorMessage}
        </div>
      ) : null}

      {previewUrl ? (
        <div className="flex shrink-0 items-center gap-3 border-t border-border bg-surface px-4 py-3 sm:px-6">
          <img
            alt=""
            className="size-16 rounded-md object-cover"
            src={previewUrl}
          />
          <span className="min-w-0 flex-1 truncate text-sm font-semibold text-muted">
            {selectedFile?.name}
          </span>
          <Button
            aria-label={t("messages.removeImage")}
            onClick={clearSelectedFile}
            size="icon-sm"
            type="button"
            variant="ghost"
          >
            <XIcon />
          </Button>
        </div>
      ) : null}

      <form
        className="relative flex shrink-0 items-center gap-2 border-t border-border bg-surface px-4 py-3 sm:gap-3 sm:px-6 sm:py-4"
        onSubmit={handleSubmit}
      >
        {isEmojiOpen ? (
          <div className="absolute bottom-[76px] left-4 grid grid-cols-4 gap-1 rounded-md border border-border bg-surface p-2 shadow-lg sm:left-6">
            {EMOJI_OPTIONS.map((emoji) => (
              <button
                className="size-10 rounded-md text-lg transition hover:bg-surface-muted"
                key={emoji}
                onClick={() => setText((currentText) => `${currentText}${emoji}`)}
                type="button"
              >
                {emoji}
              </button>
            ))}
          </div>
        ) : null}
        <Button
          aria-label={t("messages.addEmoji")}
          className="shrink-0 rounded-full"
          onClick={() => setIsEmojiOpen((isOpen) => !isOpen)}
          size="icon-lg"
          type="button"
          variant="ghost"
        >
          <SmileIcon />
        </Button>
        <Button
          aria-label={t("messages.addImage")}
          className="shrink-0 rounded-full"
          onClick={() => fileInputRef.current?.click()}
          size="icon-lg"
          type="button"
          variant="ghost"
        >
          <ImageIcon />
        </Button>
        <Input
          accept="image/*"
          className="hidden"
          onChange={handleFileChange}
          ref={fileInputRef}
          type="file"
        />
        <Input
          className="h-14 min-w-0 flex-1 rounded-full bg-background px-5 text-sm placeholder:text-muted"
          disabled={!canSend}
          onChange={(event) => setText(event.target.value)}
          placeholder={
            canSend ? t("messages.inputPlaceholder") : t("messages.inputOpening")
          }
          type="text"
          value={text}
        />
        <Button
          aria-label={t("messages.send")}
          className="h-11 rounded-full py-6 text-sm font-black"
          disabled={!canSend || (!text.trim() && !selectedFile)}
          type="submit"
        >
          <SendIcon />
        </Button>
      </form>
    </section>
  );
}
