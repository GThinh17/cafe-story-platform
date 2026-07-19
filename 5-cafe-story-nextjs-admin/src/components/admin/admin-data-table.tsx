"use client";

import type { ReactNode } from "react";
import {
  ChevronLeftIcon,
  ChevronRightIcon,
  ChevronsLeftIcon,
  ChevronsRightIcon,
  MoreHorizontalIcon,
  type LucideIcon,
} from "lucide-react";
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
  /** Set false to opt out of the default truncate-fade wrapper (e.g. actions,
   *  images, or cells whose flex children must lay out freely). */
  truncate?: boolean;
  /** Multi-line variant: keeps N lines (2), then fades vertically. Default is
   *  single-line horizontal fade. */
  lines?: 1 | 2;
  /** Explicit max cell width in px. Defaults to 260px for single-line and
   *  360px for 2-line cells. */
  maxWidth?: number;
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
                {columns.map((column) => {
                  const isNarrowCol =
                    /\bw-(?:12|16|20|24|28)\b/.test(column.className ?? "") ||
                    /\btext-right\b/.test(column.className ?? "");
                  const shouldTruncate =
                    column.truncate ?? (!isNarrowCol && column.header !== "");
                  const lines = column.lines ?? 1;
                  const maxWidth = column.maxWidth ?? (lines === 2 ? 360 : 260);
                  return (
                    <TableCell
                      className={cn(
                        "px-3 py-2 align-middle",
                        column.className,
                      )}
                      key={column.header}
                    >
                      {shouldTruncate ? (
                        <div
                          className={cn(
                            "min-w-0",
                            lines === 2
                              ? "truncate-fade-2"
                              : "truncate-fade [&_p]:overflow-hidden [&_p]:text-ellipsis [&_p]:whitespace-nowrap",
                          )}
                          style={{ maxWidth }}
                        >
                          {column.cell(row)}
                        </div>
                      ) : (
                        column.cell(row)
                      )}
                    </TableCell>
                  );
                })}
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

/** Build a compact page list with ellipsis, always keeping the first and last
 *  page visible plus a sliding window around the current page. */
function buildPageWindow(current: number, total: number): (number | "…")[] {
  if (total <= 7) {
    return Array.from({ length: total }, (_, index) => index + 1);
  }

  const pages: (number | "…")[] = [1];
  const windowStart = Math.max(2, current - 1);
  const windowEnd = Math.min(total - 1, current + 1);

  if (windowStart > 2) {
    pages.push("…");
  }
  for (let page = windowStart; page <= windowEnd; page += 1) {
    pages.push(page);
  }
  if (windowEnd < total - 1) {
    pages.push("…");
  }
  pages.push(total);
  return pages;
}

export function AdminPagination<T>({ page, onPageChange }: AdminPaginationProps<T>) {
  if (!page) {
    return null;
  }

  const totalPages = Math.max(page.totalPages, 1);
  const currentPage = page.number + 1;
  const pageWindow = buildPageWindow(currentPage, totalPages);

  return (
    <div className="grid grid-cols-3 items-center gap-2 rounded-md border border-border bg-surface px-3 py-2 text-xs text-muted">
      <span className="justify-self-start">{page.totalElements} records</span>
      <div className="flex items-center justify-self-center gap-1">
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          aria-label="First page"
          disabled={page.first}
          onClick={() => onPageChange(0)}
        >
          <ChevronsLeftIcon />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          aria-label="Previous page"
          disabled={page.first}
          onClick={() => onPageChange(Math.max(page.number - 1, 0))}
        >
          <ChevronLeftIcon />
        </Button>
        {pageWindow.map((entry, index) =>
          entry === "…" ? (
            <span
              className="px-1.5 text-muted"
              key={`ellipsis-${index}`}
              aria-hidden="true"
            >
              …
            </span>
          ) : (
            <Button
              type="button"
              variant={entry === currentPage ? "default" : "ghost"}
              size="icon-xs"
              aria-current={entry === currentPage ? "page" : undefined}
              aria-label={`Page ${entry}`}
              key={entry}
              onClick={() => onPageChange(entry - 1)}
            >
              {entry}
            </Button>
          ),
        )}
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          aria-label="Next page"
          disabled={page.last}
          onClick={() => onPageChange(page.number + 1)}
        >
          <ChevronRightIcon />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          aria-label="Last page"
          disabled={page.last}
          onClick={() => onPageChange(totalPages - 1)}
        >
          <ChevronsRightIcon />
        </Button>
      </div>
      <span aria-hidden="true" className="justify-self-end" />
    </div>
  );
}
