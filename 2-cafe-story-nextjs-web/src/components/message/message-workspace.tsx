import { ChatPanel } from "@/components/message/chat-panel";
import { ConversationList } from "@/components/message/conversation-list";
import type { Conversation, MessageThread } from "@/types/message";

type MessageWorkspaceProps = {
  conversations: Conversation[];
  thread: MessageThread;
};

export function MessageWorkspace({
  conversations,
  thread,
}: MessageWorkspaceProps) {
  return (
    <main className="-ml-8 h-screen w-[calc(100vw-64px)] max-w-none overflow-hidden bg-background sm:-ml-14 sm:w-[calc(100vw-72px)] xl:-ml-[248px]">
      <section className="grid h-screen min-h-0 w-full overflow-hidden border-l border-r border-border bg-surface lg:grid-cols-[360px_minmax(0,1fr)]">
        <ConversationList conversations={conversations} />
        <ChatPanel thread={thread} />
      </section>
    </main>
  );
}
