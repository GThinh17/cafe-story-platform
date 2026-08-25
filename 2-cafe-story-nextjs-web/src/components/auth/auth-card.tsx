"use client";

import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Field,
  FieldContent,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { SearchableDropdown } from "@/components/ui/searchable-dropdown";
import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, useEffect, useRef, useState } from "react";
import { ApiError } from "@/lib/api/client";
import { login, register, suggestUserNames } from "@/lib/api/auth";
import { updateMeRegion, type UpdateMeRegionRequest } from "@/lib/api/users";
import {
  getRegionProvinces,
  getRegionCities,
  getRegionWards,
  type RegionProvinceResponse,
  type RegionCityResponse,
  type RegionWardResponse,
} from "@/lib/api/regions";
import type { AuthField, AuthFormCopy, AuthMode } from "@/types/auth";
import { cn } from "@/lib/utils";
import { useAuth } from "@/components/providers/auth-provider";
import { useI18n } from "@/components/providers/locale-provider";
import type { Translate } from "@/lib/i18n";

type AuthCardProps = {
  mode: AuthMode;
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

const emptyRegion: RegionState = {
  provinceCode: "",
  province: "",
  cityCode: "",
  city: "",
  wardCode: "",
  ward: "",
  area: "",
  street: "",
};

function getAuthCopy(t: Translate): Record<AuthMode, AuthFormCopy> {
  return {
    login: {
      eyebrow: "",
      title: t("auth.login.title"),
      description: t("auth.login.description"),
      submitLabel: t("auth.login.submit"),
      switchPrompt: t("auth.login.switchPrompt"),
      switchHref: "/register",
      switchLabel: t("auth.login.switchLabel"),
    },
    register: {
      eyebrow: "Cafe Story",
      title: t("auth.register.title"),
      description: t("auth.register.description"),
      submitLabel: t("auth.register.submit"),
      switchPrompt: t("auth.register.switchPrompt"),
      switchHref: "/login",
      switchLabel: t("auth.register.switchLabel"),
    },
  };
}

function getSharedFields(t: Translate): AuthField[] {
  return [
    {
      autoComplete: "username",
      label: t("auth.field.identifier"),
      name: "identifier",
      placeholder: t("auth.field.identifierPlaceholder"),
      type: "text",
    },
    {
      autoComplete: "current-password",
      label: t("auth.field.password"),
      name: "password",
      placeholder: t("auth.field.passwordPlaceholder"),
      type: "password",
    },
  ];
}

function getRegisterFields(t: Translate): AuthField[] {
  return [
    {
      autoComplete: "name",
      label: t("auth.field.fullName"),
      name: "userFullName",
      placeholder: t("auth.field.fullNamePlaceholder"),
      type: "text",
    },
    {
      autoComplete: "email",
      label: t("auth.field.email"),
      name: "userEmail",
      placeholder: t("auth.field.emailPlaceholder"),
      type: "email",
    },
    {
      autoComplete: "username",
      label: t("auth.field.username"),
      name: "userName",
      placeholder: t("auth.field.usernamePlaceholder"),
      type: "text",
    },
    {
      autoComplete: "new-password",
      label: t("auth.field.password"),
      name: "password",
      placeholder: t("auth.field.newPasswordPlaceholder"),
      type: "password",
    },
  ];
}

function getFields(mode: AuthMode, t: Translate) {
  if (mode === "register") {
    return getRegisterFields(t);
  }

  return getSharedFields(t);
}

function getSafeNextPath(nextPath: string | null) {
  if (!nextPath || !nextPath.startsWith("/") || nextPath.startsWith("//")) {
    return "/";
  }

  return nextPath;
}

function buildSwitchHref(
  baseHref: string,
  nextPath: string,
  reason: string | null,
) {
  const params = new URLSearchParams();

  if (reason) {
    params.set("reason", reason);
  }

  if (nextPath !== "/") {
    params.set("next", nextPath);
  }

  const query = params.toString();

  return query ? `${baseHref}?${query}` : baseHref;
}

function trimToUndefined(value: string) {
  const trimmedValue = value.trim();

  return trimmedValue || undefined;
}

function hasStartedRegion(region: RegionState) {
  return Boolean(
    region.provinceCode ||
      region.province.trim() ||
      region.ward.trim() ||
      region.area.trim() ||
      region.street.trim(),
  );
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) {
    return error.message;
  }

  return fallback;
}

export function AuthCard({ mode }: AuthCardProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { refetch, setUser } = useAuth();
  const { t } = useI18n();
  const copy = getAuthCopy(t)[mode];
  const fields = getFields(mode, t);
  const [errorMessage, setErrorMessage] = useState("");
  const [fullName, setFullName] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);
  const [selectedSuggestion, setSelectedSuggestion] = useState("");
  const [suggestionError, setSuggestionError] = useState("");
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [userName, setUserName] = useState("");
  const [region, setRegion] = useState<RegionState>(emptyRegion);
  const [provinceOptions, setProvinceOptions] = useState<RegionProvinceResponse[]>([]);
  const [cityOptions, setCityOptions] = useState<RegionCityResponse[]>([]);
  const [wardOptions, setWardOptions] = useState<RegionWardResponse[]>([]);
  const [isProvinceLoading, setIsProvinceLoading] = useState(mode === "register");
  const [isCityLoading, setIsCityLoading] = useState(false);
  const [isWardLoading, setIsWardLoading] = useState(false);
  const [addressDataError, setAddressDataError] = useState<string | null>(null);
  const suggestionRequestIdRef = useRef(0);
  const reason = searchParams.get("reason");
  const nextPath = getSafeNextPath(searchParams.get("next"));
  const switchHref = buildSwitchHref(copy.switchHref, nextPath, reason);
  const shouldShowAuthNotice = reason === "auth_required";

  useEffect(() => {
    if (mode !== "register") {
      return;
    }

    const trimmedFullName = fullName.trim();

    if (trimmedFullName.length < 2) {
      setSuggestions([]);
      setSelectedSuggestion("");
      setSuggestionError("");
      setIsLoadingSuggestions(false);
      return;
    }

    const requestId = suggestionRequestIdRef.current + 1;
    suggestionRequestIdRef.current = requestId;
    const abortController = new AbortController();
    const timeoutId = window.setTimeout(async () => {
      setIsLoadingSuggestions(true);
      setSuggestionError("");

      try {
        const response = await suggestUserNames(trimmedFullName, abortController.signal);

        if (suggestionRequestIdRef.current !== requestId) {
          return;
        }

        setSuggestions(response.suggestions);
      } catch (error) {
        if (abortController.signal.aborted || suggestionRequestIdRef.current !== requestId) {
          return;
        }

        setSuggestions([]);
        setSuggestionError(t("auth.suggestions.unavailable"));
      } finally {
        if (suggestionRequestIdRef.current === requestId) {
          setIsLoadingSuggestions(false);
        }
      }
    }, 400);

    return () => {
      window.clearTimeout(timeoutId);
      abortController.abort();
    };
  }, [fullName, mode, t]);

  useEffect(() => {
    if (mode !== "register") {
      setIsProvinceLoading(false);
      return;
    }

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
  }, [mode, t]);

  useEffect(() => {
    if (mode !== "register" || !region.provinceCode) {
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
  }, [mode, region.provinceCode, t]);

  useEffect(() => {
    if (mode !== "register" || !region.cityCode || !region.provinceCode) {
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
  }, [mode, region.cityCode, region.provinceCode, t]);

  function handleFullNameChange(value: string) {
    setFullName(value);
    setUserName("");
    setSelectedSuggestion("");
    setSuggestions([]);
    setSuggestionError("");
  }

  function handleSuggestionClick(suggestion: string) {
    setUserName(suggestion);
    setSelectedSuggestion(suggestion);
  }

  function buildRegionRequest(): UpdateMeRegionRequest | null {
    if (mode !== "register" || !hasStartedRegion(region)) {
      return null;
    }

    if (!region.provinceCode || !region.wardCode) {
      throw new Error(t("auth.error.regionIncomplete"));
    }

    const area = region.area.trim();

    return {
      cityCode: region.cityCode || undefined,
      city: trimToUndefined(region.city),
      provinceCode: region.provinceCode || undefined,
      province: trimToUndefined(region.province),
      wardCode: region.wardCode || undefined,
      ward: trimToUndefined(region.ward),
      area: area || undefined,
      district: area || undefined,
      street: trimToUndefined(region.street),
    };
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");
    setIsSubmitting(true);

    const formData = new FormData(event.currentTarget);
    const password = String(formData.get("password") ?? "");

    try {
      if (mode === "login") {
        const response = await login({
          identifier: String(formData.get("identifier") ?? ""),
          password,
        });

        if (setUser) {
          setUser(response.user);
        } else {
          await refetch();
        }
      } else {
        const userEmail = String(formData.get("userEmail") ?? "");
        const regionRequest = buildRegionRequest();

        await register({
          userName: String(formData.get("userName") ?? ""),
          userFullName: String(formData.get("userFullName") ?? ""),
          userEmail,
          password,
        });
        const response = await login({
          identifier: userEmail,
          password,
        });

        if (!regionRequest) {
          if (setUser) {
            setUser(response.user);
          } else {
            await refetch();
          }
        } else {
          try {
            await updateMeRegion(regionRequest);
            await refetch();
          } catch (regionError) {
            if (setUser) {
              setUser(response.user);
            } else {
              await refetch();
            }

            throw new Error(
              t("auth.error.regionAfterRegister", {
                reason: getErrorMessage(
                  regionError,
                  t("auth.error.updateAddressLater"),
                ),
              }),
            );
          }
        }
      }

      router.push(nextPath);
      router.refresh();
    } catch (error) {
      setErrorMessage(
        getErrorMessage(error, t("common.somethingWentWrong")),
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="w-full">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-black leading-tight text-espresso sm:text-[32px]">
          {copy.title}
        </h1>
        <p className="max-w-[360px] text-base leading-6 text-coffee-muted">
          {copy.description}
        </p>
      </div>

      {shouldShowAuthNotice ? (
        <p className="mt-6 rounded border border-primary/20 bg-primary/10 px-4 py-3 text-sm font-semibold leading-6 text-primary-strong">
          {t("auth.notice.signInRequired")}
        </p>
      ) : null}

      <form className="mt-6 space-y-6" onSubmit={handleSubmit}>
        <FieldGroup>
          {fields.map((field) => {
            const isFullNameField = mode === "register" && field.name === "userFullName";
            const isUserNameField = mode === "register" && field.name === "userName";

            return (
              <Field key={field.name}>
                <div className="flex items-center justify-between gap-3">
                  <FieldLabel
                    className="text-xs font-black uppercase tracking-[0.08em] text-coffee-muted"
                    htmlFor={field.name}
                  >
                    {field.label}
                  </FieldLabel>
                  {mode === "login" && field.name === "password" ? (
                    <a
                      className="text-xs font-bold normal-case tracking-normal text-coffee-muted no-underline transition hover:text-espresso"
                      href="#"
                    >
                      {t("auth.forgotPassword")}
                    </a>
                  ) : null}
                </div>
                <Input
                  autoComplete={field.autoComplete}
                  className="rounded-none border-0 border-b border-line-soft bg-transparent px-0 text-base text-espresso placeholder:text-line-soft focus:border-espresso"
                  id={field.name}
                  name={field.name}
                  onChange={
                    isFullNameField
                      ? (event) => handleFullNameChange(event.target.value)
                      : undefined
                  }
                  placeholder={field.placeholder}
                  readOnly={isUserNameField}
                  required
                  type={field.type}
                  value={
                    isFullNameField
                      ? fullName
                      : isUserNameField
                        ? userName
                        : undefined
                  }
                />

                {isFullNameField &&
                (isLoadingSuggestions || suggestions.length > 0 || suggestionError) ? (
                  <div className="flex min-h-9 flex-wrap items-center gap-2 pt-1">
                    {isLoadingSuggestions ? (
                      <span className="text-xs font-semibold text-coffee-muted">
                        {t("auth.suggestions.loading")}
                      </span>
                    ) : null}
                    {!isLoadingSuggestions && suggestionError ? (
                      <span className="text-xs font-semibold text-coffee-muted">
                        {suggestionError}
                      </span>
                    ) : null}
                    {!isLoadingSuggestions
                      ? suggestions.map((suggestion) => (
                          <button
                            className={cn(
                              "min-h-8 rounded border border-line-soft bg-surface px-3 text-xs font-black text-espresso transition hover:border-espresso",
                              selectedSuggestion === suggestion &&
                                "border-espresso bg-espresso text-white",
                            )}
                            key={suggestion}
                            onClick={() => handleSuggestionClick(suggestion)}
                            type="button"
                          >
                            {suggestion}
                          </button>
                        ))
                      : null}
                  </div>
                ) : null}
              </Field>
            );
          })}
        </FieldGroup>

        {mode === "register" ? (
          <>
            <FieldGroup>
              <SearchableDropdown
                disabled={isSubmitting}
                emptyLabel={t("profileEdit.address.provinceEmpty")}
                isLoading={isProvinceLoading}
                label={t("profileEdit.address.province")}
                labelClassName="text-xs font-black uppercase tracking-[0.08em] text-coffee-muted leading-none"
                triggerClassName="h-12 rounded-none border-0 border-b border-line-soft bg-transparent px-0 text-base text-espresso"
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
                disabled={!region.provinceCode || isSubmitting}
                emptyLabel={t("profileEdit.address.cityEmpty")}
                isLoading={isCityLoading}
                label={t("profileEdit.address.city")}
                labelClassName="text-xs font-black uppercase tracking-[0.08em] text-coffee-muted leading-none"
                triggerClassName="h-12 rounded-none border-0 border-b border-line-soft bg-transparent px-0 text-base text-espresso"
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
                disabled={!region.cityCode || isSubmitting}
                emptyLabel={t("profileEdit.address.wardEmpty")}
                isLoading={isWardLoading}
                label={t("profileEdit.address.ward")}
                labelClassName="text-xs font-black uppercase tracking-[0.08em] text-coffee-muted leading-none"
                triggerClassName="h-12 rounded-none border-0 border-b border-line-soft bg-transparent px-0 text-base text-espresso"
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
                <FieldLabel
                  className="text-xs font-black uppercase tracking-[0.08em] text-coffee-muted"
                  htmlFor="area"
                >
                  {t("profileEdit.address.area")}
                </FieldLabel>
                <Input
                  className="rounded-none border-0 border-b border-line-soft bg-transparent px-0 text-base text-espresso placeholder:text-line-soft focus:border-espresso"
                  disabled={isSubmitting}
                  id="area"
                  name="area"
                  onChange={(event) =>
                    setRegion((current) => ({
                      ...current,
                      area: event.target.value,
                    }))
                  }
                  placeholder={t("auth.field.areaPlaceholder")}
                  value={region.area}
                />
              </Field>
              <Field>
                <FieldLabel
                  className="text-xs font-black uppercase tracking-[0.08em] text-coffee-muted"
                  htmlFor="street"
                >
                  {t("profileEdit.address.street")}
                </FieldLabel>
                <Input
                  className="rounded-none border-0 border-b border-line-soft bg-transparent px-0 text-base text-espresso placeholder:text-line-soft focus:border-espresso"
                  disabled={isSubmitting}
                  id="street"
                  name="street"
                  onChange={(event) =>
                    setRegion((current) => ({
                      ...current,
                      street: event.target.value,
                    }))
                  }
                  placeholder={t("auth.field.streetPlaceholder")}
                  value={region.street}
                />
              </Field>
              {addressDataError ? (
                <p className="rounded border border-accent/25 bg-accent/10 px-4 py-3 text-sm font-medium text-espresso">
                  {addressDataError}
                </p>
              ) : null}
            </FieldGroup>

            <Field orientation="horizontal">
              <Checkbox
                className="mt-0.5"
                name="terms"
                required
              />
              <FieldContent>
                <FieldLabel className="text-xs leading-5 text-coffee-muted">
                  {t("auth.terms")}
                </FieldLabel>
              </FieldContent>
            </Field>
          </>
        ) : null}

        {errorMessage ? (
          <p className="rounded border border-accent/25 bg-accent/10 px-4 py-3 text-sm font-medium text-espresso">
            {errorMessage}
          </p>
        ) : null}

        <Button
          className="mt-2 flex h-[62px] w-full items-center justify-center gap-3 rounded bg-espresso px-10 text-sm font-black uppercase tracking-[0.08em] text-white shadow-[0_20px_25px_-5px_rgba(39,19,16,0.05),0_8px_10px_-6px_rgba(39,19,16,0.05)] transition hover:bg-[#3a201b] focus:outline-none focus:ring-4 focus:ring-espresso/15 disabled:cursor-not-allowed disabled:opacity-65"
          disabled={isSubmitting}
          type="submit"
        >
          {isSubmitting ? t("auth.pleaseWait") : copy.submitLabel}
          <span aria-hidden="true">-&gt;</span>
        </Button>
      </form>

      <div className="my-10 flex items-center gap-4">
        <Separator className="flex-1" />
        <span className="text-xs uppercase text-coffee-muted">
          {t("auth.orContinueWith")}
        </span>
        <Separator className="flex-1" />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <Button
          className="flex h-[54px] items-center justify-center gap-3 rounded border border-line-soft bg-surface text-sm font-black uppercase tracking-[0.08em] text-foreground transition hover:border-espresso"
          type="button"
          variant="outline"
        >
          <span className="grid size-5 place-items-center rounded-full border border-line-soft text-xs">
            G
          </span>
          {t("auth.provider.google")}
        </Button>
        <Button
          className="flex h-[54px] items-center justify-center gap-3 rounded border border-line-soft bg-surface text-sm font-black uppercase tracking-[0.08em] text-foreground transition hover:border-espresso"
          type="button"
          variant="outline"
        >
          <span className="text-xs font-black">iOS</span>
          {t("auth.provider.apple")}
        </Button>
      </div>

      <div className="mt-8 flex items-center justify-center gap-1 text-center text-base text-coffee-muted">
        <span>{copy.switchPrompt}</span>
        <Link className="font-black text-espresso no-underline" href={switchHref}>
          {copy.switchLabel}
        </Link>
      </div>
    </section>
  );
}
