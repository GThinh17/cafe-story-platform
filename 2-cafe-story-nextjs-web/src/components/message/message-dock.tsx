import type { MessageDockData } from "@/types/message";
import {MessageCircleIcon} from "lucide-react";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";
type MessageDockProps = {
  data: MessageDockData;
};

export function MessageDock({ data }: MessageDockProps) {
  return (
    <a
      className="flex h-14 w-fit items-center gap-3 rounded-full border border-border bg-surface px-4"
      href="/messages"
    >
      <span className="grid h-8 w-8 place-items-center rounded-full text-foreground">
        <MessageCircleIcon className="size-6" strokeWidth={2.2} />
      </span>
      <span className="min-w-0 text-base font-medium text-foreground">
        Messages      </span>
      <span className="flex -space-x-2 pl-3">
        {data.contacts.map((contact) => (
          <Avatar
            className="size-7 border-2 border-surface bg-surface-muted"
            key={contact.id}
            title={contact.name}
          >
            <AvatarImage alt={contact.name} src={contact.avatarImage} />
            <AvatarFallback className="text-[9px] font-bold text-primary-strong">
              {contact.initials}
            </AvatarFallback>
          </Avatar>
        ))}
      </span>
    </a>
  );
}
