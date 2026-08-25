"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import {
  CreatePostModal,
  type CreatePostInitialImage,
} from "@/components/review/create-post-modal";
import {
  CreatePostSetupModal,
  type CroppedImage,
} from "@/components/review/create-post-setup-modal";
import { useCurrentUser } from "@/hooks/use-current-user";
import { getCafePagesByOwnerId } from "@/lib/api/cafes";
import { mockReviewComposer, mockReviewDraftHints } from "@/mocks/reviews";
import type { CafePageResponse } from "@/types/cafe";

type CreatePostContextValue = {
  isOpen: boolean;
  open: () => void;
  close: () => void;
};

type CreatePostStep = "idle" | "setup" | "compose";

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
  const [step, setStep] = useState<CreatePostStep>("idle");
  const [composeImages, setComposeImages] = useState<CreatePostInitialImage[]>([]);
  const [ownedCafePage, setOwnedCafePage] = useState<CafePageResponse | null>(null);
  const [sessionKey, setSessionKey] = useState(0);

  useEffect(() => {
    let active = true;

    if (!userId) {
      setStep("idle");
      setComposeImages([]);
      setOwnedCafePage(null);
      return () => {
        active = false;
      };
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
    return () => {
      active = false;
    };
  }, [userId]);

  const open = useCallback(() => {
    setComposeImages([]);
    setSessionKey((k) => k + 1);
    setStep("setup");
  }, []);

  const close = useCallback(() => {
    setStep("idle");
    setComposeImages([]);
  }, []);

  const handleSetupNext = useCallback((images: CroppedImage[]) => {
    setComposeImages(
      images.map((img) => ({ id: img.id, file: img.file, name: img.name })),
    );
    setStep("compose");
  }, []);

  const handleComposeBack = useCallback(() => {
    setStep("setup");
  }, []);

  const isOpen = step !== "idle";

  return (
    <CreatePostContext.Provider value={{ isOpen, open, close }}>
      {children}
      <CreatePostSetupModal
        isOpen={step === "setup"}
        key={`setup-${sessionKey}`}
        onClose={close}
        onNext={handleSetupNext}
      />
      <CreatePostModal
        composer={mockReviewComposer}
        hints={mockReviewDraftHints}
        initialImages={composeImages}
        isOpen={step === "compose"}
        key={`compose-${sessionKey}`}
        onBack={handleComposeBack}
        onClose={close}
        ownedCafePage={ownedCafePage}
      />
    </CreatePostContext.Provider>
  );
}
