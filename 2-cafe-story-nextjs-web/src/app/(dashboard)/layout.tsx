import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Reviewer Dashboard | Cafe Story",
  description: "Reviewer performance, badges, payouts, and ranking.",
};

export default function DashboardRootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return children;
}
