"use client";

import type { ReactNode } from "react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
import type { PageResponse } from "@/types/api";

export type AdminTableColumn<T> = {
  header: string;
  className?: string;
  cell: (row: T) => ReactNode;
};

type AdminDataTableProps<T> = {
  columns: AdminTableColumn<T>[];
  rows: T[];
  getRowKey: (row: T) => string;
  isLoading?: boolean;
  error?: string | null;
  emptyTitle?: string;
  emptyDescription?: string;
};

export function AdminDataTable<T>({
  columns,
  rows,
  getRowKey,
  isLoading = false,
  error,
  emptyTitle = "No records",
  emptyDescription = "Try changing the filters or refresh the page.",
}: AdminDataTableProps<T>) {
  if (isLoading) {
    return (
      <Card className="overflow-hidden">
        <div className="flex flex-col gap-3 p-4">
          {Array.from({ length: 6 }).map((_, index) => (
            <Skeleton className="h-12 w-full" key={index} />
          ))}
        </div>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="p-5">
        <p className="text-sm font-bold text-accent">Request failed</p>
        <p className="mt-2 text-sm text-muted">{error}</p>
      </Card>
    );
  }

  if (!rows.length) {
    return (
      <Card className="p-6">
        <p className="text-sm font-black text-espresso">{emptyTitle}</p>
        <p className="mt-2 text-sm text-muted">{emptyDescription}</p>
      </Card>
    );
  }

  return (
    <Card className="overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full min-w-[880px] border-collapse text-sm">
          <thead className="bg-surface-muted/70 text-left text-xs font-black uppercase tracking-[0.1em] text-muted">
            <tr>
              {columns.map((column) => (
                <th className={cn("px-4 py-3", column.className)} key={column.header}>
                  {column.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr className="border-t border-border" key={getRowKey(row)}>
                {columns.map((column) => (
                  <td
                    className={cn("px-4 py-3 align-top", column.className)}
                    key={column.header}
                  >
                    {column.cell(row)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  );
}

type AdminPaginationProps<T> = {
  page: PageResponse<T> | null;
  onPageChange: (page: number) => void;
};

export function AdminPagination<T>({ page, onPageChange }: AdminPaginationProps<T>) {
  if (!page) {
    return null;
  }

  return (
    <div className="flex flex-col gap-3 rounded-md border border-border bg-surface px-4 py-3 text-sm text-muted sm:flex-row sm:items-center sm:justify-between">
      <span>
        Page {page.number + 1} of {Math.max(page.totalPages, 1)} ·{" "}
        {page.totalElements} records
      </span>
      <div className="flex gap-2">
        <Button
          type="button"
          variant="outline"
          size="sm"
          disabled={page.first}
          onClick={() => onPageChange(Math.max(page.number - 1, 0))}
        >
          Previous
        </Button>
        <Button
          type="button"
          variant="outline"
          size="sm"
          disabled={page.last}
          onClick={() => onPageChange(page.number + 1)}
        >
          Next
        </Button>
      </div>
    </div>
  );
}
