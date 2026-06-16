import type { ReactNode } from "react";
import { Card, CardContent } from "@/components/ui/card";

type AdminStatCardProps = {
  label: string;
  value: ReactNode;
  meta?: string;
};

export function AdminStatCard({ label, value, meta }: AdminStatCardProps) {
  return (
    <Card>
      <CardContent className="p-4">
        <p className="text-xs font-bold uppercase tracking-[0.12em] text-muted">
          {label}
        </p>
        <div className="mt-3 text-2xl font-black text-espresso">{value}</div>
        {meta ? <p className="mt-2 text-xs font-semibold text-muted">{meta}</p> : null}
      </CardContent>
    </Card>
  );
}
