"use client";

import {
  useEffect,
  useRef,
  useState,
  type ChangeEvent,
} from "react";
import { useRouter } from "next/navigation";
import { Camera, ChevronDownIcon, ImagePlus } from "lucide-react";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { AvatarImage } from "@/components/ui/avatar-image";
import { Button } from "@/components/ui/button";
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { SearchableDropdown } from "@/components/ui/searchable-dropdown";
import { Textarea } from "@/components/ui/textarea";
import { useCurrentUser } from "@/hooks/use-current-user";
import { ApiError } from "@/lib/api/client";
import {
  createCafePage,
  getCafePagesByOwnerId,
  updateCafePage,
} from "@/lib/api/cafes";
import { uploadCafeImageToCloudinary } from "@/lib/api/cloudinary";
import {
  getRegionProvinces,
  getRegionCities,
  getRegionWards,
  type RegionProvinceResponse,
  type RegionCityResponse,
  type RegionWardResponse,
} from "@/lib/api/regions";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import { cn } from "@/lib/utils";
import type { CafePageResponse } from "@/types/cafe";

type FormStatus = {
  error: string | null;
  success: string | null;
};

type RegionState = {
  provinceCode: string;
  province: string;
  cityCode: string;
  city: string;
  wardCode: string;
  ward: string;
  area: string;
  street: string;
};

const emptyStatus: FormStatus = { error: null, success: null };

const MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
const DEFAULT_COVER_IMAGE = "/images/cafes/velvet-roast/minimal-interior.jpg";
const BACKEND_DEFAULT_ADDRESS = "Pending update";

function getSubmitErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) return error.message;
  return fallback;
}

function buildAddress(region: RegionState) {
  return [region.street, region.ward, region.area, region.city || region.province]
    .map((part) => part.trim())
    .filter(Boolean)
    .join(", ");
}

function isUsableAddress(address: string | null | undefined) {
  const normalized = address?.trim();
  return Boolean(normalized && normalized !== BACKEND_DEFAULT_ADDRESS);
}

// ─── Section card ────────────────────────────────────────────────────────────

function SectionCard({ children }: { children: React.ReactNode }) {
  return (
    <div className="overflow-hidden rounded-md border border-border bg-surface">
      {children}
    </div>
  );
}

// ─── Collapsible trigger row ─────────────────────────────────────────────────

type CollapsibleTriggerProps = {
  isOpen: boolean;
  label: string;
  preview: string;
  previewEmpty?: string;
  onToggle: () => void;
};

function CollapsibleTrigger({
  isOpen,
  label,
  preview,
  previewEmpty = "Not set yet",
  onToggle,
}: CollapsibleTriggerProps) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className={cn(
        "flex w-full items-start gap-3 p-4 text-left transition-colors hover:bg-surface-muted sm:p-5",
        isOpen && "border-b border-border",
      )}
    >
      <div className="min-w-0 flex-1">
        <p className="text-xs font-medium text-muted">{label}</p>
        <p className="mt-0.5 truncate text-sm font-semibold text-foreground">
          {preview || <span className="font-normal italic text-muted">{previewEmpty}</span>}
        </p>
      </div>
      <div className="flex shrink-0 items-center gap-1 text-xs font-semibold text-primary">
        <span>{isOpen ? "Collapse" : "Edit"}</span>
        <ChevronDownIcon
          className={cn("size-4 transition-transform duration-300", isOpen && "rotate-180")}
        />
      </div>
    </button>
  );
}

// ─── Status message ──────────────────────────────────────────────────────────

function StatusMessage({ status }: { status: FormStatus }) {
  if (status.error) return <FieldError>{status.error}</FieldError>;
  if (status.success) {
    return (
      <p className="text-sm font-medium text-primary" role="status">
        {status.success}
      </p>
    );
  }
  return null;
}

// ─── Main component ───────────────────────────────────────────────────────────

export function EditCafePageForm() {
  const router = useRouter();
  const { user, isLoading: isUserLoading } = useCurrentUser();
  const avatarInputRef = useRef<HTMLInputElement | null>(null);
  const coverInputRef = useRef<HTMLInputElement | null>(null);

  const [ownedCafePage, setOwnedCafePage] = useState<CafePageResponse | null>(null);
  const [isLoadingCafePage, setIsLoadingCafePage] = useState(false);

  // Images
  const [avatarUrl, setAvatarUrl] = useState("");
  const [coverUrl, setCoverUrl] = useState("");
  const [selectedAvatarFile, setSelectedAvatarFile] = useState<File | null>(null);
  const [selectedCoverFile, setSelectedCoverFile] = useState<File | null>(null);
  const [selectedAvatarName, setSelectedAvatarName] = useState("");
  const [selectedCoverName, setSelectedCoverName] = useState("");

  // Basic info
  const [basicInfo, setBasicInfo] = useState({ name: "", description: "" });

  // Region
  const [region, setRegion] = useState<RegionState>({
    provinceCode: "", province: "", cityCode: "", city: "",
    wardCode: "", ward: "", area: "", street: "",
  });
  const [provinceOptions, setProvinceOptions] = useState<RegionProvinceResponse[]>([]);
  const [cityOptions, setCityOptions] = useState<RegionCityResponse[]>([]);
  const [wardOptions, setWardOptions] = useState<RegionWardResponse[]>([]);
  const [isProvinceLoading, setIsProvinceLoading] = useState(true);
  const [isCityLoading, setIsCityLoading] = useState(false);
  const [isWardLoading, setIsWardLoading] = useState(false);
  const [addressDataError, setAddressDataError] = useState<string | null>(null);

  // Collapsible
  const [isNameFormOpen, setIsNameFormOpen] = useState(false);
  const [isRegionFormOpen, setIsRegionFormOpen] = useState(false);

  // Per-section loading
  const [isSubmittingImages, setIsSubmittingImages] = useState(false);
  const [isSubmittingName, setIsSubmittingName] = useState(false);
  const [isTogglingStatus, setIsTogglingStatus] = useState(false);
  const [isSubmittingRegion, setIsSubmittingRegion] = useState(false);
  const [isCreating, setIsCreating] = useState(false);

  // Per-section status messages
  const [imagesStatus, setImagesStatus] = useState<FormStatus>(emptyStatus);
  const [nameStatus, setNameStatus] = useState<FormStatus>(emptyStatus);
  const [statusRowStatus, setStatusRowStatus] = useState<FormStatus>(emptyStatus);
  const [regionStatus, setRegionStatus] = useState<FormStatus>(emptyStatus);
  const [createStatus, setCreateStatus] = useState<FormStatus>(emptyStatus);

  const isCurrentUserUnavailable = !isUserLoading && !user;
  const editMode = !!ownedCafePage;
  const isActive = ownedCafePage?.status === "ACTIVE";
  const isAnySectionBusy =
    isSubmittingImages || isSubmittingName || isTogglingStatus || isSubmittingRegion || isCreating;

  // ── Auth redirect ──────────────────────────────────────────────────────────

  useEffect(() => {
    if (isCurrentUserUnavailable) router.replace("/login");
  }, [isCurrentUserUnavailable, router]);

  // ── Load owned cafe ────────────────────────────────────────────────────────

  useEffect(() => {
    if (isUserLoading || !user) return;

    let isMounted = true;
    const ownerUserId = user.userId;

    async function loadOwnedCafePage() {
      setIsLoadingCafePage(true);
      try {
        const cafes = await getCafePagesByOwnerId(ownerUserId);
        if (isMounted) setOwnedCafePage(cafes[0] ?? null);
      } catch (error) {
        if (isMounted)
          setCreateStatus({
            error: getSubmitErrorMessage(error, "Unable to load your cafe page."),
            success: null,
          });
      } finally {
        if (isMounted) setIsLoadingCafePage(false);
      }
    }

    void loadOwnedCafePage();
    return () => { isMounted = false; };
  }, [isUserLoading, user]);

  // ── Populate from loaded data ──────────────────────────────────────────────

  useEffect(() => {
    if (isLoadingCafePage) return;
    setBasicInfo({
      name: ownedCafePage?.name ?? "",
      description: ownedCafePage?.description ?? "",
    });
    setAvatarUrl(ownedCafePage?.avatarUrl?.trim() ?? "");
    setCoverUrl(ownedCafePage?.coverUrl?.trim() ?? "");
    setSelectedAvatarFile(null);
    setSelectedCoverFile(null);
    setSelectedAvatarName("");
    setSelectedCoverName("");
  }, [isLoadingCafePage, ownedCafePage]);

  useEffect(() => {
    if (isLoadingCafePage) return;
    setRegion({
      provinceCode: ownedCafePage?.regionProvinceCode ?? "",
      province: ownedCafePage?.regionProvince ?? "",
      cityCode: ownedCafePage?.regionCityCode ?? "",
      city: ownedCafePage?.regionCity ?? "",
      wardCode: ownedCafePage?.regionWardCode ?? "",
      ward: ownedCafePage?.regionWard ?? "",
      area: ownedCafePage?.regionArea ?? "",
      street:
        ownedCafePage?.regionStreet ??
        (isUsableAddress(ownedCafePage?.address) ? ownedCafePage?.address ?? "" : ""),
    });
  }, [isLoadingCafePage, ownedCafePage]);

  // Open sections by default when creating
  useEffect(() => {
    if (!isLoadingCafePage && !ownedCafePage) {
      setIsNameFormOpen(true);
      setIsRegionFormOpen(true);
    }
  }, [isLoadingCafePage, ownedCafePage]);

  // ── Region cascades ────────────────────────────────────────────────────────

  useEffect(() => {
    let isMounted = true;
    setIsProvinceLoading(true);
    setAddressDataError(null);
    getRegionProvinces()
      .then((p) => { if (isMounted) setProvinceOptions(p); })
      .catch(() => { if (isMounted) setAddressDataError("Unable to load Vietnam address data."); })
      .finally(() => { if (isMounted) setIsProvinceLoading(false); });
    return () => { isMounted = false; };
  }, []);

  useEffect(() => {
    if (!region.provinceCode) { setCityOptions([]); return; }
    let isMounted = true;
    setIsCityLoading(true);
    getRegionCities(region.provinceCode)
      .then((c) => { if (isMounted) setCityOptions(c); })
      .catch(() => { if (isMounted) setAddressDataError("Unable to load city data."); })
      .finally(() => { if (isMounted) setIsCityLoading(false); });
    return () => { isMounted = false; };
  }, [region.provinceCode]);

  useEffect(() => {
    if (!region.cityCode || !region.provinceCode) { setWardOptions([]); return; }
    let isMounted = true;
    setIsWardLoading(true);
    getRegionWards({ cityCode: region.cityCode, provinceCode: region.provinceCode })
      .then((w) => { if (isMounted) setWardOptions(w); })
      .catch(() => { if (isMounted) setAddressDataError("Unable to load ward data."); })
      .finally(() => { if (isMounted) setIsWardLoading(false); });
    return () => { isMounted = false; };
  }, [region.cityCode, region.provinceCode]);

  // ── Blob URL cleanup ───────────────────────────────────────────────────────

  useEffect(() => {
    return () => {
      if (avatarUrl.startsWith("blob:")) URL.revokeObjectURL(avatarUrl);
      if (coverUrl.startsWith("blob:")) URL.revokeObjectURL(coverUrl);
    };
  }, [avatarUrl, coverUrl]);

  // ── Image file handler ─────────────────────────────────────────────────────

  function changeImageFile(event: ChangeEvent<HTMLInputElement>, imageType: "avatar" | "cover") {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      setImagesStatus({ error: "Please choose an image file.", success: null });
      event.target.value = "";
      return;
    }
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      setImagesStatus({ error: "Cafe images must be 5MB or smaller.", success: null });
      event.target.value = "";
      return;
    }
    const nextUrl = URL.createObjectURL(file);
    if (imageType === "avatar") {
      setAvatarUrl((cur) => { if (cur.startsWith("blob:")) URL.revokeObjectURL(cur); return nextUrl; });
      setSelectedAvatarFile(file);
      setSelectedAvatarName(file.name);
    } else {
      setCoverUrl((cur) => { if (cur.startsWith("blob:")) URL.revokeObjectURL(cur); return nextUrl; });
      setSelectedCoverFile(file);
      setSelectedCoverName(file.name);
    }
    setImagesStatus(emptyStatus);
  }

  // ── Section save handlers ──────────────────────────────────────────────────

  async function submitImages() {
    if (!editMode) return;
    setImagesStatus(emptyStatus);
    setIsSubmittingImages(true);
    try {
      const [uploadedAvatarUrl, uploadedCoverUrl] = await Promise.all([
        selectedAvatarFile
          ? uploadCafeImageToCloudinary(selectedAvatarFile, "avatar")
          : Promise.resolve(ownedCafePage?.avatarUrl ?? undefined),
        selectedCoverFile
          ? uploadCafeImageToCloudinary(selectedCoverFile, "cover")
          : Promise.resolve(ownedCafePage?.coverUrl ?? undefined),
      ]);
      const updated = await updateCafePage(ownedCafePage!.id, {
        avatarUrl: uploadedAvatarUrl || undefined,
        coverUrl: uploadedCoverUrl || undefined,
      });
      setOwnedCafePage(updated);
      setSelectedAvatarFile(null);
      setSelectedCoverFile(null);
      setSelectedAvatarName("");
      setSelectedCoverName("");
      setImagesStatus({ error: null, success: "Images saved." });
    } catch (error) {
      setImagesStatus({ error: getSubmitErrorMessage(error, "Unable to save images."), success: null });
    } finally {
      setIsSubmittingImages(false);
    }
  }

  async function submitNameDesc() {
    const name = basicInfo.name.trim();
    if (!name) {
      setNameStatus({ error: "Cafe name is required.", success: null });
      return;
    }
    setNameStatus(emptyStatus);
    setIsSubmittingName(true);
    try {
      const updated = await updateCafePage(ownedCafePage!.id, {
        name,
        description: basicInfo.description.trim() || undefined,
      });
      setOwnedCafePage(updated);
      setNameStatus({ error: null, success: "Saved." });
      setIsNameFormOpen(false);
    } catch (error) {
      setNameStatus({ error: getSubmitErrorMessage(error, "Unable to save."), success: null });
    } finally {
      setIsSubmittingName(false);
    }
  }

  async function toggleStatus() {
    if (!editMode || isTogglingStatus) return;
    setIsTogglingStatus(true);
    setStatusRowStatus(emptyStatus);
    try {
      const updated = await updateCafePage(ownedCafePage!.id, {
        status: isActive ? "DRAFT" : "ACTIVE",
      });
      setOwnedCafePage(updated);
    } catch (error) {
      setStatusRowStatus({
        error: getSubmitErrorMessage(error, "Unable to update status."),
        success: null,
      });
    } finally {
      setIsTogglingStatus(false);
    }
  }

  async function submitRegion() {
    if (!region.provinceCode || !region.cityCode || !region.wardCode) {
      setRegionStatus({ error: "Province, city and ward are required.", success: null });
      return;
    }
    const address = buildAddress(region);
    if (!address) {
      setRegionStatus({ error: "Street address is required.", success: null });
      return;
    }
    setRegionStatus(emptyStatus);
    setIsSubmittingRegion(true);
    try {
      const updated = await updateCafePage(ownedCafePage!.id, {
        address,
        regionId: ownedCafePage!.regionId ?? undefined,
      });
      setOwnedCafePage(updated);
      setRegionStatus({ error: null, success: "Location saved." });
      setIsRegionFormOpen(false);
    } catch (error) {
      setRegionStatus({ error: getSubmitErrorMessage(error, "Unable to save location."), success: null });
    } finally {
      setIsSubmittingRegion(false);
    }
  }

  async function submitCreateCafe() {
    const name = basicInfo.name.trim();
    const address = buildAddress(region);

    if (!name) {
      setNameStatus({ error: "Cafe name is required.", success: null });
      setIsNameFormOpen(true);
      return;
    }
    if (!region.provinceCode || !region.cityCode || !region.wardCode || !address) {
      setRegionStatus({ error: "Province, city, ward and street are required.", success: null });
      setIsRegionFormOpen(true);
      return;
    }

    setCreateStatus(emptyStatus);
    setIsCreating(true);
    try {
      const [uploadedAvatarUrl, uploadedCoverUrl] = await Promise.all([
        selectedAvatarFile
          ? uploadCafeImageToCloudinary(selectedAvatarFile, "avatar")
          : Promise.resolve(undefined),
        selectedCoverFile
          ? uploadCafeImageToCloudinary(selectedCoverFile, "cover")
          : Promise.resolve(undefined),
      ]);
      const savedCafe = await createCafePage({
        name,
        address,
        description: basicInfo.description.trim() || undefined,
        avatarUrl: uploadedAvatarUrl || undefined,
        coverUrl: uploadedCoverUrl || undefined,
      });
      setOwnedCafePage(savedCafe);
      router.push(`/cafes/${savedCafe.id}`);
      router.refresh();
    } catch (error) {
      setCreateStatus({
        error: getSubmitErrorMessage(error, "Unable to create cafe page."),
        success: null,
      });
    } finally {
      setIsCreating(false);
    }
  }

  // ── Redirect guard ─────────────────────────────────────────────────────────

  if (isCurrentUserUnavailable) {
    return (
      <p className="text-sm font-semibold text-muted" role="status">
        Redirecting...
      </p>
    );
  }

  const locationPreview = editMode
    ? ownedCafePage!.address || buildAddress(region)
    : buildAddress(region);

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <div className="space-y-4">
      <header className="space-y-2">
        <h1 className="text-2xl font-black text-foreground sm:text-3xl">
          {editMode ? "Edit cafe page" : "Create cafe page"}
        </h1>
        <p className="max-w-[560px] text-sm leading-6 text-muted">
          {editMode
            ? "Update your cafe profile. Each section saves independently."
            : "Set up your cafe page to get started."}
        </p>
      </header>

      {/* ── Section 1: Photos ─────────────────────────────────────────────── */}
      <SectionCard>
        <div className="space-y-5 p-4 sm:p-5">
          <p className="text-xs font-semibold uppercase tracking-wider text-muted">Photos</p>

          <Field>
            <FieldLabel>Cover image</FieldLabel>
            <div className="relative aspect-[16/7] overflow-hidden rounded-md border border-border bg-muted/15">
              <img
                alt=""
                className="size-full object-cover"
                src={coverUrl || DEFAULT_COVER_IMAGE}
              />
              <Button
                aria-label="Change cover image"
                className="absolute bottom-3 right-3"
                disabled={isAnySectionBusy}
                onClick={() => coverInputRef.current?.click()}
                type="button"
                variant="secondary"
              >
                <ImagePlus data-icon="inline-start" />
                Cover
              </Button>
              <Input
                accept="image/*"
                className="sr-only"
                onChange={(event) => changeImageFile(event, "cover")}
                ref={coverInputRef}
                type="file"
              />
            </div>
            <FieldDescription>
              {selectedCoverName || "Choose a wide image from your device."}
            </FieldDescription>
          </Field>

          <Field>
            <FieldLabel>Avatar</FieldLabel>
            <div className="grid gap-4 sm:grid-cols-[112px_minmax(0,1fr)] sm:items-start">
              <div className="relative size-24 shrink-0">
                <Avatar className="relative size-full overflow-hidden rounded-full">
                  <AvatarImage
                    alt=""
                    className="absolute inset-0 !size-full rounded-full object-cover !object-center"
                    src={avatarUrl || DEFAULT_AVATAR_IMAGE}
                  />
                  <AvatarFallback>CS</AvatarFallback>
                </Avatar>
                <button
                  aria-label="Change avatar"
                  className="absolute -bottom-1 -right-1 grid size-9 place-items-center rounded-full border-2 border-background bg-primary text-primary-foreground shadow-sm transition hover:bg-primary-strong disabled:cursor-not-allowed disabled:opacity-60"
                  disabled={isAnySectionBusy}
                  onClick={() => avatarInputRef.current?.click()}
                  type="button"
                >
                  <Camera />
                </button>
                <Input
                  accept="image/*"
                  className="sr-only"
                  onChange={(event) => changeImageFile(event, "avatar")}
                  ref={avatarInputRef}
                  type="file"
                />
              </div>
              <div className="flex flex-col gap-2">
                <Button
                  className="w-full sm:!w-fit"
                  disabled={isAnySectionBusy}
                  onClick={() => avatarInputRef.current?.click()}
                  type="button"
                  variant="outline"
                >
                  Change avatar
                </Button>
                <FieldDescription>
                  {selectedAvatarName || "Choose an image from your device."}
                </FieldDescription>
              </div>
            </div>
          </Field>

          <StatusMessage status={imagesStatus} />

          {editMode && (
            <Button
              disabled={isAnySectionBusy || isLoadingCafePage || (!selectedAvatarFile && !selectedCoverFile)}
              onClick={() => void submitImages()}
              type="button"
            >
              {isSubmittingImages ? "Saving..." : "Save images"}
            </Button>
          )}
        </div>
      </SectionCard>

      {/* ── Section 2: Name & Description ────────────────────────────────── */}
      <SectionCard>
        <CollapsibleTrigger
          isOpen={isNameFormOpen}
          label="Cafe identity"
          preview={basicInfo.name}
          previewEmpty="Add a name for your cafe"
          onToggle={() => {
            setIsNameFormOpen((o) => !o);
            setNameStatus(emptyStatus);
          }}
        />

        <div
          className={cn(
            "grid transition-[grid-template-rows] duration-300 ease-in-out",
            isNameFormOpen ? "grid-rows-[1fr]" : "grid-rows-[0fr]",
          )}
        >
          <div className="overflow-hidden">
            <div className="space-y-5 p-4 sm:p-5">
              <FieldGroup>
                <Field>
                  <FieldLabel htmlFor="cafeName">Cafe name</FieldLabel>
                  <Input
                    disabled={isAnySectionBusy || isLoadingCafePage}
                    id="cafeName"
                    name="cafeName"
                    onChange={(e) =>
                      setBasicInfo((cur) => ({ ...cur, name: e.target.value }))
                    }
                    value={basicInfo.name}
                  />
                </Field>
                <Field>
                  <FieldLabel htmlFor="description">Description</FieldLabel>
                  <Textarea
                    disabled={isAnySectionBusy || isLoadingCafePage}
                    id="description"
                    name="description"
                    onChange={(e) =>
                      setBasicInfo((cur) => ({ ...cur, description: e.target.value }))
                    }
                    value={basicInfo.description}
                  />
                </Field>
              </FieldGroup>

              <StatusMessage status={nameStatus} />

              {editMode && (
                <div className="flex flex-wrap items-center gap-3">
                  <Button
                    disabled={isAnySectionBusy || isLoadingCafePage}
                    onClick={() => void submitNameDesc()}
                    type="button"
                  >
                    {isSubmittingName ? "Saving..." : "Save changes"}
                  </Button>
                  <Button
                    disabled={isAnySectionBusy}
                    onClick={() => {
                      setIsNameFormOpen(false);
                      setNameStatus(emptyStatus);
                    }}
                    type="button"
                    variant="ghost"
                  >
                    Cancel
                  </Button>
                </div>
              )}
            </div>
          </div>
        </div>
      </SectionCard>

      {/* ── Section 3: Visibility toggle (edit mode only) ────────────────── */}
      {editMode && (
        <SectionCard>
          <div className="flex items-center justify-between gap-4 p-4 sm:p-5">
            <div className="min-w-0">
              <p className="text-sm font-semibold text-foreground">Open to visitors</p>
              <p className="mt-0.5 text-xs text-muted">
                {isActive
                  ? "Your page is live and visible to the community"
                  : "Your page is hidden — only you can see it"}
              </p>
            </div>
            <button
              aria-checked={isActive}
              aria-label="Toggle page visibility"
              className={cn(
                "relative inline-flex h-6 w-11 shrink-0 rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2",
                isActive ? "bg-primary" : "bg-border",
                isTogglingStatus && "cursor-not-allowed opacity-50",
              )}
              disabled={isTogglingStatus}
              onClick={() => void toggleStatus()}
              role="switch"
              type="button"
            >
              <span
                className={cn(
                  "pointer-events-none inline-block size-5 rounded-full bg-white shadow-sm transition-transform duration-200",
                  isActive ? "translate-x-5" : "translate-x-0",
                )}
              />
            </button>
          </div>
          {statusRowStatus.error && (
            <div className="border-t border-border px-4 pb-4 sm:px-5">
              <StatusMessage status={statusRowStatus} />
            </div>
          )}
        </SectionCard>
      )}

      {/* ── Section 4: Location ───────────────────────────────────────────── */}
      <SectionCard>
        <CollapsibleTrigger
          isOpen={isRegionFormOpen}
          label="Location"
          preview={locationPreview}
          previewEmpty="Add your cafe's address"
          onToggle={() => {
            setIsRegionFormOpen((o) => !o);
            setRegionStatus(emptyStatus);
          }}
        />

        <div
          className={cn(
            "grid transition-[grid-template-rows] duration-300 ease-in-out",
            isRegionFormOpen ? "grid-rows-[1fr]" : "grid-rows-[0fr]",
          )}
        >
          <div className="overflow-hidden">
            <div className="space-y-5 p-4 sm:p-5">
              <FieldGroup>
                <SearchableDropdown
                  disabled={isAnySectionBusy}
                  emptyLabel="No provinces found."
                  isLoading={isProvinceLoading}
                  label="Province / city"
                  labelClassName="text-sm font-medium normal-case tracking-normal text-foreground leading-none"
                  triggerClassName="h-12 bg-surface"
                  onSelect={(province) =>
                    setRegion((cur) => ({
                      ...cur,
                      provinceCode: province.provinceCode,
                      province: province.name,
                      cityCode: "", city: "", wardCode: "", ward: "",
                    }))
                  }
                  options={provinceOptions}
                  placeholder="Select province..."
                  selectedCode={region.provinceCode || null}
                  selectedName={region.province || null}
                  valueKey="provinceCode"
                />
                <SearchableDropdown
                  disabled={!region.provinceCode || isAnySectionBusy}
                  emptyLabel="No cities found for this province."
                  isLoading={isCityLoading}
                  label="City / district"
                  labelClassName="text-sm font-medium normal-case tracking-normal text-foreground leading-none"
                  triggerClassName="h-12 bg-surface"
                  onSelect={(city) =>
                    setRegion((cur) => ({
                      ...cur,
                      cityCode: city.cityCode,
                      city: city.name,
                      wardCode: "", ward: "",
                    }))
                  }
                  options={cityOptions}
                  placeholder="Select city / district..."
                  selectedCode={region.cityCode || null}
                  selectedName={region.city || null}
                  valueKey="cityCode"
                />
                <SearchableDropdown
                  disabled={!region.cityCode || isAnySectionBusy}
                  emptyLabel="No wards found for this city."
                  isLoading={isWardLoading}
                  label="Ward"
                  labelClassName="text-sm font-medium normal-case tracking-normal text-foreground leading-none"
                  triggerClassName="h-12 bg-surface"
                  onSelect={(ward) =>
                    setRegion((cur) => ({ ...cur, wardCode: ward.wardCode, ward: ward.name }))
                  }
                  options={wardOptions}
                  placeholder="Select ward..."
                  selectedCode={region.wardCode || null}
                  selectedName={region.ward || null}
                  valueKey="wardCode"
                />
                <Field>
                  <FieldLabel htmlFor="area">Area</FieldLabel>
                  <Input
                    disabled={isAnySectionBusy}
                    id="area"
                    name="area"
                    onChange={(e) =>
                      setRegion((cur) => ({ ...cur, area: e.target.value }))
                    }
                    value={region.area}
                  />
                  <FieldDescription>
                    Optional neighborhood or local landmark.
                  </FieldDescription>
                </Field>
                <Field>
                  <FieldLabel htmlFor="street">Street</FieldLabel>
                  <Input
                    disabled={isAnySectionBusy}
                    id="street"
                    name="street"
                    onChange={(e) =>
                      setRegion((cur) => ({ ...cur, street: e.target.value }))
                    }
                    placeholder="House number, street name, detailed address"
                    value={region.street}
                  />
                </Field>
              </FieldGroup>

              {addressDataError && <FieldError>{addressDataError}</FieldError>}
              <StatusMessage status={regionStatus} />

              {editMode && (
                <div className="flex flex-wrap items-center gap-3">
                  <Button
                    disabled={isAnySectionBusy || isLoadingCafePage}
                    onClick={() => void submitRegion()}
                    type="button"
                  >
                    {isSubmittingRegion ? "Saving..." : "Save location"}
                  </Button>
                  <Button
                    disabled={isAnySectionBusy}
                    onClick={() => {
                      setIsRegionFormOpen(false);
                      setRegionStatus(emptyStatus);
                    }}
                    type="button"
                    variant="ghost"
                  >
                    Cancel
                  </Button>
                </div>
              )}
            </div>
          </div>
        </div>
      </SectionCard>

      {/* ── Create mode submit ─────────────────────────────────────────────── */}
      {!editMode && (
        <div className="space-y-3">
          <StatusMessage status={createStatus} />
          <Button
            disabled={isCreating || isLoadingCafePage}
            onClick={() => void submitCreateCafe()}
            type="button"
          >
            {isCreating ? "Creating..." : "Create cafe page"}
          </Button>
        </div>
      )}
    </div>
  );
}
