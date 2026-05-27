# Phase 08 - Notifications And Messages

## Mục tiêu

Kết nối notifications và chat/messages sau khi các domain chính đã ổn.

## Phạm vi frontend

- `2-cafe-story-nextjs-web/src/app/(main)/notifications/page.tsx`
- `2-cafe-story-nextjs-web/src/components/notification/activity-list.tsx`
- `2-cafe-story-nextjs-web/src/app/(main)/messages/page.tsx`
- `2-cafe-story-nextjs-web/src/components/message/message-workspace.tsx`
- `2-cafe-story-nextjs-web/src/components/message/conversation-list.tsx`
- `2-cafe-story-nextjs-web/src/components/message/chat-panel.tsx`
- `2-cafe-story-nextjs-web/src/components/message/message-dock.tsx`
- `2-cafe-story-nextjs-web/src/lib/api/notifications.ts`
- `2-cafe-story-nextjs-web/src/lib/api/chat.ts`
- `2-cafe-story-nextjs-web/src/types/message.ts`
- `2-cafe-story-nextjs-web/src/features/notifications/`
- `2-cafe-story-nextjs-web/src/features/chat/`

## Backend liên quan

Notifications:

- `GET /notifications`
- `GET /notifications/unread-count`
- `PATCH /notifications/{id}/read`
- `PATCH /notifications/read-all`
- `DELETE /notifications/{id}`

Chat:

- `POST /api/chat/conversations/direct`
- `POST /api/chat/conversations/group`
- `GET /api/chat/conversations`
- `GET /api/chat/conversations/{conversationId}/messages`
- `POST /api/chat/conversations/{conversationId}/messages`
- `POST /api/chat/conversations/{conversationId}/members`
- `DELETE /api/chat/conversations/{conversationId}/members/{memberUserId}`
- `POST /api/chat/conversations/{conversationId}/leave`
- `PATCH /api/chat/conversations/{conversationId}/group`

## Việc cần làm

1. Kiểm tra `NotificationController` hiện dùng base path `/notifications`, không có `/api`.
   - Nếu frontend convention muốn toàn bộ backend API dưới `/api`, cân nhắc chỉnh backend sang `/api/notifications`.
   - Nếu giữ nguyên, khai báo endpoint đúng trong `apiEndpoints.notifications`.
2. Tạo `src/lib/api/notifications.ts`:
   - `getNotifications()`;
   - `getUnreadNotificationCount()`;
   - `markNotificationAsRead(id)`;
   - `markAllNotificationsAsRead()`;
   - `deleteNotification(id)`.
3. Tạo `src/lib/api/chat.ts`:
   - `getConversations()`;
   - `getConversationMessages(conversationId)`;
   - `sendMessage(conversationId, request)`;
   - các action group/member sau.
4. Làm notifications trước chat vì đơn giản hơn.
5. Chat nên làm REST list/message trước, realtime/WebSocket để sau.

## Rủi ro cần kiểm tra

- Realtime chat có thể cần socket auth/session khác REST.
- Notification base path khác chuẩn `/api`.
- UI mock message có thể khác nhiều so với DTO backend.

## Tiêu chí hoàn thành

- `/notifications` hiển thị notification thật.
- Mark read/read all/delete hoạt động.
- `/messages` hiển thị conversation thật.
- Mở conversation load messages thật.
- Send message lưu backend và hiển thị lại sau reload.
- `npm run typecheck` pass.

