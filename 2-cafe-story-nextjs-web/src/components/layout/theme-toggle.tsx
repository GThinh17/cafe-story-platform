"use client";

import { MoonIcon, SunIcon } from "lucide-react";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";

type ThemeMode = "light" | "dark";

function applyTheme(mode: ThemeMode) {
  document.documentElement.classList.toggle("dark", mode === "dark");
  localStorage.setItem("cafestory-theme", mode);
}

export function ThemeToggle() {
  const [mode, setMode] = useState<ThemeMode>("light");

  useEffect(() => {
    const stored = localStorage.getItem("cafestory-theme") as ThemeMode | null;
    const preferred =
      stored ??
      (window.matchMedia("(prefers-color-scheme: dark)").matches
        ? "dark"
        : "light");

    setMode(preferred);
    applyTheme(preferred);
  }, []);

  return (
    <Button
      aria-label={`Switch to ${mode === "dark" ? "light" : "dark"} mode`}
      className="rounded-full bg-surface shadow-lg"
      onClick={() => {
        const nextMode = mode === "dark" ? "light" : "dark";
        setMode(nextMode);
        applyTheme(nextMode);
      }}
      size="icon-sm"
      type="button"
      variant="outline"
    >
      {mode === "dark" ? <SunIcon /> : <MoonIcon />}
    </Button>
  );
}
