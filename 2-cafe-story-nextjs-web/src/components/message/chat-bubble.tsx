import { CircleAlertIcon, LoaderCircleIcon } from "lucide-react";
import type { ChatMessage } from "@/types/message";

type ChatBubbleProps = {
  message: ChatMessage;
};

export function ChatBubble({ message }: ChatBubbleProps) {
  const isMine = message.author === "me";
  const imageUrls = message.imageUrls ?? [];
  const hasText = Boolean(message.body?.trim());
  const isSending = message.localStatus === "sending";
  const isError = message.localStatus === "error";
  const sources = message.sources ?? [];
  // Assistant reply với localStatus="sending" và body rỗng → chấm loading.
  const showThinkingIndicator = !isMine && isSending && !hasText;

  return (
    <div className={`flex ${isMine ? "justify-end" : "justify-start"}`}>
      <div className="max-w-[82%] sm:max-w-[72%]">
        <div
          className={`overflow-hidden rounded-2xl text-sm leading-6 ${
            isMine
              ? "rounded-br-md bg-primary text-primary-foreground"
              : "rounded-bl-md bg-surface-muted text-foreground"
          }`}
        >
          {imageUrls.length > 0 ? (
            <div className="grid gap-1 p-1">
              {imageUrls.map((imageUrl) => (
                <img
                  alt=""
                  className="max-h-80 rounded-xl object-cover"
                  key={imageUrl}
                  src={imageUrl}
                />
              ))}
            </div>
          ) : null}
          {showThinkingIndicator ? (
            <p
              aria-label="Assistant is thinking"
              className="flex items-center gap-1 px-4 pt-3"
            >
              <span className="size-1.5 animate-bounce rounded-full bg-muted [animation-delay:-0.3s]" />
              <span className="size-1.5 animate-bounce rounded-full bg-muted [animation-delay:-0.15s]" />
              <span className="size-1.5 animate-bounce rounded-full bg-muted" />
            </p>
          ) : hasText ? (
            <p className="whitespace-pre-wrap break-words px-4 pt-2.5">
              {message.body}
            </p>
          ) : null}
          <p
            className={`flex min-h-4 items-center gap-1.5 px-4 pb-2 pt-1 text-[11px] ${
              isMine ? "justify-end text-primary-foreground/75" : "text-muted"
            }`}
          >
            <span>{message.time}</span>
            {isMine ? (
              <span className="grid size-3.5 place-items-center">
                {isSending ? (
                  <LoaderCircleIcon className="size-3 animate-spin" />
                ) : isError ? (
                  <CircleAlertIcon className="size-3" />
                ) : (
                  <span className="size-3" />
                )}
              </span>
            ) : null}
          </p>
        </div>
        {sources.length > 0 ? (
          <div className="mt-2 flex flex-wrap gap-1.5">
            {sources.map((source) => (
              <span
                className="rounded-md bg-surface-muted px-2.5 py-1 text-[11px] font-medium text-primary-strong"
                key={`${source.sourceType}:${source.sourceId}`}
              >
                {source.title}
              </span>
            ))}
          </div>
        ) : null}
        {isError && isMine ? (
          <p className="mt-1 text-right text-xs font-semibold text-destructive">
            Unable to send message.
          </p>
        ) : null}
        {isError && !isMine ? (
          <p className="mt-1 text-left text-xs font-semibold text-destructive">
            {message.body || "Assistant reply failed."}
          </p>
        ) : null}
      </div>
    </div>
  );
}
