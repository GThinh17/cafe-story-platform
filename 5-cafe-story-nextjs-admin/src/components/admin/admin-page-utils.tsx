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

  return useMemo(
    () => ({
      data,
      rows: data?.content ?? [],
      isLoading,
      error,
      pageNumber,
      setPageNumber,
      refetch,
    }),
    [data, error, isLoading, pageNumber, refetch],
  );
}

export function FilterSelect<T extends string>({
  value,
  options,
  placeholder,
  onChange,
}: {
  value: T | "";
  options: readonly T[];
  placeholder: string;
  onChange: (value: T | "") => void;
}) {
  return (
    <Select
      value={value || "all"}
      onValueChange={(nextValue) => onChange(nextValue === "all" ? "" : (nextValue as T))}
    >
      <SelectTrigger className="h-10 w-full bg-surface sm:w-44">
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
}

export function BooleanFilterSelect({
  value,
  onChange,
  trueLabel = "Active",
  falseLabel = "Inactive",
}: {
  value: boolean | null;
  onChange: (value: boolean | null) => void;
  trueLabel?: string;
  falseLabel?: string;
}) {
  return (
    <Select
      value={value === null ? "all" : String(value)}
      onValueChange={(nextValue) =>
        onChange(nextValue === "all" ? null : nextValue === "true")
      }
    >
      <SelectTrigger className="h-10 w-full bg-surface sm:w-44">
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
}

export function FilterInput({
  value,
  placeholder,
  onChange,
}: {
  value: string;
  placeholder: string;
  onChange: (value: string) => void;
}) {
  return (
    <Input
      className="h-10 bg-surface sm:w-64"
      value={value}
      placeholder={placeholder}
      onChange={(event) => onChange(event.target.value)}
    />
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
    <div className="flex flex-col gap-3 rounded-md border border-border bg-surface p-3 sm:flex-row sm:flex-wrap sm:items-center">
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
