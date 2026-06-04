import type { MessageDockData } from "@/types/message";
import {MessageCircleIcon} from "lucide-react";
type MessageDockProps = {
  data: MessageDockData;
};

export function MessageDock({ data }: MessageDockProps) {
  return (
    <a
      className="flex h-14 w-fit items-center gap-3 rounded-full border border-border bg-surface px-4 shadow-xl transition hover:-translate-y-0.5 hover:shadow-2xl"
      href="/messages"
    >
      <span className="grid h-8 w-8 place-items-center rounded-full text-foreground">
        <MessageCircleIcon className="size-6" strokeWidth={2.2} />
      </span>
      <span className="min-w-0 text-base font-medium text-foreground">
        {data.title}
      </span>
      <span className="flex -space-x-2 pl-3">
        {data.contacts.map((contact) => (
          <span
            aria-label={contact.name}
            className="grid h-7 w-7 place-items-center rounded-full border-2 border-surface bg-surface-muted text-[9px] font-bold text-primary-strong"
            key={contact.name}
            title={contact.name}
          >
            {contact.initials}
          </span>
        ))}
      </span>
    </a>
  );
}
