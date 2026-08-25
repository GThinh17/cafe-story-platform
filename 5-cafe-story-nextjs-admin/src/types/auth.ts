export type AuthUser = {
  userId: string;
  userName: string;
  userFullName: string | null;
  userEmail: string;
  userPhone: number | null;
  userAvatar: string | null;
  followingCount?: number | null;
  accountStatus: boolean;
  regionCity?: string | null;
  regionProvince?: string | null;
  regionWard?: string | null;
  regionArea?: string | null;
  regionStreet?: string | null;
  roles: string[];
};

export type AuthState = {
  user: AuthUser | null;
  isLoading: boolean;
  isInitialLoading: boolean;
  hasResolvedInitialAuth: boolean;
  isRefreshingAuth: boolean;
  isAuthenticated: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  setUser?: (user: AuthUser | null) => void;
};

export type AuthResponse = {
  user: AuthUser;
};

export type LoginRequest = {
  identifier: string;
  password: string;
};
