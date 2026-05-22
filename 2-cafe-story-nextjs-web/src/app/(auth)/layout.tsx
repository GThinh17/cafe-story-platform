import type { Metadata } from "next";
import "./../globals.css";

export const metadata: Metadata = {
  title: "Cafe Story Auth",
  description: "Sign in or create a Cafe Story account.",
  icons: {
    icon: "/icons/cafestory-brand-icon.svg",
  },
};

export default function AuthRootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="min-h-full">{children}</body>
    </html>
  );
}
