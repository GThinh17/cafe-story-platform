"use client";

import type { ReactNode } from "react";
import { MoreHorizontalIcon, type LucideIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
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
  onRowClick?: (row: T) => void;
};

export function AdminDataTable<T>({
  columns,
  rows,
  getRowKey,
  isLoading = false,
  error,
  emptyTitle = "No records",
  emptyDescription = "Try changing the filters or refresh the page.",
  onRowClick,
}: AdminDataTableProps<T>) {
  if (isLoading) {
    return (
      <Card className="overflow-hidden">
        <div className="flex flex-col gap-2 p-3">
          {Array.from({ length: 6 }).map((_, index) => (
            <Skeleton className="h-10 w-full" key={index} />
          ))}
        </div>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="p-4">
        <p className="text-sm font-semibold text-accent">Request failed</p>
        <p className="mt-1 text-sm text-muted">{error}</p>
      </Card>
    );
  }

  if (!rows.length) {
    return (
      <Card className="p-5">
        <p className="text-sm font-semibold text-espresso">{emptyTitle}</p>
        <p className="mt-1 text-sm text-muted">{emptyDescription}</p>
      </Card>
    );
  }

  return (
    <Card className="overflow-hidden">
      <div className="overflow-x-auto">
        <Table className="min-w-[720px]">
          <TableHeader className="bg-surface-muted/70">
            <TableRow className="hover:bg-transparent">
              {columns.map((column) => (
                <TableHead
                  className={cn(
                    "h-9 px-3 py-2 text-[11px] font-semibold uppercase tracking-wide text-muted",
                    column.className,
                  )}
                  key={column.header}
                >
                  {column.header}
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {rows.map((row) => (
              <TableRow
                className={cn(
                  "border-t border-border",
                  onRowClick &&
                    "cursor-pointer transition-colors hover:bg-surface-muted/50 focus-visible:bg-surface-muted/50 focus-visible:outline-none",
                )}
                key={getRowKey(row)}
                tabIndex={onRowClick ? 0 : undefined}
                onClick={onRowClick ? () => onRowClick(row) : undefined}
                onKeyDown={
                  onRowClick
                    ? (event) => {
                        if (
                          (event.key === "Enter" || event.key === " ") &&
                          event.target === event.currentTarget
                        ) {
                          event.preventDefault();
                          onRowClick(row);
                        }
                      }
                    : undefined
                }
              >
                {columns.map((column) => (
                  <TableCell
                    className={cn("px-3 py-2 align-middle", column.className)}
                    key={column.header}
                  >
                    {column.cell(row)}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </Card>
  );
}

export type AdminRowAction = {
  label: string;
  icon?: LucideIcon;
  destructive?: boolean;
  disabled?: boolean;
  onSelect: () => void;
};

export function AdminRowActions({ actions }: { actions: AdminRowAction[] }) {
  if (!actions.length) {
    return null;
  }

  return (
    <div
      className="flex justify-end"
      onClick={(event) => event.stopPropagation()}
      onKeyDown={(event) => event.stopPropagation()}
    >
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            type="button"
            variant="ghost"
            size="icon-sm"
            aria-label="Row actions"
          >
            <MoreHorizontalIcon />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="min-w-44">
          {actions.map((action) => (
            <DropdownMenuItem
              key={action.label}
              disabled={action.disabled}
              variant={action.destructive ? "destructive" : "default"}
              onSelect={() => action.onSelect()}
            >
              {action.icon ? <action.icon className="size-3.5" /> : null}
              {action.label}
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
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
    <div className="flex flex-col gap-2 rounded-md border border-border bg-surface px-3 py-2 text-xs text-muted sm:flex-row sm:items-center sm:justify-between">
      <span>
        Page {page.number + 1} of {Math.max(page.totalPages, 1)} ·{" "}
        {page.totalElements} records
      </span>
      <div className="flex gap-2">
        <Button
          type="button"
          variant="outline"
          size="xs"
          disabled={page.first}
          onClick={() => onPageChange(Math.max(page.number - 1, 0))}
        >
          Previous
        </Button>
        <Button
          type="button"
          variant="outline"
          size="xs"
          disabled={page.last}
          onClick={() => onPageChange(page.number + 1)}
        >
          Next
        </Button>
      </div>
    </div>
  );
}
