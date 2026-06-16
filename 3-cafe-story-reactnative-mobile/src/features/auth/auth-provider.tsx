import {
  createContext,
  PropsWithChildren,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";
import {
  clearAuthAccessToken,
  getMe,
  login as loginRequest,
  logout as logoutRequest,
  register as registerRequest,
  setAuthAccessToken,
} from "../../services/api";
import type { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from "../../types";

export type AuthContextValue = {
  continueAsTestUser: () => void;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (request: LoginRequest) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  refreshCurrentUser: () => Promise<void>;
  register: (request: RegisterRequest) => Promise<AuthResponse>;
  user: AuthUser | null;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

const testUser: AuthUser = {
  accountStatus: true,
  roles: ["USER"],
  userAvatar:
    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=180&q=80",
  userEmail: "test@cafestory.vn",
  userFullName: "Gia Thinh",
  userId: "00000000-0000-4000-8000-000000000001",
  userName: "gthinh_1704",
  userPhone: null,
};

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const refreshCurrentUser = useCallback(async () => {
    setIsLoading(true);

    try {
      const response = await getMe();
      setUser(response.user);
    } catch {
      clearAuthAccessToken();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const continueAsTestUser = useCallback(() => {
    setUser(testUser);
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    const loginResponse = await loginRequest(request);
    setAuthAccessToken(loginResponse.accessToken);

    const currentUserResponse = await getMe();
    setUser(currentUserResponse.user);

    return currentUserResponse;
  }, []);

  const register = useCallback(async (request: RegisterRequest) => {
    const response = await registerRequest(request);
    setAuthAccessToken(response.accessToken);

    return response;
  }, []);

  const logout = useCallback(async () => {
    try {
      await logoutRequest();
    } catch {
      // Mobile uses bearer auth, so the backend may not receive the refresh cookie.
      // Local credentials must still be cleared so the app returns to auth flow.
    } finally {
      clearAuthAccessToken();
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
      continueAsTestUser,
      login,
      logout,
      refreshCurrentUser,
      register,
      user,
    }),
    [
      continueAsTestUser,
      isLoading,
      login,
      logout,
      refreshCurrentUser,
      register,
      user,
    ],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
