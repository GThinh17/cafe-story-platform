import {
  Avatar,
  AvatarBadge,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { Conversation } from "@/types/message";

type ConversationListProps = {
  conversations: Conversation[];
};

export function ConversationList({ conversations }: ConversationListProps) {
  return (
    <section className="hidden h-screen min-h-0 flex-col border-r border-border bg-surface lg:flex">
      <header className="shrink-0 px-6 pb-4 pt-12">
        <div className="flex items-center justify-between gap-3">
          <div className="min-w-0">
            <h1 className="truncate text-2xl font-black">gthinh_1704</h1>
          </div>
          <Button
            aria-label="New message"
            className="text-xl font-black"
            size="icon"
            type="button"
            variant="ghost"
          >
            +
          </Button>
        </div>

        <label className="mt-5 block">
          <span className="sr-only">Search friends</span>
          <Input
            className="h-12 rounded-full border-0 bg-background px-5 text-sm placeholder:text-muted focus:ring-2 focus:ring-primary/20"
            placeholder="Tim kiem"
            type="search"
          />
        </label>
      </header>

      <ScrollArea className="min-h-0 flex-1">
        <div className="flex items-center justify-between px-6 pb-3 pt-2">
          <h2 className="text-lg font-black">Tin nhan</h2>
          <Button className="text-sm font-black text-muted" type="button" variant="ghost">
            Tin nhan dang cho
          </Button>
        </div>

        {conversations.map((conversation) => (
          <button
            className={`flex w-full items-center gap-4 px-6 py-3 text-left transition ${
              conversation.active
                ? "bg-surface-muted"
                : "bg-surface hover:bg-background"
            }`}
            key={conversation.id}
            type="button"
          >
            <Avatar className="size-[68px]">
              <AvatarImage alt="" src={conversation.avatarImage} />
              <AvatarFallback>{conversation.name.slice(0, 1)}</AvatarFallback>
              {conversation.status === "Active now" ? (
                <AvatarBadge className="size-3.5 border-2 border-surface" />
              ) : null}
            </Avatar>

            <span className="min-w-0 flex-1">
              <span className="flex items-center justify-between gap-2">
                <span className="truncate text-base font-medium">
                  {conversation.name}
                </span>
                <span className="shrink-0 text-xs text-muted">
                  {conversation.time}
                </span>
              </span>
              <span className="mt-1 block truncate text-sm text-muted">
                {conversation.status}
              </span>
            </span>

            {conversation.unread ? (
              <span className="size-2.5 shrink-0 rounded-full bg-accent" />
            ) : null}
          </button>
        ))}
      </ScrollArea>
    </section>
  );
}
