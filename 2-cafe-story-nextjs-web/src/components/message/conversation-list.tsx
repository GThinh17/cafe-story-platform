import {
  Avatar,
  AvatarBadge,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import type { Conversation } from "@/types/message";

type ConversationListProps = {
  activeConversationId: string | null;
  conversations: Conversation[];
  currentUsername: string;
  errorMessage: string | null;
  isLoading: boolean;
  onRetry: () => void;
  onSelectConversation: (conversation: Conversation) => void;
};

function ConversationListSkeleton() {
  return (
    <div aria-busy="true" className="flex flex-col">
      {Array.from({ length: 6 }).map((_, index) => (
        <div className="flex w-full items-center gap-4 px-6 py-3" key={index}>
          <Skeleton className="size-[68px] shrink-0 rounded-full" />
          <div className="min-w-0 flex-1 space-y-2">
            <div className="flex items-center justify-between gap-2">
              <Skeleton className="h-4 w-32" />
              <Skeleton className="h-3 w-10 shrink-0" />
            </div>
            <Skeleton className="h-3 w-44 max-w-full" />
          </div>
        </div>
      ))}
    </div>
  );
}

export function ConversationList({
  activeConversationId,
  conversations,
  currentUsername,
  errorMessage,
  isLoading,
  onRetry,
  onSelectConversation,
}: ConversationListProps) {
  return (
    <section className="hidden h-screen min-h-0 flex-col border-r border-border bg-surface lg:flex">
      <header className="shrink-0 px-6 pb-4 pt-12">
        <div className="flex items-center justify-between gap-3">
          <div className="min-w-0">
            <h1 className="truncate text-2xl font-black">{currentUsername}</h1>
          </div>
        </div>

        <label className="mt-5 block">
          <span className="sr-only">Search friends</span>
          <Input
            className="h-12 rounded-full border-0 bg-background px-5 text-sm placeholder:text-muted focus:ring-2 focus:ring-primary/20"
            placeholder="Search messages"
            type="search"
          />
        </label>
      </header>

      <ScrollArea className="min-h-0 flex-1">
        <div className="flex items-center justify-between px-6 pb-3 pt-2">
          <h2 className="text-lg font-black">Messages</h2>
          {errorMessage ? (
            <Button
              className="text-sm font-black text-muted"
              onClick={onRetry}
              type="button"
              variant="ghost"
            >
              Retry
            </Button>
          ) : null}
        </div>

        {isLoading ? (
          <ConversationListSkeleton />
        ) : errorMessage ? (
          <div className="mx-6 rounded-md border border-border bg-surface-muted px-4 py-3 text-sm font-semibold text-muted">
            {errorMessage}
          </div>
        ) : conversations.length === 0 ? (
          <div className="px-6 py-6 text-sm font-semibold text-muted">
            No conversations yet.
          </div>
        ) : (
          conversations.map((conversation) => {
            const isActive = conversation.id === activeConversationId;
            const isCreating = conversation.localStatus === "creating";
            const isError = conversation.localStatus === "error";

            return (
              <button
                className={`flex w-full items-center gap-4 px-6 py-3 text-left transition ${
                  isActive
                    ? "bg-surface-muted"
                    : "bg-surface hover:bg-background"
                }`}
                key={conversation.id}
                onClick={() => onSelectConversation(conversation)}
                type="button"
              >
                <Avatar
                  className={`size-[68px] ${
                    conversation.isAssistant
                      ? "bg-primary text-primary-foreground"
                      : ""
                  }`}
                >
                  <AvatarImage alt="" src={conversation.avatarImage} />
                  <AvatarFallback
                    className={
                      conversation.isAssistant
                        ? "bg-primary text-primary-foreground font-semibold"
                        : undefined
                    }
                  >
                    {conversation.initials ?? conversation.name.slice(0, 1)}
                  </AvatarFallback>
                  {conversation.isAssistant ||
                  conversation.serverId ||
                  conversation.isTemporary ? (
                    <AvatarBadge
                      className={`size-3.5 border-2 border-surface ${
                        conversation.isAssistant ? "bg-primary" : ""
                      }`}
                    />
                  ) : null}
                </Avatar>

                <span className="min-w-0 flex-1">
                  <span className="flex items-center justify-between gap-2">
                    <span className="flex min-w-0 items-center gap-1.5">
                      <span className="truncate text-base font-medium">
                        {conversation.name}
                      </span>
                      {conversation.isAssistant ? (
                        <span className="shrink-0 rounded-md bg-surface-muted px-1.5 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-primary-strong">
                          AI
                        </span>
                      ) : null}
                    </span>
                    <span className="shrink-0 text-xs text-muted">
                      {conversation.time}
                    </span>
                  </span>
                  <span
                    className={`mt-1 block truncate text-sm ${
                      isError ? "text-destructive" : "text-muted"
                    }`}
                  >
                    {isCreating
                      ? "Opening..."
                      : isError
                        ? "Unable to open conversation."
                        : conversation.preview}
                  </span>
                </span>
              </button>
            );
          })
        )}
      </ScrollArea>
    </section>
  );
}
