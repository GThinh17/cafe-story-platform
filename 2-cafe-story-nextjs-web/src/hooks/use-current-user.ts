"use client";

import { useCallback, useEffect, useState } from "react";
import { ApiError } from "@/lib/api/client";
import { getMe } from "@/lib/api/auth";
import type { AuthUser } from "@/types/auth";

type CurrentUserState = {
  user: AuthUser | null;
  isLoading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
};

export function useCurrentUser(): CurrentUserState {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refetch = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await getMe();
      setUser(response.user);
    } catch (requestError) {
      setUser(null);
      setError(
        requestError instanceof ApiError
          ? requestError.message
          : "Unable to load current user.",
      );
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    user,
    isLoading,
    error,
    refetch,
  };
}
