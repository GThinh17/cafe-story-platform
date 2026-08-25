"use client";

import {
  useEffect,
  useRef,
  useState,
  type ChangeEvent,
  type FormEvent,
} from "react";
import { useRouter } from "next/navigation";
import { Camera } from "lucide-react";
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
import { SearchableDropdown } from "@/components/ui/searchable-dropdown";
import { Switch } from "@/components/ui/switch";
import { useI18n } from "@/components/providers/locale-provider";
import { LanguageSettings } from "@/components/settings/language-settings";
import { useCurrentUser } from "@/hooks/use-current-user";
import { uploadAvatarToCloudinary } from "@/lib/api/cloudinary";
import { ApiError } from "@/lib/api/client";
import { updateMe, updateMeRegion } from "@/lib/api/users";
import {
  getRegionProvinces,
  getRegionCities,
  getRegionWards,
  type RegionProvinceResponse,
  type RegionCityResponse,
  type RegionWardResponse,
} from "@/lib/api/regions";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";

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

const MAX_AVATAR_SIZE_BYTES = 5 * 1024 * 1024;

function getSubmitErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) {
    return error.message;
  }

  return fallback;
}

type EditProfileFormProps = {
  routeUsername?: string;
};

function normalizeUsername(username: string | null | undefined) {
  return username?.trim().toLowerCase() ?? "";
}

export function EditProfileForm({ routeUsername }: EditProfileFormProps) {
  const router = useRouter();
  const { t } = useI18n();
  const { user, isLoading: isUserLoading, refetch } = useCurrentUser();
  const [avatarUrl, setAvatarUrl] = useState("");
  const [selectedAvatarFile, setSelectedAvatarFile] = useState<File | null>(null);
  const [selectedAvatarName, setSelectedAvatarName] = useState("");
  const avatarInputRef = useRef<HTMLInputElement | null>(null);
  const [basicInfo, setBasicInfo] = useState({
    userFullName: "",
    userPhone: "",
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
  const [avatarStatus, setAvatarStatus] = useState<FormStatus>(emptyStatus);
  const [basicStatus, setBasicStatus] = useState<FormStatus>(emptyStatus);
  const [regionStatus, setRegionStatus] = useState<FormStatus>(emptyStatus);
  const [displayError, setDisplayError] = useState<string | null>(null);
  const [hideCafeOnProfile, setHideCafeOnProfile] = useState(false);
  const [isSavingDisplay, setIsSavingDisplay] = useState(false);
  const [submittingForm, setSubmittingForm] = useState<
    "avatar" | "basic" | "region" | null
  >(null);

  const profileHref = user?.userName ? `/${user.userName}` : "#";
  const normalizedRouteUsername = normalizeUsername(routeUsername);
  const normalizedCurrentUsername = normalizeUsername(user?.userName);
  const isCurrentUserUnavailable = !isUserLoading && !user;
  const isRouteMismatch = Boolean(
    !isUserLoading &&
      normalizedRouteUsername &&
      normalizedCurrentUsername &&
      normalizedRouteUsername !== normalizedCurrentUsername,
  );

  useEffect(() => {
    if (isCurrentUserUnavailable) {
      router.replace("/login");
      return;
    }

    if (!isRouteMismatch || !user?.userName) {
      return;
    }

    router.replace(`/${user.userName}/edit`);
  }, [isCurrentUserUnavailable, isRouteMismatch, router, user?.userName]);

  useEffect(() => {
    if (isUserLoading || !user) {
      return;
    }

    setBasicInfo({
      userFullName: user.userFullName ?? "",
      userPhone: user.userPhone === null ? "" : String(user.userPhone),
    });
    setAvatarUrl(user.userAvatar?.trim() ?? "");
    setSelectedAvatarFile(null);
    setSelectedAvatarName("");
    setHideCafeOnProfile(user.hideCafePageOnProfile === true);
  }, [isUserLoading, user]);

  useEffect(() => {
    if (isUserLoading || !user) {
      return;
    }

    setRegion({
      provinceCode: user.regionProvinceCode ?? "",
      province: user.regionProvince ?? "",
      cityCode: user.regionCityCode ?? "",
      city: user.regionCity ?? "",
      wardCode: user.regionWardCode ?? "",
      ward: user.regionWard ?? "",
      area: user.regionArea ?? "",
      street: user.regionStreet ?? "",
    });
  }, [isUserLoading, user]);

  useEffect(() => {
    let isMounted = true;
    setIsProvinceLoading(true);
    setAddressDataError(null);

    getRegionProvinces()
      .then((provinces) => {
        if (isMounted) setProvinceOptions(provinces);
      })
      .catch(() => {
        if (isMounted) setAddressDataError(t("profileEdit.address.loadProvincesError"));
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
        if (isMounted) setAddressDataError(t("profileEdit.address.loadCitiesError"));
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
        if (isMounted) setAddressDataError(t("profileEdit.address.loadWardsError"));
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
    };
  }, [avatarUrl]);

  if (isCurrentUserUnavailable || isRouteMismatch) {
    return (
      <p className="text-sm font-semibold text-muted" role="status">
        {t("common.redirecting")}
      </p>
    );
  }

  function changeAvatarFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    if (!file.type.startsWith("image/")) {
      setSelectedAvatarFile(null);
      setAvatarStatus({ error: t("profileEdit.avatar.notAnImage"), success: null });
      event.target.value = "";
      return;
    }

    if (file.size > MAX_AVATAR_SIZE_BYTES) {
      setSelectedAvatarFile(null);
      setAvatarStatus({
        error: t("profileEdit.avatar.tooLarge"),
        success: null,
      });
      event.target.value = "";
      return;
    }

    const nextAvatarUrl = URL.createObjectURL(file);

    setAvatarUrl((currentAvatarUrl) => {
      if (currentAvatarUrl.startsWith("blob:")) {
        URL.revokeObjectURL(currentAvatarUrl);
      }

      return nextAvatarUrl;
    });
    setSelectedAvatarFile(file);
    setSelectedAvatarName(file.name);
    setAvatarStatus({ error: null, success: t("profileEdit.avatar.previewUpdated") });
  }

  async function submitAvatar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setAvatarStatus(emptyStatus);

    if (!selectedAvatarFile) {
      setAvatarStatus({
        error: t("profileEdit.avatar.missing"),
        success: null,
      });
      return;
    }

    setSubmittingForm("avatar");

    try {
      const uploadedAvatarUrl = await uploadAvatarToCloudinary(selectedAvatarFile);
      const updatedUser = await updateMe({ userAvatar: uploadedAvatarUrl });
      await refetch();
      setAvatarUrl(updatedUser.userAvatar?.trim() ?? uploadedAvatarUrl);
      setSelectedAvatarFile(null);
      setSelectedAvatarName("");
      setAvatarStatus({
        error: null,
        success: t("profileEdit.avatar.success"),
      });
      router.refresh();
    } catch (error) {
      setAvatarStatus({
        error: getSubmitErrorMessage(error, t("profileEdit.avatar.error")),
        success: null,
      });
    } finally {
      setSubmittingForm(null);
    }
  }

  async function submitBasicInfo(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const phone = basicInfo.userPhone.trim();

    if (phone && !Number.isFinite(Number(phone))) {
      event.preventDefault();
      setBasicStatus({ error: t("profileEdit.basic.invalidPhone"), success: null });
      return;
    }

    setSubmittingForm("basic");
    setBasicStatus(emptyStatus);

    try {
      await updateMe({
        userFullName: basicInfo.userFullName.trim() || undefined,
        userPhone: phone ? Number(phone) : undefined,
      });
      await refetch();
      setBasicStatus({
        error: null,
        success: t("profileEdit.basic.success"),
      });
      router.refresh();
    } catch (error) {
      setBasicStatus({
        error: getSubmitErrorMessage(error, t("profileEdit.basic.error")),
        success: null,
      });
    } finally {
      setSubmittingForm(null);
    }
  }

  async function toggleHideCafeOnProfile(next: boolean) {
    const previous = hideCafeOnProfile;

    setHideCafeOnProfile(next);
    setDisplayError(null);
    setIsSavingDisplay(true);

    try {
      await updateMe({ hideCafePageOnProfile: next });
      await refetch();
      router.refresh();
    } catch (error) {
      setHideCafeOnProfile(previous);
      setDisplayError(
        getSubmitErrorMessage(error, t("profileEdit.display.error")),
      );
    } finally {
      setIsSavingDisplay(false);
    }
  }

  async function submitRegion(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!region.provinceCode || !region.wardCode) {
      setRegionStatus({
        error: t("profileEdit.address.required"),
        success: null,
      });
      return;
    }

    const area = region.area.trim();

    setSubmittingForm("region");
    setRegionStatus(emptyStatus);

    try {
      await updateMeRegion({
        cityCode: region.cityCode || undefined,
        city: region.city || undefined,
        provinceCode: region.provinceCode || undefined,
        province: region.province || undefined,
        wardCode: region.wardCode || undefined,
        ward: region.ward || undefined,
        area: area || undefined,
        district: area || undefined,
        street: region.street.trim() || undefined,
      });
      await refetch();
      setRegionStatus({
        error: null,
        success: t("profileEdit.address.success"),
      });
    } catch (error) {
      setRegionStatus({
        error: getSubmitErrorMessage(error, t("profileEdit.address.error")),
        success: null,
      });
    } finally {
      setSubmittingForm(null);
    }
  }

  return (
    <div className="space-y-6">
      <header className="space-y-2">
        <h1 className="text-2xl font-black text-foreground sm:text-3xl">
          {t("profileEdit.title")}
        </h1>
        <p className="max-w-[560px] text-sm leading-6 text-muted">
          {t("profileEdit.subtitle")}
        </p>
      </header>

      <form
        className="space-y-5 rounded-md border border-border bg-surface p-4 sm:p-5"
        onSubmit={submitAvatar}
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
                aria-label={t("profileEdit.avatar.change")}
                className="absolute -bottom-1 -right-1 z-10 grid size-9 place-items-center rounded-full border-2 border-background bg-primary text-primary-foreground shadow-sm transition hover:bg-primary-strong disabled:cursor-not-allowed disabled:opacity-60"
                disabled={submittingForm === "avatar"}
                onClick={() => avatarInputRef.current?.click()}
                type="button"
              >
                <Camera />
              </button>
              <Input
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
              <FieldLabel>{t("profileEdit.avatar.label")}</FieldLabel>
              <Button
                className="w-full sm:!w-fit"
                disabled={submittingForm === "avatar"}
                onClick={() => avatarInputRef.current?.click()}
                type="button"
                variant="outline"
              >
                {t("profileEdit.avatar.change")}
              </Button>
              <FieldDescription>
                {selectedAvatarName || t("profileEdit.avatar.hint")}
              </FieldDescription>
            </Field>
            <FormStatusMessage status={avatarStatus} />
            <Button
              className="w-full sm:w-fit"
              disabled={submittingForm === "avatar" || !selectedAvatarFile}
              type="submit"
            >
              {submittingForm === "avatar"
                ? t("common.saving")
                : t("profileEdit.avatar.save")}
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
            <FieldLabel htmlFor="userFullName">{t("profileEdit.basic.fullName")}</FieldLabel>
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
            <FieldLabel htmlFor="userPhone">{t("profileEdit.basic.phone")}</FieldLabel>
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
          {submittingForm === "basic"
            ? t("common.saving")
            : t("profileEdit.basic.save")}
        </Button>
      </form>

      <section className="space-y-5 rounded-md border border-border bg-surface p-4 sm:p-5">
        <div className="space-y-1">
          <h2 className="text-base font-bold text-foreground">
            {t("profileEdit.display.title")}
          </h2>
          <p className="text-xs text-muted">
            {t("profileEdit.display.description")}
          </p>
        </div>
        <label
          className="flex items-start justify-between gap-4"
          htmlFor="hideCafePageOnProfile"
        >
          <span className="flex flex-col gap-1">
            <span className="text-sm font-medium text-foreground">
              {t("profileEdit.display.hideCafe")}
            </span>
            <span className="text-xs text-muted">
              {t("profileEdit.display.hideCafeHint")}
              {isSavingDisplay ? ` ${t("common.saving")}` : null}
            </span>
          </span>
          <Switch
            checked={hideCafeOnProfile}
            disabled={isSavingDisplay}
            id="hideCafePageOnProfile"
            onCheckedChange={toggleHideCafeOnProfile}
          />
        </label>
        {displayError ? <FieldError>{displayError}</FieldError> : null}
      </section>

      <LanguageSettings />

      <form
        className="space-y-5 rounded-md border border-border bg-surface p-4 sm:p-5"
        onSubmit={submitRegion}
      >
        <FieldGroup>
          <SearchableDropdown
            disabled={submittingForm === "region"}
            emptyLabel={t("profileEdit.address.provinceEmpty")}
            isLoading={isProvinceLoading}
            label={t("profileEdit.address.province")}
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
            placeholder={t("profileEdit.address.provincePlaceholder")}
            selectedCode={region.provinceCode || null}
            selectedName={region.province || null}
            valueKey="provinceCode"
          />
          <SearchableDropdown
            disabled={!region.provinceCode || submittingForm === "region"}
            emptyLabel={t("profileEdit.address.cityEmpty")}
            isLoading={isCityLoading}
            label={t("profileEdit.address.city")}
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
            placeholder={t("profileEdit.address.cityPlaceholder")}
            selectedCode={region.cityCode || null}
            selectedName={region.city || null}
            valueKey="cityCode"
          />
          <SearchableDropdown
            disabled={!region.cityCode || submittingForm === "region"}
            emptyLabel={t("profileEdit.address.wardEmpty")}
            isLoading={isWardLoading}
            label={t("profileEdit.address.ward")}
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
            placeholder={t("profileEdit.address.wardPlaceholder")}
            selectedCode={region.wardCode || null}
            selectedName={region.ward || null}
            valueKey="wardCode"
          />
          <Field>
            <FieldLabel htmlFor="area">{t("profileEdit.address.area")}</FieldLabel>
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
            <FieldDescription>
              {t("profileEdit.address.areaHint")}
            </FieldDescription>
          </Field>
          <Field>
            <FieldLabel htmlFor="street">{t("profileEdit.address.street")}</FieldLabel>
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
              placeholder={t("profileEdit.address.streetPlaceholder")}
              value={region.street}
            />
          </Field>
        </FieldGroup>
        {addressDataError ? <FieldError>{addressDataError}</FieldError> : null}
        <FormStatusMessage status={regionStatus} />
        <Button disabled={submittingForm === "region"} type="submit">
          {submittingForm === "region"
            ? t("common.saving")
            : t("profileEdit.address.save")}
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
