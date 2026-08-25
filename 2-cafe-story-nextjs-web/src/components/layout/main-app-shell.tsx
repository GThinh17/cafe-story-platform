import type { ReactNode } from "react";
import { BottomNav } from "@/components/layout/bottom-nav";
import { SharedSidebar } from "@/components/layout/shared-sidebar";
import { CreatePostProvider } from "@/context/create-post-context";
import { CommentModalProvider } from "@/context/comment-modal-context";

type MainAppShellProps = {
  children: ReactNode;
};

export function MainAppShell({ children }: MainAppShellProps) {
  return (
    <CommentModalProvider>
      <CreatePostProvider>
        <SharedSidebar />
        <div className="min-h-screen w-full max-w-full overflow-x-clip pb-[68px] sm:pb-0 sm:pl-28 xl:pl-80">
          {children}
        </div>
        <BottomNav />
      </CreatePostProvider>
    </CommentModalProvider>
  );
}
