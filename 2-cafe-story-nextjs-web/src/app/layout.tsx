import type { Metadata } from "next";
import { cookies } from "next/headers";
import { Toaster } from "sonner";
import { AppProviders } from "@/components/providers/app-providers";
import { LOCALE_HTML_LANG, LOCALE_STORAGE_KEY, normalizeLocale } from "@/lib/i18n";
import { getServerTranslator } from "@/lib/i18n/server";
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
  const cookieStore = await cookies();
  const locale = normalizeLocale(cookieStore.get(LOCALE_STORAGE_KEY)?.value);

  return (
    <html lang={LOCALE_HTML_LANG[locale]} className="h-full antialiased">
      {/* suppressHydrationWarning: browser extensions (e.g. ColorZilla) inject
          attributes like cz-shortcut-listen into <body> before React hydrates. */}
      <body className="min-h-full" suppressHydrationWarning>
        <AppProviders initialLocale={locale}>{children}</AppProviders>
        <Toaster position="top-right" richColors closeButton />
      </body>
    </html>
  );
}
