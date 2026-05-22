import type { MessageThread } from "@/types/message";

type ChatPanelProps = {
  thread: MessageThread;
};

export function ChatPanel({ thread }: ChatPanelProps) {
  return (
    <section className="flex h-screen min-h-0 flex-col bg-background">
      <header className="flex h-[94px] shrink-0 items-center justify-between gap-4 border-b border-border bg-surface px-6">
        <div className="flex min-w-0 items-center gap-3">
          <img
            alt=""
            className="h-14 w-14 shrink-0 rounded-full object-cover"
            decoding="async"
            src={thread.recipientAvatar}
          />
          <div className="min-w-0">
            <h2 className="truncate text-base font-black">
              {thread.recipientName}
            </h2>
            <p className="truncate text-sm text-muted">
              {thread.recipientStatus}
            </p>
          </div>
        </div>

        <div className="flex shrink-0 gap-4">
          {["☎", "▢", "i"].map((label) => (
            <button
              aria-label={label === "i" ? "Conversation details" : "Call"}
              className="grid h-10 w-10 place-items-center rounded-md text-2xl font-black transition hover:bg-surface-muted"
              key={label}
              type="button"
            >
              {label}
            </button>
          ))}
        </div>
      </header>

      <div className="min-h-0 flex-1 space-y-4 overflow-y-auto px-6 py-6">
        {thread.messages.map((message) => (
          <div
            className={`flex ${
              message.author === "me" ? "justify-end" : "justify-start"
            }`}
            key={message.id}
          >
            <div
              className={`max-w-[72%] rounded-2xl px-4 py-2.5 text-sm leading-6 ${
                message.author === "me"
                  ? "rounded-br-md bg-[#4f46e5] text-white"
                  : "rounded-bl-md bg-surface-muted text-foreground"
              }`}
            >
              <p>{message.body}</p>
              <p
                className={`mt-1 text-[11px] ${
                  message.author === "me" ? "text-white/75" : "text-muted"
                }`}
              >
                {message.time}
              </p>
            </div>
          </div>
        ))}
      </div>

      <form className="flex shrink-0 items-center gap-3 border-t border-border bg-surface px-6 py-4">
        <button
          aria-label="Add attachment"
          className="grid h-11 w-11 shrink-0 place-items-center rounded-full text-2xl font-black transition hover:bg-surface-muted"
          type="button"
        >
          ☺
        </button>
        <input
          className="h-14 min-w-0 flex-1 rounded-full border border-border bg-background px-5 text-sm outline-none transition placeholder:text-muted focus:border-primary"
          placeholder="Nhắn tin..."
          type="text"
        />
        <button
          className="h-11 rounded-full bg-primary px-5 text-sm font-black text-white transition hover:bg-primary-strong"
          type="submit"
        >
          Send
        </button>
      </form>
    </section>
  );
}
