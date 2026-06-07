"use client";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";

type CafeMapModalProps = {
  cafeName: string;
  onOpenChange: (open: boolean) => void;
  open: boolean;
  regionArea?: string | null;
  regionCity?: string | null;
  regionProvince?: string | null;
  regionStreet?: string | null;
  regionWard?: string | null;
};

function buildLocationQuery({
  regionArea,
  regionCity,
  regionProvince,
  regionStreet,
  regionWard,
}: Pick<
  CafeMapModalProps,
  | "regionArea"
  | "regionCity"
  | "regionProvince"
  | "regionStreet"
  | "regionWard"
>) {
  return [
    regionStreet,
    regionWard,
    regionArea,
    regionCity,
    regionProvince,
  ]
    .map((value) => value?.trim())
    .filter(Boolean)
    .join(", ");
}

export function CafeMapModal({
  cafeName,
  onOpenChange,
  open,
  regionArea,
  regionCity,
  regionProvince,
  regionStreet,
  regionWard,
}: CafeMapModalProps) {
  const locationQuery = buildLocationQuery({
    regionArea,
    regionCity,
    regionProvince,
    regionStreet,
    regionWard,
  });
  const mapUrl = locationQuery
    ? `https://www.google.com/maps?q=${encodeURIComponent(locationQuery)}&output=embed`
    : null;

  return (
    <Dialog onOpenChange={onOpenChange} open={open}>
      <DialogContent className="w-[min(94vw,880px)] max-w-[880px] p-0">
        <div className="flex items-start justify-between gap-4 border-b border-line-soft px-5 py-4">
          <div className="min-w-0">
            <DialogTitle className="truncate text-lg font-black text-espresso">
              {cafeName}
            </DialogTitle>
            <DialogDescription className="mt-1 text-sm text-coffee-muted">
              {locationQuery || "Cafe location"}
            </DialogDescription>
          </div>
        </div>

        {mapUrl ? (
          <iframe
            className="h-[420px] w-full border-0"
            loading="lazy"
            referrerPolicy="no-referrer-when-downgrade"
            src={mapUrl}
            title={`${cafeName} location map`}
          />
        ) : (
          <div className="grid min-h-[220px] place-items-center px-6 py-12 text-center">
            <p className="max-w-md text-sm font-semibold leading-6 text-coffee-muted">
              Location details are not available for this cafe yet.
            </p>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
