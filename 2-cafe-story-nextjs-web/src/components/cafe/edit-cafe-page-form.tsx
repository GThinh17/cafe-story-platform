"use client";

import {
  useEffect,
  useRef,
  useState,
  type ChangeEvent,
  type FormEvent,
} from "react";
import { useRouter } from "next/navigation";
import { Camera, ImagePlus } from "lucide-react";
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

const emptyStatus: FormStatus = {
  error: null,
  success: null,
};

const MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
const DEFAULT_COVER_IMAGE = "/images/cafes/velvet-roast/minimal-interior.jpg";
const BACKEND_DEFAULT_ADDRESS = "Pending update";

function getSubmitErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) {
    return error.message;
  }

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

export function EditCafePageForm() {
  const router = useRouter();
  const { user, isLoading: isUserLoading } = useCurrentUser();
  const avatarInputRef = useRef<HTMLInputElement | null>(null);
  const coverInputRef = useRef<HTMLInputElement | null>(null);
  const [ownedCafePage, setOwnedCafePage] = useState<CafePageResponse | null>(null);
  const [isLoadingCafePage, setIsLoadingCafePage] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState("");
  const [coverUrl, setCoverUrl] = useState("");
  const [selectedAvatarFile, setSelectedAvatarFile] = useState<File | null>(null);
  const [selectedCoverFile, setSelectedCoverFile] = useState<File | null>(null);
  const [selectedAvatarName, setSelectedAvatarName] = useState("");
  const [selectedCoverName, setSelectedCoverName] = useState("");
  const [basicInfo, setBasicInfo] = useState({
    name: "",
    description: "",
  });
  const [region, setRegion] = useState<RegionState>({
    provinceCode: "",
    province: "",
    cityCode: "",
    city: "",
    wardCode: "",
    ward: "",
    area: "",
    street: "",
  });
  const [provinceOptions, setProvinceOptions] = useState<RegionProvinceResponse[]>([]);
  const [cityOptions, setCityOptions] = useState<RegionCityResponse[]>([]);
  const [wardOptions, setWardOptions] = useState<RegionWardResponse[]>([]);
  const [isProvinceLoading, setIsProvinceLoading] = useState(true);
  const [isCityLoading, setIsCityLoading] = useState(false);
  const [isWardLoading, setIsWardLoading] = useState(false);
  const [addressDataError, setAddressDataError] = useState<string | null>(null);
  const [formStatus, setFormStatus] = useState<FormStatus>(emptyStatus);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isCurrentUserUnavailable = !isUserLoading && !user;

  useEffect(() => {
    if (isCurrentUserUnavailable) {
      router.replace("/login");
    }
  }, [isCurrentUserUnavailable, router]);

  useEffect(() => {
    if (isUserLoading || !user) {
      return;
    }

    let isMounted = true;
    const ownerUserId = user.userId;

    async function loadOwnedCafePage() {
      setIsLoadingCafePage(true);
      setFormStatus(emptyStatus);

      try {
        const cafes = await getCafePagesByOwnerId(ownerUserId);

        if (isMounted) {
          setOwnedCafePage(cafes[0] ?? null);
        }
      } catch (error) {
        if (isMounted) {
          setFormStatus({
            error: getSubmitErrorMessage(error, "Unable to load your cafe page."),
            success: null,
          });
        }
      } finally {
        if (isMounted) {
          setIsLoadingCafePage(false);
        }
      }
    }

    void loadOwnedCafePage();

    return () => {
      isMounted = false;
    };
  }, [isUserLoading, user]);

  useEffect(() => {
    if (isLoadingCafePage) {
      return;
    }

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
    if (isLoadingCafePage) {
      return;
    }

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

  useEffect(() => {
    let isMounted = true;
    setIsProvinceLoading(true);
    setAddressDataError(null);

    getRegionProvinces()
      .then((provinces) => {
        if (isMounted) setProvinceOptions(provinces);
      })
      .catch(() => {
        if (isMounted) setAddressDataError("Unable to load Vietnam address data.");
      })
      .finally(() => {
        if (isMounted) setIsProvinceLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    if (!region.provinceCode) {
      setCityOptions([]);
      return;
    }

    let isMounted = true;
    setIsCityLoading(true);
    setAddressDataError(null);

    getRegionCities(region.provinceCode)
      .then((cities) => {
        if (isMounted) setCityOptions(cities);
      })
      .catch(() => {
        if (isMounted) setAddressDataError("Unable to load city data for this province.");
      })
      .finally(() => {
        if (isMounted) setIsCityLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [region.provinceCode]);

  useEffect(() => {
    if (!region.cityCode || !region.provinceCode) {
      setWardOptions([]);
      return;
    }

    let isMounted = true;
    setIsWardLoading(true);
    setAddressDataError(null);

    getRegionWards({ cityCode: region.cityCode, provinceCode: region.provinceCode })
      .then((wards) => {
        if (isMounted) setWardOptions(wards);
      })
      .catch(() => {
        if (isMounted) setAddressDataError("Unable to load ward data for this city.");
      })
      .finally(() => {
        if (isMounted) setIsWardLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [region.cityCode, region.provinceCode]);

  useEffect(() => {
    return () => {
      if (avatarUrl.startsWith("blob:")) {
        URL.revokeObjectURL(avatarUrl);
      }
      if (coverUrl.startsWith("blob:")) {
        URL.revokeObjectURL(coverUrl);
      }
    };
  }, [avatarUrl, coverUrl]);

  if (isCurrentUserUnavailable) {
    return (
      <p className="text-sm font-semibold text-muted" role="status">
        Redirecting...
      </p>
    );
  }

  function changeImageFile(
    event: ChangeEvent<HTMLInputElement>,
    imageType: "avatar" | "cover",
  ) {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    if (!file.type.startsWith("image/")) {
      setFormStatus({ error: "Please choose an image file.", success: null });
      event.target.value = "";
      return;
    }

    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      setFormStatus({
        error: "Cafe images must be 5MB or smaller.",
        success: null,
      });
      event.target.value = "";
      return;
    }

    const nextImageUrl = URL.createObjectURL(file);

    if (imageType === "avatar") {
      setAvatarUrl((currentAvatarUrl) => {
        if (currentAvatarUrl.startsWith("blob:")) {
          URL.revokeObjectURL(currentAvatarUrl);
        }

        return nextImageUrl;
      });
      setSelectedAvatarFile(file);
      setSelectedAvatarName(file.name);
    } else {
      setCoverUrl((currentCoverUrl) => {
        if (currentCoverUrl.startsWith("blob:")) {
          URL.revokeObjectURL(currentCoverUrl);
        }

        return nextImageUrl;
      });
      setSelectedCoverFile(file);
      setSelectedCoverName(file.name);
    }

    setFormStatus({ error: null, success: "Image preview updated." });
  }

  async function submitCafePage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormStatus(emptyStatus);

    const name = basicInfo.name.trim();
    const description = basicInfo.description.trim();
    const address = buildAddress(region);

    if (!name) {
      setFormStatus({ error: "Cafe name is required.", success: null });
      return;
    }

    if (!region.provinceCode || !region.wardCode) {
      setFormStatus({ error: "Province, city and ward are required.", success: null });
      return;
    }

    if (!address) {
      setFormStatus({ error: "Address is required.", success: null });
      return;
    }

    setIsSubmitting(true);

    try {
      const [uploadedAvatarUrl, uploadedCoverUrl] = await Promise.all([
        selectedAvatarFile
          ? uploadCafeImageToCloudinary(selectedAvatarFile, "avatar")
          : Promise.resolve(ownedCafePage?.avatarUrl ?? undefined),
        selectedCoverFile
          ? uploadCafeImageToCloudinary(selectedCoverFile, "cover")
          : Promise.resolve(ownedCafePage?.coverUrl ?? undefined),
      ]);

      const payload = {
        address,
        avatarUrl: uploadedAvatarUrl || undefined,
        coverUrl: uploadedCoverUrl || undefined,
        description: description || undefined,
        name,
        regionId: ownedCafePage?.regionId ?? undefined,
      };

      const savedCafePage = ownedCafePage
        ? await updateCafePage(ownedCafePage.id, payload)
        : await createCafePage(payload);

      setFormStatus({
        error: null,
        success: "Cafe page saved successfully.",
      });
      router.push(`/cafes/${savedCafePage.id}`);
      router.refresh();
    } catch (error) {
      setFormStatus({
        error: getSubmitErrorMessage(error, "Unable to save cafe page."),
        success: null,
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="space-y-6">
      <header className="space-y-2">
        <h1 className="text-2xl font-black text-foreground sm:text-3xl">
          Edit cafe page
        </h1>
        <p className="max-w-[560px] text-sm leading-6 text-muted">
          Complete your cafe profile after payment.
        </p>
      </header>

      <form
        className="space-y-5 rounded-md border border-border bg-surface p-4 sm:p-5"
        onSubmit={submitCafePage}
      >
        <FieldGroup>
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
                disabled={isSubmitting}
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
            <FieldLabel>Avatar image</FieldLabel>
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
                  disabled={isSubmitting}
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
                  disabled={isSubmitting}
                  onClick={() => avatarInputRef.current?.click()}
                  type="button"
                  variant="outline"
                >
                  Change Avatar
                </Button>
                <FieldDescription>
                  {selectedAvatarName || "Choose an image from your device."}
                </FieldDescription>
              </div>
            </div>
          </Field>

          <Field>
            <FieldLabel htmlFor="cafeName">Cafe name</FieldLabel>
            <Input
              disabled={isSubmitting || isLoadingCafePage}
              id="cafeName"
              name="cafeName"
              onChange={(event) =>
                setBasicInfo((current) => ({
                  ...current,
                  name: event.target.value,
                }))
              }
              value={basicInfo.name}
            />
          </Field>

          <Field>
            <FieldLabel htmlFor="description">Description</FieldLabel>
            <Textarea
              disabled={isSubmitting || isLoadingCafePage}
              id="description"
              name="description"
              onChange={(event) =>
                setBasicInfo((current) => ({
                  ...current,
                  description: event.target.value,
                }))
              }
              value={basicInfo.description}
            />
          </Field>
        </FieldGroup>

        <FieldGroup>
          <SearchableDropdown
            disabled={isSubmitting}
            emptyLabel="No provinces found."
            isLoading={isProvinceLoading}
            label="Province / city"
            labelClassName="text-sm font-medium normal-case tracking-normal text-foreground leading-none"
            triggerClassName="h-12 bg-surface"
            onSelect={(province) =>
              setRegion((current) => ({
                ...current,
                provinceCode: province.provinceCode,
                province: province.name,
                cityCode: "",
                city: "",
                wardCode: "",
                ward: "",
              }))
            }
            options={provinceOptions}
            placeholder="Select province..."
            selectedCode={region.provinceCode || null}
            selectedName={region.province || null}
            valueKey="provinceCode"
          />
          <SearchableDropdown
            disabled={!region.provinceCode || isSubmitting}
            emptyLabel="No cities found for this province."
            isLoading={isCityLoading}
            label="City / district"
            labelClassName="text-sm font-medium normal-case tracking-normal text-foreground leading-none"
            triggerClassName="h-12 bg-surface"
            onSelect={(city) =>
              setRegion((current) => ({
                ...current,
                cityCode: city.cityCode,
                city: city.name,
                wardCode: "",
                ward: "",
              }))
            }
            options={cityOptions}
            placeholder="Select city / district..."
            selectedCode={region.cityCode || null}
            selectedName={region.city || null}
            valueKey="cityCode"
          />
          <SearchableDropdown
            disabled={!region.cityCode || isSubmitting}
            emptyLabel="No wards found for this city."
            isLoading={isWardLoading}
            label="Ward"
            labelClassName="text-sm font-medium normal-case tracking-normal text-foreground leading-none"
            triggerClassName="h-12 bg-surface"
            onSelect={(ward) =>
              setRegion((current) => ({
                ...current,
                wardCode: ward.wardCode,
                ward: ward.name,
              }))
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
              disabled={isSubmitting}
              id="area"
              name="area"
              onChange={(event) =>
                setRegion((current) => ({
                  ...current,
                  area: event.target.value,
                }))
              }
              value={region.area}
            />
            <FieldDescription>
              Optional smaller area, neighborhood, or local landmark.
            </FieldDescription>
          </Field>
          <Field>
            <FieldLabel htmlFor="street">Street</FieldLabel>
            <Input
              disabled={isSubmitting}
              id="street"
              name="street"
              onChange={(event) =>
                setRegion((current) => ({
                  ...current,
                  street: event.target.value,
                }))
              }
              placeholder="House number, street name, detailed address"
              value={region.street}
            />
          </Field>
        </FieldGroup>

        {addressDataError ? <FieldError>{addressDataError}</FieldError> : null}
        <FormStatusMessage status={formStatus} />
        <Button disabled={isSubmitting || isLoadingCafePage} type="submit">
          {isSubmitting ? "Saving..." : ownedCafePage ? "Save cafe page" : "Create cafe page"}
        </Button>
      </form>
    </div>
  );
}

function FormStatusMessage({ status }: { status: FormStatus }) {
  if (status.error) {
    return <FieldError>{status.error}</FieldError>;
  }

  if (status.success) {
    return (
      <p className="text-sm font-medium text-primary" role="status">
        {status.success}
      </p>
    );
  }

  return null;
}
