"use client";

import { useCallback, useEffect, useState } from "react";
import { getPageMembers } from "@/lib/api/cafes";
import { getUserById } from "@/lib/api/users";
import type { PageMemberResponse } from "@/types/cafe";
import type { UserResponse } from "@/types/user";

export type CoOwnerEntry = {
  member: PageMemberResponse;
  user: UserResponse;
};

type UseCafePageMembersResult = {
  members: PageMemberResponse[];
  coOwners: CoOwnerEntry[];
  isLoading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
};

export function useCafePageMembers(cafePageId: string | null | undefined): UseCafePageMembersResult {
  const [members, setMembers] = useState<PageMemberResponse[]>([]);
  const [coOwners, setCoOwners] = useState<CoOwnerEntry[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!cafePageId) {
      setMembers([]);
      setCoOwners([]);
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const memberList = await getPageMembers(cafePageId);
      setMembers(memberList);

      const activeCoOwners = memberList.filter(
        (m) => m.roleName === "CO_OWNER" && m.status === "ACTIVE",
      );
      const enriched = await Promise.all(
        activeCoOwners.map(async (member) => {
          const user = await getUserById(member.userId);
          return { member, user } satisfies CoOwnerEntry;
        }),
      );
      setCoOwners(enriched);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Không tải được danh sách cộng sự.");
      setMembers([]);
      setCoOwners([]);
    } finally {
      setIsLoading(false);
    }
  }, [cafePageId]);

  useEffect(() => {
    void load();
  }, [load]);

  return { members, coOwners, isLoading, error, refresh: load };
}
