"use client";

import type { ReactNode } from "react";
import { ReviewerDashboardSidebar } from "@/features/reviewer-dashboard/components/reviewer-dashboard-sidebar";

import { mockReviewerProfile } from "@/features/reviewer-dashboard/reviewer-dashboard.mock";

type ReviewerDashboardShellProps = {
  children: ReactNode;
};

export function ReviewerDashboardShell({
  children,
}: ReviewerDashboardShellProps) {
  return (
    <div className="min-h-screen bg-background text-foreground">
      <ReviewerDashboardSidebar profile={mockReviewerProfile} />

      <div className="min-h-screen lg:pl-72">
       
        <main className="px-4 py-6 sm:px-6 lg:px-8">{children}</main>
      </div>
    </div>
  );
}
