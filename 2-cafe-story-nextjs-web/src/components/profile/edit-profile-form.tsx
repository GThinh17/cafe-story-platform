"use client";

import {
  useEffect,
  useMemo,
  useRef,
  useState,
  type ChangeEvent,
  type FormEvent,
} from "react";
import Link from "next/link";
import { Camera, ChevronLeft } from "lucide-react";
import { AvatarImage } from "@/components/ui/avatar-image";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";

type FormStatus = {
  error: string | null;
  success: string | null;
};

type RegionState = {
  provinceId: string;
  province: string;
  ward: string;
  area: string;
  street: string;
};

type ProvinceOption = {
  idProvince: string;
  name: string;
};

type WardOption = {
  idProvince: string;
  idWard: string;
  name: string;
};

const emptyStatus: FormStatus = {
  error: null,
  success: null,
};

function getCityName(province: string) {
  return province
    .replace(/^Thành phố\s+/i, "")
    .replace(/^Tỉnh\s+/i, "")
    .trim();
}

export function EditProfileForm() {
  const [avatarUrl, setAvatarUrl] = useState("");
  const [selectedAvatarName, setSelectedAvatarName] = useState("");
  const avatarInputRef = useRef<HTMLInputElement | null>(null);
  const [basicInfo, setBasicInfo] = useState({
    userFullName: "",
    userPhone: "",
  });
  const [region, setRegion] = useState<RegionState>({
    provinceId: "",
    province: "",
    ward: "",
    area: "",
    street: "",
  });
  const [provinceOptions, setProvinceOptions] = useState<ProvinceOption[]>([]);
  const [wardOptions, setWardOptions] = useState<WardOption[]>([]);
  const [isProvinceLoading, setIsProvinceLoading] = useState(true);
  const [isWardLoading, setIsWardLoading] = useState(false);
  const [addressDataError, setAddressDataError] = useState<string | null>(null);
  const [avatarStatus, setAvatarStatus] = useState<FormStatus>(emptyStatus);
  const [basicStatus, setBasicStatus] = useState<FormStatus>(emptyStatus);
  const [regionStatus, setRegionStatus] = useState<FormStatus>(emptyStatus);
  const [submittingForm, setSubmittingForm] = useState<
    "avatar" | "basic" | "region" | null
  >(null);

  const selectedProvince = useMemo(
    () =>
      provinceOptions.find(
        (province) => province.idProvince === region.provinceId,
      ),
    [provinceOptions, region.provinceId],
  );

  useEffect(() => {
    let isMounted = true;

    async function loadProvinces() {
      setIsProvinceLoading(true);
      setAddressDataError(null);

      try {
        const { getAllProvincesSorted } = await import("new-vn-provinces/provinces");
        const provinces = await getAllProvincesSorted();

        if (isMounted) {
          setProvinceOptions(provinces);
        }
      } catch {
        if (isMounted) {
          setAddressDataError("Unable to load Vietnam address data.");
        }
      } finally {
        if (isMounted) {
          setIsProvinceLoading(false);
        }
      }
    }

    void loadProvinces();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    if (!region.provinceId) {
      setWardOptions([]);
      return;
    }

    let isMounted = true;

    async function loadWards() {
      setIsWardLoading(true);
      setAddressDataError(null);

      try {
        const { getWardsByProvinceId } = await import("new-vn-provinces/provinces");
        const wards = await getWardsByProvinceId(region.provinceId);

        if (isMounted) {
          setWardOptions(wards);
        }
      } catch {
        if (isMounted) {
          setAddressDataError("Unable to load ward data for this province.");
        }
      } finally {
        if (isMounted) {
          setIsWardLoading(false);
        }
      }
    }

    void loadWards();

    return () => {
      isMounted = false;
    };
  }, [region.provinceId]);

  useEffect(() => {
    return () => {
      if (avatarUrl.startsWith("blob:")) {
        URL.revokeObjectURL(avatarUrl);
      }
    };
  }, [avatarUrl]);

  function completeUiSubmit(
    event: FormEvent<HTMLFormElement>,
    form: "avatar" | "basic" | "region",
    setStatus: (status: FormStatus) => void,
    success: string,
  ) {
    event.preventDefault();
    setSubmittingForm(form);
    setStatus(emptyStatus);

    window.setTimeout(() => {
      setStatus({ error: null, success });
      setSubmittingForm(null);
    }, 300);
  }

  function changeAvatarFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    if (!file.type.startsWith("image/")) {
      setAvatarStatus({ error: "Please choose an image file.", success: null });
      return;
    }

    const nextAvatarUrl = URL.createObjectURL(file);

    setAvatarUrl((currentAvatarUrl) => {
      if (currentAvatarUrl.startsWith("blob:")) {
        URL.revokeObjectURL(currentAvatarUrl);
      }

      return nextAvatarUrl;
    });
    setSelectedAvatarName(file.name);
    setAvatarStatus({ error: null, success: "Avatar preview updated." });
  }

  function submitBasicInfo(event: FormEvent<HTMLFormElement>) {
    if (basicInfo.userPhone.trim() && !Number.isFinite(Number(basicInfo.userPhone))) {
      event.preventDefault();
      setBasicStatus({ error: "Phone must be a valid number.", success: null });
      return;
    }

    completeUiSubmit(event, "basic", setBasicStatus, "Basic info ready.");
  }

  function submitRegion(event: FormEvent<HTMLFormElement>) {
    if (!selectedProvince?.name || !region.ward) {
      event.preventDefault();
      setRegionStatus({
        error: "Province and ward are required.",
        success: null,
      });
      return;
    }

    completeUiSubmit(event, "region", setRegionStatus, "Address ready.");
  }

  return (
    <div className="space-y-6">
      <Button asChild className="w-fit" size="sm" variant="ghost">
        <Link href="/profile">
          <ChevronLeft />
          Profile
        </Link>
      </Button>

      <header className="space-y-2">
        <h1 className="text-2xl font-black text-foreground sm:text-3xl">
          Edit profile
        </h1>
        <p className="max-w-[560px] text-sm leading-6 text-muted">
          Update your public profile details and Vietnam address.
        </p>
      </header>

      <form
        className="space-y-5 rounded-md border border-border bg-surface p-4 sm:p-5"
        onSubmit={(event) =>
          completeUiSubmit(event, "avatar", setAvatarStatus, "Avatar ready.")
        }
      >
        <div className="grid gap-4 sm:grid-cols-[112px_minmax(0,1fr)] sm:items-start">
          <div className="flex justify-center sm:justify-start">
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
                className="absolute -bottom-1 -right-1 z-10 grid size-9 place-items-center rounded-full border-2 border-background bg-primary text-primary-foreground shadow-sm transition hover:bg-primary-strong disabled:cursor-not-allowed disabled:opacity-60"
                disabled={submittingForm === "avatar"}
                onClick={() => avatarInputRef.current?.click()}
                type="button"
              >
                <Camera />
              </button>
              <input
                accept="image/*"
                className="sr-only"
                onChange={changeAvatarFile}
                ref={avatarInputRef}
                type="file"
              />
            </div>
          </div>
          <FieldGroup className="min-w-0">
            <Field>
              <FieldLabel>Avatar image</FieldLabel>
              <Button
                className="w-full sm:!w-fit"
                disabled={submittingForm === "avatar"}
                onClick={() => avatarInputRef.current?.click()}
                type="button"
                variant="outline"
              >
                Change Avatar
              </Button>
              <FieldDescription>
                {selectedAvatarName || "Choose an image from your device."}
              </FieldDescription>
            </Field>
            <FormStatusMessage status={avatarStatus} />
            <Button
              className="w-full sm:w-fit"
              disabled={submittingForm === "avatar"}
              type="submit"
            >
              {submittingForm === "avatar" ? "Saving..." : "Save avatar"}
            </Button>
          </FieldGroup>
        </div>
      </form>

      <form
        className="space-y-5 rounded-md border border-border bg-surface p-4 sm:p-5"
        onSubmit={submitBasicInfo}
      >
        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="userFullName">Full name</FieldLabel>
            <Input
              disabled={submittingForm === "basic"}
              id="userFullName"
              name="userFullName"
              onChange={(event) =>
                setBasicInfo((current) => ({
                  ...current,
                  userFullName: event.target.value,
                }))
              }
              value={basicInfo.userFullName}
            />
          </Field>
          <Field>
            <FieldLabel htmlFor="userPhone">Phone</FieldLabel>
            <Input
              disabled={submittingForm === "basic"}
              id="userPhone"
              inputMode="numeric"
              name="userPhone"
              onChange={(event) =>
                setBasicInfo((current) => ({
                  ...current,
                  userPhone: event.target.value.replace(/\D/g, ""),
                }))
              }
              value={basicInfo.userPhone}
            />
          </Field>
        </FieldGroup>
        <FormStatusMessage status={basicStatus} />
        <Button disabled={submittingForm === "basic"} type="submit">
          {submittingForm === "basic" ? "Saving..." : "Save basic info"}
        </Button>
      </form>

      <form
        className="space-y-5 rounded-md border border-border bg-surface p-4 sm:p-5"
        onSubmit={submitRegion}
      >
        <FieldGroup>
          <Field>
            <FieldLabel>Province / city</FieldLabel>
            <Select
              disabled={isProvinceLoading || submittingForm === "region"}
              onValueChange={(provinceId) =>
                setRegion((current) => ({
                  ...current,
                  provinceId,
                  province:
                    provinceOptions.find(
                      (province) => province.idProvince === provinceId,
                    )?.name ?? "",
                  ward: "",
                  area: "",
                }))
              }
              value={region.provinceId}
            >
              <SelectTrigger className="h-12 w-full bg-surface">
                <SelectValue
                  placeholder={
                    isProvinceLoading ? "Loading provinces..." : "Select province"
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {provinceOptions.map((province) => (
                  <SelectItem key={province.idProvince} value={province.idProvince}>
                    {province.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </Field>
          <Field>
            <FieldLabel>Ward</FieldLabel>
            <Select
              disabled={
                !region.provinceId || isWardLoading || submittingForm === "region"
              }
              onValueChange={(ward) =>
                setRegion((current) => ({
                  ...current,
                  ward,
                  area: "",
                }))
              }
              value={region.ward}
            >
              <SelectTrigger className="h-12 w-full bg-surface">
                <SelectValue
                  placeholder={isWardLoading ? "Loading wards..." : "Select ward"}
                />
              </SelectTrigger>
              <SelectContent>
                {wardOptions.map((ward) => (
                  <SelectItem key={ward.idWard} value={ward.name}>
                    {ward.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </Field>
          <Field>
            <FieldLabel htmlFor="city">City</FieldLabel>
            <Input
              id="city"
              name="city"
              readOnly
              value={selectedProvince ? getCityName(selectedProvince.name) : ""}
            />
            <FieldDescription>
              Derived from the selected Vietnam province or centrally governed city.
            </FieldDescription>
          </Field>
          <Field>
            <FieldLabel htmlFor="area">Area</FieldLabel>
            <Input
              disabled={submittingForm === "region"}
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
            {/* Vietnam's post-2025 dataset exposes province/city and ward/commune only, so area remains optional free text. */}
            <FieldDescription>
              Optional smaller area, neighborhood, or local landmark.
            </FieldDescription>
          </Field>
          <Field>
            <FieldLabel htmlFor="street">Street</FieldLabel>
            <Input
              disabled={submittingForm === "region"}
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
        <FormStatusMessage status={regionStatus} />
        <Button disabled={submittingForm === "region"} type="submit">
          {submittingForm === "region" ? "Saving..." : "Save address"}
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
