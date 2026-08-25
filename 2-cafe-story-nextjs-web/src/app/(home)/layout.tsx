import type { Metadata } from "next";
import { getServerTranslator } from "@/lib/i18n/server";
import { MainAppShell } from "@/components/layout/main-app-shell";

export async function generateMetadata(): Promise<Metadata> {
  const t = await getServerTranslator();

  return {
    title: t("meta.app.title"),
    description: t("meta.app.description"),
  };
}

export default function HomeLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return <MainAppShell>{children}</MainAppShell>;
}
