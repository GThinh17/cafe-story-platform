import { InfoIcon, PhoneIcon, SmileIcon, SquareIcon } from "lucide-react";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { MessageThread } from "@/types/message";

type ChatPanelProps = {
  thread: MessageThread;
};

export function ChatPanel({ thread }: ChatPanelProps) {
  return (
    <section className="flex h-screen min-h-0 flex-col bg-background">
      <header className="flex h-[78px] shrink-0 items-center justify-between gap-4 border-b border-border bg-surface px-4 sm:h-[94px] sm:px-6">
        <div className="flex min-w-0 items-center gap-3">
          <Avatar className="size-12 sm:size-14">
            <AvatarImage alt="" src={thread.recipientAvatar} />
            <AvatarFallback>{thread.recipientName.slice(0, 1)}</AvatarFallback>
          </Avatar>
          <div className="min-w-0">
            <h2 className="truncate text-base font-black">
              {thread.recipientName}
            </h2>
            <p className="hidden truncate text-sm text-muted sm:block">
              {thread.recipientStatus}
            </p>
          </div>
        </div>

        <div className="flex shrink-0 gap-1 sm:gap-2">
          {[
            { label: "Call", icon: PhoneIcon },
            { label: "Open media", icon: SquareIcon },
            { label: "Conversation details", icon: InfoIcon },
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
        <div className="flex flex-col gap-4">
          {thread.messages.map((message) => (
            <div
              className={`flex ${
                message.author === "me" ? "justify-end" : "justify-start"
              }`}
              key={message.id}
            >
              <div
                className={`max-w-[82%] rounded-2xl px-4 py-2.5 text-sm leading-6 sm:max-w-[72%] ${
                  message.author === "me"
                    ? "rounded-br-md bg-primary text-primary-foreground"
                    : "rounded-bl-md bg-surface-muted text-foreground"
                }`}
              >
                <p>{message.body}</p>
                <p
                  className={`mt-1 text-[11px] ${
                    message.author === "me"
                      ? "text-primary-foreground/75"
                      : "text-muted"
                  }`}
                >
                  {message.time}
                </p>
              </div>
            </div>
          ))}
        </div>
      </ScrollArea>

      <form className="flex shrink-0 items-center gap-2 border-t border-border bg-surface px-4 py-3 sm:gap-3 sm:px-6 sm:py-4">
        <Button
          aria-label="Add attachment"
          className="shrink-0 rounded-full text-2xl font-black"
          size="icon-lg"
          type="button"
          variant="ghost"
        >
          <SmileIcon />
        </Button>
        <Input
          className="h-14 min-w-0 flex-1 rounded-full bg-background px-5 text-sm placeholder:text-muted"
          placeholder="Nhap tin..."
          type="text"
        />
        <Button className="h-11 rounded-full px-4 text-sm font-black sm:px-5" type="submit">
          Send
        </Button>
      </form>
    </section>
  );
}
