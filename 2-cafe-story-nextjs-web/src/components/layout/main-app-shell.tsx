import type { ReactNode } from "react";
import { SharedSidebar } from "@/components/layout/shared-sidebar";

type MainAppShellProps = {
  children: ReactNode;
};

export function MainAppShell({ children }: MainAppShellProps) {
  return (
    <>
      <SharedSidebar />
      <div className="min-h-screen w-full max-w-full overflow-x-clip pl-20 sm:pl-28 xl:pl-80">
        {children}
      </div>
    </>
  );
}
