import type {
  NotificationResponse,
  NotificationType,
  UnreadCountResponse,
} from "../../types";
import { apiCacheTtl, cachedApiCall, invalidateApiCache } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

type NotificationListParams = {
  isRead?: boolean;
  limit?: number;
  page?: number;
  type?: NotificationType;
};

function withQuery(path: string, params: Record<string, boolean | number | string | undefined>) {
  const searchParams = new URLSearchParams();

  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") {
      searchParams.set(key, String(value));
    }
  }

  const query = searchParams.toString();

  return query ? `${path}?${query}` : path;
}

export function getNotifications(params: NotificationListParams = {}) {
  const path = withQuery(apiEndpoints.notifications.list, {
    isRead: params.isRead,
    limit: params.limit ?? 20,
    page: params.page ?? 0,
    type: params.type,
  });

  return cachedApiCall(`notifications:list:${path}`, apiCacheTtl.dynamic, () =>
    apiFetch<NotificationResponse[]>(path, {
      method: "GET",
    }),
  );
}

export function getUnreadNotificationCount() {
  return cachedApiCall("notifications:unread-count", apiCacheTtl.dynamic, () =>
    apiFetch<UnreadCountResponse>(apiEndpoints.notifications.unreadCount, {
      method: "GET",
    }),
  );
}

export async function markNotificationRead(notificationId: string) {
  const response = await apiFetch<NotificationResponse>(
    apiEndpoints.notifications.markRead(notificationId),
    {
      method: "PATCH",
    },
  );
  invalidateNotificationCache();
  return response;
}

export async function markAllNotificationsRead() {
  const response = await apiFetch<void>(apiEndpoints.notifications.markAllRead, {
    method: "PATCH",
  });
  invalidateNotificationCache();
  return response;
}

export async function deleteNotification(notificationId: string) {
  const response = await apiFetch<void>(apiEndpoints.notifications.byId(notificationId), {
    method: "DELETE",
  });
  invalidateNotificationCache();
  return response;
}

function invalidateNotificationCache() {
  invalidateApiCache("notifications:");
}
