import type { Metadata } from "next";
import { TooltipProvider } from "@/components/ui/tooltip";
import "./../globals.css";

export const metadata: Metadata = {
  title: "Reviewer Dashboard | Cafe Story",
  description: "Reviewer performance, badges, payouts, and ranking.",
  icons: {
    icon: "/icons/cafestory-brand-icon.svg",
  },
};

export default function DashboardRootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="min-h-full">
        <TooltipProvider>{children}</TooltipProvider>
      </body>
    </html>
  );
}
