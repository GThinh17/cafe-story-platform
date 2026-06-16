export type AuthUser = {
  userId: string;
  userName: string;
  userFullName: string | null;
  userEmail: string;
  userPhone: number | null;
  userAvatar: string | null;
  userDescription?: string | null;
  accountStatus: boolean;
  regionArea?: string | null;
  regionCity?: string | null;
  regionCityCode?: string | null;
  regionId?: string | null;
  regionProvince?: string | null;
  regionProvinceCode?: string | null;
  regionStreet?: string | null;
  regionWard?: string | null;
  regionWardCode?: string | null;
  roles: string[];
};

export type AuthResponse = {
  accessToken?: string | null;
  user: AuthUser;
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
  userDescription?: string;
};
