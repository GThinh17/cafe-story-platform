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
  const [hasResolvedInitialAuth, setHasResolvedInitialAuth] = useState(false);
  const [isRefreshingAuth, setIsRefreshingAuth] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const userRef = useRef<AuthUser | null>(null);
  const requestIdRef = useRef(0);

  const applyUser = useCallback((nextUser: AuthUser | null) => {
    userRef.current = nextUser;
    setUser(nextUser);
  }, []);

  const setAuthUser = useCallback((nextUser: AuthUser | null) => {
    requestIdRef.current += 1;
    applyUser(nextUser);
    setError(null);
    setIsLoading(false);
    setIsRefreshingAuth(false);
    setHasResolvedInitialAuth(true);
  }, [applyUser]);

  const refreshAuth = useCallback(async (options?: { background?: boolean }) => {
    const requestId = requestIdRef.current + 1;
    const shouldRefreshInBackground =
      Boolean(options?.background) && Boolean(userRef.current);

    requestIdRef.current = requestId;
    if (shouldRefreshInBackground) {
      setIsRefreshingAuth(true);
    } else {
      setIsLoading(true);
      setIsRefreshingAuth(false);
    }
    setError(null);

    try {
      const response = await getMe();

      if (requestIdRef.current !== requestId) {
        return;
      }

      applyUser(response.user);
    } catch (requestError) {
      if (requestIdRef.current !== requestId) {
        return;
      }

      applyUser(null);

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
        setIsRefreshingAuth(false);
        setHasResolvedInitialAuth(true);
      }
    }
  }, [applyUser]);

  const refetch = useCallback(() => refreshAuth(), [refreshAuth]);

  useEffect(() => {
    void refreshAuth();
  }, [refreshAuth]);

  useEffect(() => {
    function handlePageShow(event: PageTransitionEvent) {
      if (event.persisted) {
        void refreshAuth({ background: true });
      }
    }

    window.addEventListener("pageshow", handlePageShow);

    return () => {
      window.removeEventListener("pageshow", handlePageShow);
    };
  }, [refreshAuth]);

  const value = useMemo<AuthState>(
    () => ({
      user,
      isLoading,
      isInitialLoading: !hasResolvedInitialAuth && isLoading,
      hasResolvedInitialAuth,
      isRefreshingAuth,
      isAuthenticated: Boolean(user),
      error,
      refetch,
      setUser: setAuthUser,
    }),
    [
      error,
      hasResolvedInitialAuth,
      isLoading,
      isRefreshingAuth,
      refetch,
      setAuthUser,
      user,
    ],
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
