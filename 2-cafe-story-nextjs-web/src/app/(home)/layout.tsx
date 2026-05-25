import type { Metadata } from "next";
import { SharedSidebar } from "@/components/layout/shared-sidebar";
import "./../globals.css";

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
      <body className="min-h-full">
        <SharedSidebar />
        <div className="min-h-screen w-full max-w-full overflow-x-clip pl-24 sm:pl-32 xl:pl-80">
          {children}
        </div>
      </body>
    </html>
  );
}
