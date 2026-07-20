import type { Metadata } from "next";
import { Toaster } from "sonner";
import { AppProviders } from "@/components/providers/app-providers";
import "./globals.css";

export const metadata: Metadata = {
  title: "Cafe Story",
  description: "Discover cafes, reviews, and coffee stories.",
  icons: {
    icon: "/icons/cafestory-brand-icon.svg",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased">
      {/* suppressHydrationWarning: browser extensions (e.g. ColorZilla) inject
          attributes like cz-shortcut-listen into <body> before React hydrates. */}
      <body className="min-h-full" suppressHydrationWarning>
        <AppProviders>{children}</AppProviders>
        <Toaster position="top-right" richColors closeButton />
      </body>
    </html>
  );
}
