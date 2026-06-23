"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { CreatePostModal } from "@/components/review/create-post-modal";
import { useCurrentUser } from "@/hooks/use-current-user";
import { getCafePagesByOwnerId } from "@/lib/api/cafes";
import { mockReviewComposer, mockReviewDraftHints } from "@/mocks/reviews";
import type { CafePageResponse } from "@/types/cafe";

type CreatePostContextValue = {
  isOpen: boolean;
  open: () => void;
  close: () => void;
};

const CreatePostContext = createContext<CreatePostContextValue | null>(null);

export function useCreatePost() {
  const ctx = useContext(CreatePostContext);
  if (!ctx) throw new Error("useCreatePost must be used inside CreatePostProvider");
  return ctx;
}

function isActiveCafePage(cafe: CafePageResponse) {
  return cafe.pageActive === true || cafe.status === "ACTIVE";
}

export function CreatePostProvider({ children }: { children: React.ReactNode }) {
  const { user } = useCurrentUser();
  const userId = user?.userId;
  const [isOpen, setIsOpen] = useState(false);
  const [ownedCafePage, setOwnedCafePage] = useState<CafePageResponse | null>(null);

  useEffect(() => {
    let active = true;

    if (!userId) {
      setIsOpen(false);
      setOwnedCafePage(null);
      return () => { active = false; };
    }

    async function load() {
      try {
        const cafes = await getCafePagesByOwnerId(userId!);
        if (active) setOwnedCafePage(cafes.find(isActiveCafePage) ?? null);
      } catch {
        if (active) setOwnedCafePage(null);
      }
    }

    void load();
    return () => { active = false; };
  }, [userId]);

  return (
    <CreatePostContext.Provider value={{ isOpen, open: () => setIsOpen(true), close: () => setIsOpen(false) }}>
      {children}
      <CreatePostModal
        composer={mockReviewComposer}
        hints={mockReviewDraftHints}
        isOpen={isOpen}
        ownedCafePage={ownedCafePage}
        onClose={() => setIsOpen(false)}
      />
    </CreatePostContext.Provider>
  );
}
