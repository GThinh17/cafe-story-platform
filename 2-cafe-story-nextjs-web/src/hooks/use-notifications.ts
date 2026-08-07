"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import type { Client, StompSubscription } from "@stomp/stompjs";
import { createStompClient } from "@/lib/api/websocket";
import {
  getNotifications,
  getUnreadCount,
  markAllNotificationsRead,
  markNotificationRead,
} from "@/lib/api/notifications";
import { useCurrentUser } from "@/hooks/use-current-user";
import { getUserById } from "@/lib/api/users";
import type { UserResponse } from "@/types/user";
import { useI18n } from "@/components/providers/locale-provider";
import type {
  NotificationResponse,
  NotificationSocketEvent,
  NotificationType,
} from "@/types/notification";

export type NotificationFilter = "ALL" | NotificationType;

export function useNotifications() {
  const { t } = useI18n();
  const { user } = useCurrentUser();
  const [notifications, setNotifications] = useState<NotificationResponse[]>(
    [],
  );
  const [unreadCount, setUnreadCount] = useState(0);
  const [activeFilter, setActiveFilterState] = useState<NotificationFilter>("ALL");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [actors, setActors] = useState<Record<string, UserResponse>>({});
  const actorsRef = useRef<Record<string, UserResponse>>({});

  useEffect(() => {
    actorsRef.current = actors;
  }, [actors]);

  const enrichActors = useCallback(async (ids: (string | null | undefined)[]) => {
    const uniqueIds = Array.from(
      new Set(ids.filter((id): id is string => !!id && !actorsRef.current[id])),
    );
    if (uniqueIds.length === 0) return;

    const results = await Promise.all(
      uniqueIds.map(async (id) => {
        try {
          return [id, await getUserById(id)] as const;
        } catch {
          return null;
        }
      }),
    );

    setActors((prev) => {
      const next = { ...prev };
      for (const result of results) {
        if (result) next[result[0]] = result[1];
      }
      return next;
    });
  }, []);

  const setActiveFilter = useCallback((filter: string) => {
    setActiveFilterState(filter as NotificationFilter);
  }, []);

  const stompClientRef = useRef<Client | null>(null);
  const stompSubscriptionRef = useRef<StompSubscription | null>(null);

  useEffect(() => {
    if (!user?.userId) return;

    setIsLoading(true);
    setError(null);

    Promise.all([getNotifications({ limit: 50 }), getUnreadCount()])
      .then(([list, countData]) => {
        setNotifications(list);
        setUnreadCount(countData.unreadCount);
        void enrichActors(list.flatMap((n) => [n.actorId, n.userId]));
      })
      .catch(() => setError(t("notifications.loadError")))
      .finally(() => setIsLoading(false));
  }, [user?.userId, enrichActors, t]);

  useEffect(() => {
    if (!user?.userId) return;

    function subscribe(client: Client) {
      // Reconnect sẽ gọi lại onConnect; huỷ subscription cũ để không nhận trùng frame.
      stompSubscriptionRef.current?.unsubscribe();
      stompSubscriptionRef.current = client.subscribe(
        "/user/queue/notifications",
        (frame) => {
          try {
            const event = JSON.parse(frame.body) as NotificationSocketEvent;

            if (event.type === "notification:new") {
              const incoming = event.data;
              setNotifications((prev) => {
                if (prev.some((n) => n.id === incoming.id)) return prev;
                return [incoming, ...prev];
              });
              setUnreadCount((c) => c + 1);
              void enrichActors([incoming.actorId, incoming.userId]);
            } else if (event.type === "notification:unread_count_updated") {
              setUnreadCount(event.data.unreadCount);
            }
          } catch {
            // ignore malformed frames
          }
        },
      );
    }

    const existingClient = stompClientRef.current;

    if (existingClient) {
      if (existingClient.connected) {
        subscribe(existingClient);
      }
    } else {
      const client = createStompClient();
      // Gán onConnect trước activate() để không phải monkey-patch chồng handler sau này.
      client.onConnect = () => subscribe(client);
      stompClientRef.current = client;
      client.activate();
    }

    return () => {
      stompSubscriptionRef.current?.unsubscribe();
      stompSubscriptionRef.current = null;
    };
  }, [user?.userId, enrichActors]);

  useEffect(() => {
    return () => {
      stompSubscriptionRef.current?.unsubscribe();
      stompClientRef.current?.deactivate();
    };
  }, []);

  const markRead = useCallback(async (id: string) => {
    await markNotificationRead(id);
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)),
    );
    setUnreadCount((c) => Math.max(0, c - 1));
  }, []);

  const markAllRead = useCallback(async () => {
    await markAllNotificationsRead();
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    setUnreadCount(0);
  }, []);

  return {
    notifications,
    unreadCount,
    activeFilter,
    isLoading,
    error,
    actors,
    setActiveFilter,
    markRead,
    markAllRead,
  };
}
