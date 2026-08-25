"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Plus } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { PricingPlanModal } from "@/components/layout/pricing-plan-modal";
import { getCafePagesByOwnerId } from "@/lib/api/cafes";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import type { CafePageResponse } from "@/types/cafe";
import { useI18n } from "@/components/providers/locale-provider";

const BACKEND_DEFAULT_ADDRESS = "Pending update";

type ProfileCafeSectionProps = {
  cafe: CafePageResponse | null;
  isOwnProfile: boolean;
  currentUserId?: string;
};

export function ProfileCafeSection({
  cafe,
  isOwnProfile,
  currentUserId,
}: ProfileCafeSectionProps) {
  if (!cafe) {
    if (!isOwnProfile || !currentUserId) {
      return null;
    }

    return (
      <section className="flex items-center gap-4 overflow-x-auto">
        <CafePageCreateSlot currentUserId={currentUserId} />
      </section>
    );
  }

  const cafeImage =
    cafe.avatarUrl?.trim() || cafe.coverUrl?.trim() || DEFAULT_AVATAR_IMAGE;

  return (
    <section className="flex items-start gap-4 overflow-x-auto">
      <Link
        aria-label={`${cafe.name} cafe page`}
        className="grid w-20 shrink-0 justify-items-center gap-2"
        href={`/cafes/${cafe.id}`}
      >
        <span className="grid size-16 place-items-center overflow-hidden rounded-full border border-border bg-surface p-1 sm:size-20">
          <Avatar className="size-full">
            <AvatarImage alt="" src={cafeImage} />
            <AvatarFallback>{cafe.name.slice(0, 1).toUpperCase()}</AvatarFallback>
          </Avatar>
        </span>
        <span className="block w-20 truncate text-center text-xs font-bold text-foreground">
          {cafe.name}
        </span>
      </Link>
    </section>
  );
}

type CafePageCreateSlotProps = {
  currentUserId: string;
};

function CafePageCreateSlot({ currentUserId }: CafePageCreateSlotProps) {
  const { t } = useI18n();
  const router = useRouter();
  const [isPricingOpen, setIsPricingOpen] = useState(false);
  const [isResolving, setIsResolving] = useState(false);

  async function handleClick() {
    if (isResolving) return;

    setIsResolving(true);
    try {
      const cafes = await getCafePagesByOwnerId(currentUserId);
      const stub = cafes.find(
        (c) => c.address === BACKEND_DEFAULT_ADDRESS || c.status === "DRAFT",
      );

      if (stub) {
        router.push("/cafes/edit");
      } else {
        setIsPricingOpen(true);
      }
    } catch {
      setIsPricingOpen(true);
    } finally {
      setIsResolving(false);
    }
  }

  return (
    <>
      <button
        aria-label={t("profile.createCafePage")}
        className="grid w-20 shrink-0 justify-items-center gap-2 disabled:opacity-60"
        disabled={isResolving}
        onClick={handleClick}
        type="button"
      >
        <span className="grid size-16 place-items-center rounded-full border border-dashed border-border bg-surface text-muted transition hover:border-primary hover:text-primary sm:size-20">
          <Plus className="size-6" />
        </span>
        <span className="block w-20 truncate text-center text-xs font-medium text-muted">
          Create page
        </span>
      </button>
      <PricingPlanModal
        isOpen={isPricingOpen}
        onOpenChange={setIsPricingOpen}
      />
    </>
  );
}
