"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { ShieldCheckIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { BrandIcon } from "@/components/ui/brand-icon";
import { login } from "@/lib/api/auth";
import { useCurrentUser } from "@/hooks/use-current-user";
import { hasAdminRole } from "@/lib/auth";

function getNextPath() {
  if (typeof window === "undefined") {
    return "/";
  }

  const params = new URLSearchParams(window.location.search);
  const next = params.get("next");

  if (!next || !next.startsWith("/") || next.startsWith("//")) {
    return "/";
  }

  return next;
}

export function AdminLoginPage() {
  const router = useRouter();
  const {
    user,
    isInitialLoading,
    hasResolvedInitialAuth,
    isRefreshingAuth,
    setUser,
  } = useCurrentUser();
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const nextPath = useMemo(getNextPath, []);

  useEffect(() => {
    if (
      hasResolvedInitialAuth &&
      !isRefreshingAuth &&
      hasAdminRole(user)
    ) {
      router.replace(nextPath);
    }
  }, [hasResolvedInitialAuth, isRefreshingAuth, nextPath, router, user]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      const response = await login({ identifier, password });

      if (!hasAdminRole(response.user)) {
        setError("This account does not have ADMIN access.");
        return;
      }

      setUser?.(response.user);
      router.replace(nextPath);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to sign in.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  const accessDeniedMessage =
    hasResolvedInitialAuth && user && !hasAdminRole(user)
      ? "This account does not have ADMIN access."
      : null;

  return (
    <div className="grid min-h-screen place-items-center bg-background p-6 text-foreground">
      <Card className="w-full max-w-md">
        <CardHeader className="flex flex-col gap-4">
          <div className="flex items-center gap-3">
            <BrandIcon className="size-11 shadow-sm" />
            <div>
              <p className="text-sm font-black text-espresso">CafeStory Admin</p>
              <p className="text-xs font-semibold text-muted">
                Secure operations sign in
              </p>
            </div>
          </div>
          <CardTitle className="flex items-center gap-2">
            <ShieldCheckIcon className="size-5 text-primary" />
            Sign in
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form className="flex flex-col gap-3" onSubmit={handleSubmit}>
            <Input
              autoComplete="username"
              value={identifier}
              placeholder="Email or username"
              disabled={isInitialLoading || isSubmitting}
              onChange={(event) => setIdentifier(event.target.value)}
            />
            <Input
              autoComplete="current-password"
              type="password"
              value={password}
              placeholder="Password"
              disabled={isInitialLoading || isSubmitting}
              onChange={(event) => setPassword(event.target.value)}
            />
            {accessDeniedMessage ? (
              <p className="text-sm text-accent">{accessDeniedMessage}</p>
            ) : null}
            {error ? <p className="text-sm text-accent">{error}</p> : null}
            <Button
              type="submit"
              disabled={
                isInitialLoading ||
                isSubmitting ||
                !identifier.trim() ||
                !password
              }
            >
              {isSubmitting || isInitialLoading ? "Signing in..." : "Sign in"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
