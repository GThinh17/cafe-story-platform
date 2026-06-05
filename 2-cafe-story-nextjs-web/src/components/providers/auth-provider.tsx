"use client";

import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { ApiError } from "@/lib/api/client";
import { getMe } from "@/lib/api/auth";
import type { AuthState, AuthUser } from "@/types/auth";

const AuthContext = createContext<AuthState | null>(null);

type AuthProviderProps = {
  children: ReactNode;
};

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const requestIdRef = useRef(0);

  const setAuthUser = useCallback((nextUser: AuthUser | null) => {
    requestIdRef.current += 1;
    setUser(nextUser);
    setError(null);
    setIsLoading(false);
  }, []);

  const refetch = useCallback(async () => {
    const requestId = requestIdRef.current + 1;

    requestIdRef.current = requestId;
    setIsLoading(true);
    setError(null);

    try {
      const response = await getMe();

      if (requestIdRef.current !== requestId) {
        return;
      }

      setUser(response.user);
    } catch (requestError) {
      if (requestIdRef.current !== requestId) {
        return;
      }

      setUser(null);

      if (
        requestError instanceof ApiError &&
        (requestError.statusCode === 401 || requestError.statusCode === 403)
      ) {
        setError(null);
      } else {
        setError(
          requestError instanceof ApiError
            ? requestError.message
            : "Unable to load current user.",
        );
      }
    } finally {
      if (requestIdRef.current === requestId) {
        setIsLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  useEffect(() => {
    function handlePageShow(event: PageTransitionEvent) {
      if (event.persisted) {
        void refetch();
      }
    }

    window.addEventListener("pageshow", handlePageShow);

    return () => {
      window.removeEventListener("pageshow", handlePageShow);
    };
  }, [refetch]);

  const value = useMemo<AuthState>(
    () => ({
      user,
      isLoading,
      isAuthenticated: Boolean(user),
      error,
      refetch,
      setUser: setAuthUser,
    }),
    [error, isLoading, refetch, setAuthUser, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within AuthProvider.");
  }

  return context;
}
