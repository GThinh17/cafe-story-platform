import { Client } from "@stomp/stompjs";

function getWsUrl(): string {
  const base = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
  return base.replace(/^https/, "wss").replace(/^http/, "ws") + "/ws/chat";
}

export function createStompClient(): Client {
  return new Client({
    brokerURL: getWsUrl(),
    // Cookies gửi tự động qua browser — không cần auth header thêm
    reconnectDelay: 5000,
  });
}
