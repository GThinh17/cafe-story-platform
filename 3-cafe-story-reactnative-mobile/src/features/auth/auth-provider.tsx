import {
  createContext,
  PropsWithChildren,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";
import {
  getMe,
  login as loginRequest,
  logout as logoutRequest,
  register as registerRequest,
} from "../../services/api";
import type { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from "../../types";

export type AuthContextValue = {
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (request: LoginRequest) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  refreshCurrentUser: () => Promise<void>;
  register: (request: RegisterRequest) => Promise<AuthResponse>;
  user: AuthUser | null;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const refreshCurrentUser = useCallback(async () => {
    setIsLoading(true);

    try {
      const response = await getMe();
      setUser(response.user);
    } catch {
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await loginRequest(request);
    setUser(response.user);
    return response;
  }, []);

  const register = useCallback(async (request: RegisterRequest) => {
    await registerRequest(request);
    const response = await loginRequest({
      identifier: request.userEmail,
      password: request.password,
    });
    setUser(response.user);
    return response;
  }, []);

  const logout = useCallback(async () => {
    try {
      await logoutRequest();
    } finally {
      setUser(null);
    }
  }, []);

  useEffect(() => {
    void refreshCurrentUser();
  }, [refreshCurrentUser]);

  const value = useMemo<AuthContextValue>(
    () => ({
      isAuthenticated: Boolean(user),
      isLoading,
      login,
      logout,
      refreshCurrentUser,
      register,
      user,
    }),
    [isLoading, login, logout, refreshCurrentUser, register, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
