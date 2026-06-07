export type AuthMode = "login" | "register";

export type AuthField = {
  autoComplete: string;
  label: string;
  name: string;
  placeholder: string;
  type: "email" | "password" | "text";
};

export type AuthFormCopy = {
  eyebrow: string;
  title: string;
  description: string;
  submitLabel: string;
  switchPrompt: string;
  switchHref: string;
  switchLabel: string;
};

export type AuthUser = {
  userId: string;
  userName: string;
  userFullName: string | null;
  userEmail: string;
  userPhone: number | null;
  userAvatar: string | null;
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

export type UsernameSuggestionResponse = {
  suggestions: string[];
};

export type LoginRequest = {
  identifier: string;
  password: string;
};

export type RegisterRequest = {
  userName: string;
  userFullName?: string;
  password: string;
  userEmail: string;
  userPhone?: number;
  userAvatar?: string;
};
