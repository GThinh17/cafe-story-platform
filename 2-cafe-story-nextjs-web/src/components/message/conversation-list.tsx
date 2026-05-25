import type { Conversation } from "@/types/message";

type ConversationListProps = {
  conversations: Conversation[];
};

export function ConversationList({ conversations }: ConversationListProps) {
  return (
    <section className="flex h-screen min-h-0 flex-col border-r border-border bg-surface">
      <header className="shrink-0 px-6 pb-4 pt-12">
        <div className="flex items-center justify-between gap-3">
          <div className="min-w-0">
            <h1 className="truncate text-2xl font-black">gthinh_1704</h1>
          </div>
          <button
            aria-label="New message"
            className="grid h-10 w-10 shrink-0 place-items-center rounded-md text-xl font-black transition hover:bg-surface-muted"
            type="button"
          >
            ↗
          </button>
        </div>

        <label className="mt-5 block">
          <span className="sr-only">Search friends</span>
          <input
            className="h-12 w-full rounded-full border-0 bg-background px-5 text-sm outline-none transition placeholder:text-muted focus:ring-2 focus:ring-primary/20"
            placeholder="Tìm kiếm"
            type="search"
          />
        </label>
      </header>

      <div className="min-h-0 flex-1 overflow-y-auto">
        <div className="flex items-center justify-between px-6 pb-3 pt-2">
          <h2 className="text-lg font-black">Tin nhắn</h2>
          <button className="text-sm font-black text-muted" type="button">
            Tin nhắn đang chờ
          </button>
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
            <span className="relative h-[68px] w-[68px] shrink-0 overflow-hidden rounded-full bg-background">
              <img
                alt=""
                className="h-full w-full object-cover"
                decoding="async"
                loading="lazy"
                src={conversation.avatarImage}
              />
              {conversation.status === "Active now" ? (
                <span className="absolute bottom-0 right-0 h-3.5 w-3.5 rounded-full border-2 border-surface bg-primary" />
              ) : null}
            </span>

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
              <span className="h-2.5 w-2.5 shrink-0 rounded-full bg-accent" />
            ) : null}
          </button>
        ))}
      </div>
    </section>
  );
}
