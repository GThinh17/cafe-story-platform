import type {
  BlogResponse,
  CafePageResponse,
  ExploreSearchResults,
  UserResponse,
} from "../../types";
import { getBlogs } from "./blogs";
import { getCafePages } from "./cafe-pages";
import { getUsers } from "./users";

const MAX_SEARCH_RESULTS_PER_GROUP = 8;

function normalize(value: string | null | undefined) {
  return (value ?? "").trim().toLowerCase();
}

function containsQuery(values: Array<string | null | undefined>, query: string) {
  const normalizedQuery = normalize(query);

  return values.some((value) => normalize(value).includes(normalizedQuery));
}

function filterUsers(users: UserResponse[], query: string) {
  return users
    .filter((user) =>
      containsQuery(
        [
          user.userName,
          user.userFullName,
          user.userDescription,
          user.regionCity,
          user.regionProvince,
        ],
        query,
      ),
    )
    .slice(0, MAX_SEARCH_RESULTS_PER_GROUP);
}

function filterCafePages(cafePages: CafePageResponse[], query: string) {
  return cafePages
    .filter((page) =>
      containsQuery(
        [
          page.name,
          page.description,
          page.address,
          page.regionArea,
          page.regionCity,
          page.regionProvince,
          page.regionStreet,
          page.regionWard,
        ],
        query,
      ),
    )
    .slice(0, MAX_SEARCH_RESULTS_PER_GROUP);
}

function filterBlogs(blogs: BlogResponse[], query: string) {
  return blogs
    .filter((blog) =>
      containsQuery(
        [
          blog.content,
          blog.authorUserName,
          blog.authorUserFullName,
          blog.displayName,
          blog.pageName,
        ],
        query,
      ),
    )
    .slice(0, MAX_SEARCH_RESULTS_PER_GROUP);
}

export async function searchExplore(query: string): Promise<ExploreSearchResults> {
  const normalizedQuery = query.trim();

  if (normalizedQuery.length < 2) {
    return {
      blogs: [],
      cafePages: [],
      users: [],
    };
  }

  const [users, cafePages, blogs] = await Promise.all([
    getUsers().catch(() => []),
    getCafePages().catch(() => []),
    getBlogs().catch(() => []),
  ]);

  return {
    blogs: filterBlogs(blogs, normalizedQuery),
    cafePages: filterCafePages(cafePages, normalizedQuery),
    users: filterUsers(users, normalizedQuery),
  };
}
