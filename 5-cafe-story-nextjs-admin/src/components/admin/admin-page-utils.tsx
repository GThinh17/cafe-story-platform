"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import Link from "next/link";
import { EyeIcon, RefreshCwIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import type { PageResponse } from "@/types/api";

export const PAGE_SIZE = 12;

export function formatDate(value: string | null | undefined) {
  if (!value) {
    return "—";
  }

  return new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export function shortId(value: string | null | undefined) {
  if (!value) {
    return "—";
  }

  return value.slice(0, 8);
}

export function textPreview(value: string | null | undefined, max = 96) {
  if (!value) {
    return "—";
  }

  return value.length > max ? `${value.slice(0, max)}...` : value;
}

export function usePagedAdminResource<T>(
  loader: (page: number, signal: AbortSignal) => Promise<PageResponse<T>>,
  deps: readonly unknown[],
) {
  const [pageNumber, setPageNumber] = useState(0);
  const [data, setData] = useState<PageResponse<T> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (nextPage: number, signal: AbortSignal) => {
      const requestId = requestIdRef.current + 1;
      requestIdRef.current = requestId;
      setIsLoading(true);
      setError(null);

      try {
        const response = await loader(nextPage, signal);

        if (requestIdRef.current === requestId) {
          setData(response);
        }
      } catch (requestError) {
        if (signal.aborted || requestIdRef.current !== requestId) {
          return;
        }

        setError(
          requestError instanceof Error
            ? requestError.message
            : "Unable to load records.",
        );
      } finally {
        if (!signal.aborted && requestIdRef.current === requestId) {
          setIsLoading(false);
        }
      }
    },
    deps,
  );

  useEffect(() => {
    setPageNumber(0);
  }, deps);

  useEffect(() => {
    const controller = new AbortController();
    void loadPage(pageNumber, controller.signal);

    return () => controller.abort();
  }, [loadPage, pageNumber]);

  const refetch = useCallback(() => {
    const controller = new AbortController();
    void loadPage(pageNumber, controller.signal);
  }, [loadPage, pageNumber]);

  const updateRow = useCallback(
    (predicate: (item: T) => boolean, updater: (item: T) => T) => {
      setData((prev) => {
        if (!prev) return prev;
        return {
          ...prev,
          content: prev.content.map((item) => (predicate(item) ? updater(item) : item)),
        };
      });
    },
    [],
  );

  const removeRow = useCallback(
    (predicate: (item: T) => boolean) => {
      setData((prev) => {
        if (!prev) return prev;
        return {
          ...prev,
          content: prev.content.filter((item) => !predicate(item)),
          totalElements: Math.max(0, (prev.totalElements ?? 0) - 1),
        };
      });
    },
    [],
  );

  return useMemo(
    () => ({
      data,
      rows: data?.content ?? [],
      isLoading,
      error,
      pageNumber,
      setPageNumber,
      refetch,
      updateRow,
      removeRow,
    }),
    [data, error, isLoading, pageNumber, refetch, updateRow, removeRow],
  );
}

export function useAdminDetailResource<T>() {
  const [open, setOpen] = useState(false);
  const [data, setData] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const requestIdRef = useRef(0);

  const load = useCallback(
    async (loader: (signal: AbortSignal) => Promise<T>) => {
      const controller = new AbortController();
      const requestId = requestIdRef.current + 1;
      requestIdRef.current = requestId;
      setOpen(true);
      setData(null);
      setError(null);
      setIsLoading(true);

      try {
        const response = await loader(controller.signal);

        if (requestIdRef.current === requestId) {
          setData(response);
        }
      } catch (requestError) {
        if (controller.signal.aborted || requestIdRef.current !== requestId) {
          return;
        }

        setError(
          requestError instanceof Error ? requestError.message : "Unable to load detail.",
        );
      } finally {
        if (!controller.signal.aborted && requestIdRef.current === requestId) {
          setIsLoading(false);
        }
      }
    },
    [],
  );

  return {
    open,
    setOpen,
    data,
    setData,
    isLoading,
    error,
    load,
  };
}

export function FilterSelect<T extends string>({
  value,
  options,
  placeholder,
  label,
  onChange,
}: {
  value: T | "";
  options: readonly T[];
  placeholder: string;
  label?: string;
  onChange: (value: T | "") => void;
}) {
  const select = (
    <Select
      value={value || "all"}
      onValueChange={(nextValue) => onChange(nextValue === "all" ? "" : (nextValue as T))}
    >
      <SelectTrigger size="lg" className="rounded-md w-full bg-surface sm:w-44">
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>
      <SelectContent>
        <SelectGroup>
          <SelectItem value="all">All</SelectItem>
          {options.map((option) => (
            <SelectItem value={option} key={option}>
              {option}
            </SelectItem>
          ))}
        </SelectGroup>
      </SelectContent>
    </Select>
  );

  if (!label) return select;

  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs font-medium text-muted">{label}</span>
      {select}
    </div>
  );
}

export function BooleanFilterSelect({
  value,
  onChange,
  label,
  trueLabel = "Active",
  falseLabel = "Inactive",
}: {
  value: boolean | null;
  onChange: (value: boolean | null) => void;
  label?: string;
  trueLabel?: string;
  falseLabel?: string;
}) {
  const select = (
    <Select
      value={value === null ? "all" : String(value)}
      onValueChange={(nextValue) =>
        onChange(nextValue === "all" ? null : nextValue === "true")
      }
    >
      <SelectTrigger size="lg" className="rounded-md w-full bg-surface sm:w-44">
        <SelectValue placeholder="Status" />
      </SelectTrigger>
      <SelectContent>
        <SelectGroup>
          <SelectItem value="all">All</SelectItem>
          <SelectItem value="true">{trueLabel}</SelectItem>
          <SelectItem value="false">{falseLabel}</SelectItem>
        </SelectGroup>
      </SelectContent>
    </Select>
  );

  if (!label) return select;

  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs font-medium text-muted">{label}</span>
      {select}
    </div>
  );
}

export function FilterInput({
  value,
  placeholder,
  label,
  onChange,
}: {
  value: string;
  placeholder: string;
  label?: string;
  onChange: (value: string) => void;
}) {
  const input = (
    <Input
      className="h-10 bg-surface sm:w-44"
      value={value}
      placeholder={placeholder}
      onChange={(event) => onChange(event.target.value)}
    />
  );

  if (!label) return input;

  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs font-medium text-muted">{label}</span>
      {input}
    </div>
  );
}

export function Toolbar({
  children,
  onRefresh,
}: {
  children: React.ReactNode;
  onRefresh: () => void;
}) {
  return (
    <div className="flex flex-col gap-3 rounded-md border border-border bg-surface p-3 sm:flex-row sm:flex-wrap sm:items-end">
      {children}
      <Button
        className="sm:ml-auto"
        type="button"
        variant="outline"
        size="sm"
        onClick={onRefresh}
      >
        <RefreshCwIcon data-icon="inline-start" />
        Refresh
      </Button>
    </div>
  );
}

export function DetailLink({ href }: { href: string }) {
  return (
    <Button asChild type="button" variant="outline" size="sm">
      <Link href={href}>
        <EyeIcon data-icon="inline-start" />
        View
      </Link>
    </Button>
  );
}

export function FormTextarea({
  value,
  placeholder,
  onChange,
}: {
  value: string;
  placeholder: string;
  onChange: (value: string) => void;
}) {
  return (
    <Textarea
      value={value}
      placeholder={placeholder}
      onChange={(event) => onChange(event.target.value)}
    />
  );
}
