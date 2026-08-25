"use client";

import type { ReactNode } from "react";
import Link from "next/link";
import { ChevronRightIcon, type LucideIcon } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { useUiText } from "@/features/i18n";

type AdminStatCardProps = {
  label: string;
  value: ReactNode;
  meta?: string;
  href?: string;
  icon?: LucideIcon;
  iconClassName?: string;
};

export function AdminStatCard({
  label,
  value,
  meta,
  href,
  icon: Icon,
  iconClassName,
}: AdminStatCardProps) {
  const ui = useUiText();
  const content = (
    <CardContent className="flex items-start justify-between gap-3 p-4">
      <div className="min-w-0">
        <p className="text-[11px] font-semibold uppercase tracking-wide text-muted">
          {ui(label)}
        </p>
        <div className="mt-2 text-2xl font-semibold text-espresso">{value}</div>
        {meta ? <p className="mt-1 text-xs text-muted">{ui(meta)}</p> : null}
      </div>
      {Icon ? (
        <span
          className={cn(
            "grid size-9 shrink-0 place-items-center rounded-md bg-primary/10 text-primary",
            iconClassName,
          )}
        >
          <Icon className="size-4.5" />
        </span>
      ) : href ? (
        <ChevronRightIcon className="size-4 shrink-0 text-muted transition group-hover/stat:translate-x-0.5 group-hover/stat:text-primary" />
      ) : null}
    </CardContent>
  );

  if (href) {
    return (
      <Card
        asChild
        className="group/stat transition hover:border-primary/40 hover:shadow-sm"
      >
        <Link href={href} className="block no-underline">
          {content}
        </Link>
      </Card>
    );
  }

  return <Card>{content}</Card>;
}
