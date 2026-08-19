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
import { useUiText } from "@/features/i18n";

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
  const ui = useUiText();
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
        <p className="text-sm font-semibold text-accent">{ui("Request failed")}</p>
        <p className="mt-1 text-sm text-muted">{error}</p>
      </Card>
    );
  }

  if (!rows.length) {
    return (
      <Card className="p-5">
        <p className="text-sm font-semibold text-espresso">{ui(emptyTitle)}</p>
        <p className="mt-1 text-sm text-muted">{ui(emptyDescription)}</p>
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
                  {ui(column.header)}
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
  const ui = useUiText();
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
            aria-label={ui("Row actions")}
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
              {ui(action.label)}
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

/** Compact window: keeps a 3-page slice around current, plus ellipsis + last
 *  (e.g. `1 2 3 … 49`, `… 24 25 26 … 49`, `… 47 48 49`). */
function buildPageWindow(current: number, total: number): (number | "…")[] {
  if (total <= 5) {
    return Array.from({ length: total }, (_, index) => index + 1);
  }

  // Near the start: 1 2 3 … N
  if (current <= 3) {
    return [1, 2, 3, "…", total];
  }

  // Near the end: 1 … N-2 N-1 N
  if (current >= total - 2) {
    return [1, "…", total - 2, total - 1, total];
  }

  // Middle: 1 … c-1 c c+1 … N
  return [1, "…", current - 1, current, current + 1, "…", total];
}

export function AdminPagination<T>({ page, onPageChange }: AdminPaginationProps<T>) {
  const ui = useUiText();
  if (!page) {
    return null;
  }

  const totalPages = Math.max(page.totalPages, 1);
  const currentPage = page.number + 1;
  const pageWindow = buildPageWindow(currentPage, totalPages);

  return (
    <div className="grid grid-cols-3 items-center gap-2 rounded-md border border-border bg-surface px-3 py-2 text-xs text-muted">
      <span className="justify-self-start">
        {ui("{count} records", { count: page.totalElements })}
      </span>
      <div className="flex items-center justify-self-center gap-1">
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          aria-label={ui("First page")}
          disabled={page.first}
          onClick={() => onPageChange(0)}
        >
          <ChevronsLeftIcon />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          aria-label={ui("Previous page")}
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
              aria-label={ui("Page {page}", { page: entry })}
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
          aria-label={ui("Next page")}
          disabled={page.last}
          onClick={() => onPageChange(page.number + 1)}
        >
          <ChevronRightIcon />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          aria-label={ui("Last page")}
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
