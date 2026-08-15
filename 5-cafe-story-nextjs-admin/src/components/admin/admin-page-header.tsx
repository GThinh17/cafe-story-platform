"use client";

import type { ReactNode } from "react";
import { useUiText } from "@/features/i18n";

type AdminPageHeaderProps = {
  title: string;
  description?: string;
  actions?: ReactNode;
};

export function AdminPageHeader({ title, description, actions }: AdminPageHeaderProps) {
  const ui = useUiText();
  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div className="min-w-0">
        <h1 className="text-xl font-semibold text-espresso">{ui(title)}</h1>
        {description ? (
          <p className="mt-1 max-w-3xl text-sm leading-6 text-muted">{ui(description)}</p>
        ) : null}
      </div>
      {actions ? <div className="shrink-0">{actions}</div> : null}
    </div>
  );
}
