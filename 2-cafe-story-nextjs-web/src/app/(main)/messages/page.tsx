import { MessageWorkspace } from "@/components/message/message-workspace";
import { mockConversations, mockMessageThread } from "@/mocks/messages";

export default function MessagesPage() {
  return (
    <MessageWorkspace
      conversations={mockConversations}
      thread={mockMessageThread}
    />
  );
}
