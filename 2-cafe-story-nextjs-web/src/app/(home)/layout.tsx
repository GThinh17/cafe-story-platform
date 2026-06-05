import type { Metadata } from "next";
import { MainAppShell } from "@/components/layout/main-app-shell";

export const metadata: Metadata = {
  title: "Cafe Story",
  description: "Discover cafes, reviews, and coffee stories.",
};

export default function HomeLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return <MainAppShell>{children}</MainAppShell>;
}
