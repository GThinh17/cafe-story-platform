import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  NotificationResponse,
  NotificationType,
  UnreadCountResponse,
} from "@/types/notification";

type GetNotificationsParams = {
  page?: number;
  limit?: number;
  isRead?: boolean;
  type?: NotificationType;
};

export function getNotifications(params: GetNotificationsParams = {}) {
  const query = new URLSearchParams();

  if (params.page !== undefined) query.set("page", String(params.page));
  if (params.limit !== undefined) query.set("limit", String(params.limit));
  if (params.isRead !== undefined) query.set("isRead", String(params.isRead));
  if (params.type) query.set("type", params.type);

  const qs = query.toString();

  return apiFetch<NotificationResponse[]>(
    `${apiEndpoints.notifications.list}${qs ? `?${qs}` : ""}`,
    { method: "GET" },
  );
}

export function getUnreadCount() {
  return apiFetch<UnreadCountResponse>(apiEndpoints.notifications.unreadCount, {
    method: "GET",
  });
}

export function markNotificationRead(id: string) {
  return apiFetch<void>(apiEndpoints.notifications.markRead(id), {
    method: "PATCH",
  });
}

export function markAllNotificationsRead() {
  return apiFetch<void>(apiEndpoints.notifications.markAllRead, {
    method: "PATCH",
  });
}
