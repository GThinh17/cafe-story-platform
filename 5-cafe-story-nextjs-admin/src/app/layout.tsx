import type { Metadata } from "next";
import { AppProviders } from "@/components/providers/app-providers";
import { LOCALE_TAGS } from "@/features/i18n";
import {
  getServerLocaleState,
  getServerTranslator,
} from "@/features/i18n/server";
import "./globals.css";

export async function generateMetadata(): Promise<Metadata> {
  const t = await getServerTranslator();
  return {
    title: t("meta.app.title"),
    description: t("meta.app.description"),
    icons: {
      icon: "/icons/cafestory-brand-icon.svg",
    },
  };
}

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const localeState = await getServerLocaleState();

  return (
    <html lang={LOCALE_TAGS[localeState.locale]} className="h-full antialiased">
      <body className="min-h-full" suppressHydrationWarning>
        <AppProviders initialLocaleState={localeState}>{children}</AppProviders>
      </body>
    </html>
  );
}
