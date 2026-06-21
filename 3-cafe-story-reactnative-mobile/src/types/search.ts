import type { BlogResponse } from "./feed";
import type { CafePageResponse } from "./cafe-page";
import type { UserResponse } from "./profile";

export type ExploreSearchResults = {
  blogs: BlogResponse[];
  cafePages: CafePageResponse[];
  users: UserResponse[];
};
