import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Cafe Story Auth",
  description: "Sign in or create a Cafe Story account.",
};

export default function AuthRootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return children;
}
