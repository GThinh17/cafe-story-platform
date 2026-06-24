"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { CheckIcon, ChevronDownIcon, LoaderCircleIcon } from "lucide-react";
import { cn } from "@/lib/utils";
import type {
  RegionCityResponse,
  RegionProvinceResponse,
  RegionWardResponse,
} from "@/lib/api/regions";

export type SearchableDropdownOption =
  | RegionProvinceResponse
  | RegionCityResponse
  | RegionWardResponse;

export type SearchableDropdownProps<T extends SearchableDropdownOption> = {
  className?: string;
  disabled?: boolean;
  emptyLabel: string;
  isLoading?: boolean;
  label?: string;
  labelClassName?: string;
  onSelect: (option: T) => void;
  options: T[];
  placeholder: string;
  selectedCode?: string | null;
  selectedName?: string | null;
  triggerClassName?: string;
  valueKey: keyof T;
};

export function SearchableDropdown<T extends SearchableDropdownOption>({
  className,
  disabled = false,
  emptyLabel,
  isLoading = false,
  label,
  labelClassName,
  onSelect,
  options,
  placeholder,
  selectedCode,
  selectedName,
  triggerClassName,
  valueKey,
}: SearchableDropdownProps<T>) {
  const [isOpen, setIsOpen] = useState(false);
  const [query, setQuery] = useState("");
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const filtered = useMemo(() => {
    if (!query) return options;
    const lower = query.toLowerCase();
    return options.filter((o) => o.name.toLowerCase().includes(lower));
  }, [options, query]);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false);
        setQuery("");
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    if (!isOpen) setQuery("");
  }, [isOpen]);

  function handleSelect(option: T) {
    onSelect(option);
    setIsOpen(false);
    setQuery("");
  }

  return (
    <div className={cn("flex flex-col gap-1.5", className)} ref={containerRef}>
      {label && (
        <span className={cn("text-xs font-bold uppercase tracking-wider text-muted", labelClassName)}>
          {label}
        </span>
      )}

      {isLoading ? (
        <div className="flex h-10 items-center gap-2 rounded-md border border-line-soft bg-surface-muted px-3 text-sm text-muted">
          <LoaderCircleIcon className="size-4 animate-spin" />
          Loading...
        </div>
      ) : (
        <div className="relative">
          <button
            className={cn(
              "flex h-10 w-full items-center justify-between rounded-md border px-3 text-sm transition",
              disabled
                ? "cursor-not-allowed border-line-soft bg-surface-muted text-muted opacity-60"
                : selectedCode
                  ? "border-espresso bg-espresso/5 font-bold text-espresso"
                  : "border-line-soft bg-surface-muted text-muted hover:border-espresso",
              triggerClassName,
            )}
            disabled={disabled}
            onClick={() => {
              setIsOpen(!isOpen);
              setTimeout(() => inputRef.current?.focus(), 0);
            }}
            type="button"
          >
            <span className="truncate">{selectedName ?? placeholder}</span>
            <ChevronDownIcon
              className={`size-4 shrink-0 transition-transform ${isOpen ? "rotate-180" : ""}`}
            />
          </button>

          {isOpen && (
            <div className="absolute z-50 mt-1 w-full overflow-hidden rounded-md border border-line-soft bg-surface shadow-lg">
              <div className="border-b border-line-soft px-3 py-2">
                <input
                  autoComplete="off"
                  className="w-full bg-transparent text-sm text-espresso outline-none placeholder:text-muted"
                  onChange={(e) => setQuery(e.target.value)}
                  placeholder={label ? `Search ${label.toLowerCase()}...` : "Search..."}
                  ref={inputRef}
                  type="text"
                  value={query}
                />
              </div>
              <div className="max-h-48 overflow-y-auto">
                {filtered.length > 0 ? (
                  filtered.map((option) => {
                    const code = String(option[valueKey]);
                    const isSelected = code === selectedCode;

                    return (
                      <button
                        className={`flex w-full items-center gap-2 px-3 py-2 text-left text-sm transition ${
                          isSelected
                            ? "bg-espresso/10 font-bold text-espresso"
                            : "text-espresso hover:bg-surface-muted"
                        }`}
                        key={code}
                        onClick={() => handleSelect(option)}
                        type="button"
                      >
                        {isSelected && <CheckIcon className="size-4 shrink-0" />}
                        <span className={isSelected ? "" : "pl-6"}>{option.name}</span>
                      </button>
                    );
                  })
                ) : (
                  <p className="px-3 py-3 text-sm text-muted">{emptyLabel}</p>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
