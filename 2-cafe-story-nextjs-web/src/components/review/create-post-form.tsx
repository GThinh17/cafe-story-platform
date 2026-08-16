"use client";

import {
  type FormEventHandler,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";
import {
  LoaderCircleIcon,
  MapPinIcon,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogTitle,
} from "@/components/ui/dialog";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  createRegion,
  getRegionCities,
  getRegionProvinces,
  getRegionWards,
  type RegionCityResponse,
  type RegionProvinceResponse,
  type RegionWardResponse,
} from "@/lib/api/regions";
import { SearchableDropdown } from "@/components/ui/searchable-dropdown";
import type { ReviewComposerModel, ReviewDraftHint } from "@/types/review";
import { useI18n } from "@/components/providers/locale-provider";

type CreatePostFormProps = {
  composer: ReviewComposerModel;
  hints: ReviewDraftHint[];
  onSubmit?: FormEventHandler<HTMLFormElement>;
};

const ratingValues = [1, 2, 3, 4, 5];

export function CreatePostForm({
  composer,
  hints,
  onSubmit,
}: CreatePostFormProps) {
  const { t } = useI18n();
  return (
    <Card asChild>
      <form className="overflow-hidden" onSubmit={onSubmit}>
      <CardHeader className="flex items-start justify-between gap-4 border-b border-border px-6 py-5">
        <div className="min-w-0">
          <p className="text-xs font-black uppercase tracking-[0.12em] text-primary">
            {t("composer.eyebrow")}
          </p>
          <CardTitle className="mt-2 text-2xl font-black text-foreground">
            {composer.title}
          </CardTitle>
          <CardDescription className="mt-1 max-w-[440px] text-sm leading-6">
            {composer.subtitle}
          </CardDescription>
        </div>
        <Badge className="shrink-0 px-3 py-2 text-xs font-black" variant="secondary">
          {t("composer.draft")}
        </Badge>
      </CardHeader>

      <CardContent className="space-y-6 p-6">
        <Card className="overflow-hidden bg-background shadow-none">
          <img
            alt={t("composer.previewAlt", { name: composer.selectedCafe })}
            className="aspect-[16/10] w-full object-cover"
            decoding="async"
            src={composer.previewImage}
          />
          <div className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
            <div className="min-w-0">
              <p className="truncate text-sm font-black">
                {composer.selectedCafe}
              </p>
              <p className="text-xs text-muted">{composer.location}</p>
            </div>
            <Button
              className="text-xs font-black"
              size="sm"
              variant="outline"
              type="button"
            >
              {t("composer.changePhoto")}
            </Button>
          </div>
        </Card>

        <FieldGroup className="grid gap-4 sm:grid-cols-2">
          <Field className="sm:col-span-2">
            <FieldLabel className="text-sm font-black">{t("composer.cafe")}</FieldLabel>
            <Input
              defaultValue={composer.selectedCafe}
              type="text"
            />
          </Field>

          <Field>
            <FieldLabel className="text-sm font-black">
              {t("composer.visitType")}
            </FieldLabel>
            <Input
              defaultValue={composer.visitType}
              type="text"
            />
          </Field>

          <Field>
            <FieldLabel className="text-sm font-black">{t("composer.spend")}</FieldLabel>
            <Input
              defaultValue={composer.spend}
              type="text"
            />
          </Field>
        </FieldGroup>

        <section className="space-y-3">
          <p className="text-sm font-black">{t("composer.rating")}</p>
          <div className="grid grid-cols-5 gap-2">
            {ratingValues.map((rating) => (
              <Badge
                className={`flex h-11 items-center justify-center rounded-md border text-sm font-black transition ${
                  rating <= composer.rating
                    ? "border-rating bg-rating/15 text-rating"
                    : "border-border bg-surface text-muted"
                }`}
                key={rating}
                variant="outline"
              >
                {rating}
              </Badge>
            ))}
          </div>
        </section>

        <Field>
          <FieldLabel className="text-sm font-black">{t("composer.review")}</FieldLabel>
          <Textarea
            className="min-h-36"
            defaultValue={composer.caption}
          />
        </Field>

        <section className="space-y-3">
          <div className="flex items-center gap-2">
            <Badge className="grid size-6 place-items-center rounded-md bg-primary/10 p-0 text-xs font-black text-primary">
              AI
            </Badge>
            <h2 className="text-sm font-black">{t("composer.aiTags")}</h2>
          </div>
          <div className="flex flex-wrap gap-2">
            {composer.aiTags.map((tag) => (
              <Badge className="px-3 py-2 text-xs font-black" key={tag} variant="secondary">
                {tag}
              </Badge>
            ))}
          </div>
        </section>

        <FieldGroup className="grid grid-cols-2 gap-3">
          {hints.map((hint) => (
            <Field key={hint.label}>
              <FieldLabel className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                {hint.label}
              </FieldLabel>
              <Input
                className="h-11 px-3"
                defaultValue={hint.value}
                type="text"
              />
            </Field>
          ))}
        </FieldGroup>

        <section className="flex flex-wrap items-center justify-between gap-3 rounded-md border border-border bg-background px-4 py-3">
          <div className="min-w-0">
            <p className="text-sm font-black">{composer.moderation.label}</p>
            <p className="text-xs text-muted">{composer.moderation.status}</p>
          </div>
          <Badge className="bg-primary/10 px-3 py-2 text-xs font-black text-primary">
            {t("composer.public")}
          </Badge>
        </section>
      </CardContent>

      <CardFooter className="flex flex-wrap items-center justify-end gap-3 border-t border-border bg-background px-6 py-4">
        <Button
          className="h-11 text-sm font-black"
          variant="outline"
          type="button"
        >
          {t("composer.saveDraft")}
        </Button>
        <Button
          className="h-11 px-5 text-sm font-black"
          type="submit"
        >
          {t("composer.publish")}
        </Button>
      </CardFooter>
      </form>
    </Card>
  );
}

// --- Location Picker Dialog ---

export type PostLocation = {
  name: string;
  regionId: string;
};

type CreatePostLocationPickerProps = {
  isOpen: boolean;
  onApply: (location: PostLocation) => void;
  onClose: () => void;
};

export function CreatePostLocationPicker({
  isOpen,
  onApply,
  onClose,
}: CreatePostLocationPickerProps) {
  const { t } = useI18n();
  const [provinces, setProvinces] = useState<RegionProvinceResponse[]>([]);
  const [cities, setCities] = useState<RegionCityResponse[]>([]);
  const [wards, setWards] = useState<RegionWardResponse[]>([]);
  const [selectedProvince, setSelectedProvince] =
    useState<RegionProvinceResponse | null>(null);
  const [selectedCity, setSelectedCity] = useState<RegionCityResponse | null>(
    null,
  );
  const [selectedWard, setSelectedWard] = useState<RegionWardResponse | null>(
    null,
  );
  const [error, setError] = useState<string | null>(null);
  const [isProvinceLoading, setIsProvinceLoading] = useState(false);
  const [isCityLoading, setIsCityLoading] = useState(false);
  const [isWardLoading, setIsWardLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    let active = true;

    async function loadProvinces() {
      setIsProvinceLoading(true);
      setError(null);

      try {
        const data = await getRegionProvinces();
        if (active) setProvinces(data);
      } catch (err) {
        if (active)
          setError(
            err instanceof Error
              ? err.message
              : t("createPost.locationPicker.loadProvincesError"),
          );
      } finally {
        if (active) setIsProvinceLoading(false);
      }
    }

    void loadProvinces();
    return () => {
      active = false;
    };
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen || !selectedProvince) {
      setCities([]);
      setSelectedCity(null);
      return;
    }

    const province = selectedProvince;
    let active = true;

    async function loadCities() {
      setIsCityLoading(true);
      setError(null);

      try {
        const data = await getRegionCities(province.provinceCode);
        if (active) {
          setCities(data);
          setSelectedCity(data.length === 1 ? data[0] : null);
        }
      } catch (err) {
        if (active)
          setError(
            err instanceof Error
              ? err.message
              : t("createPost.locationPicker.loadCitiesError"),
          );
      } finally {
        if (active) setIsCityLoading(false);
      }
    }

    void loadCities();
    return () => {
      active = false;
    };
  }, [selectedProvince, isOpen]);

  useEffect(() => {
    if (!isOpen || !selectedProvince || !selectedCity) {
      setWards([]);
      setSelectedWard(null);
      return;
    }

    const province = selectedProvince;
    const city = selectedCity;
    let active = true;

    async function loadWards() {
      setIsWardLoading(true);
      setError(null);

      try {
        const data = await getRegionWards({
          cityCode: city.cityCode,
          provinceCode: province.provinceCode,
        });
        if (active) {
          setWards(data);
          setSelectedWard(null);
        }
      } catch (err) {
        if (active)
          setError(
            err instanceof Error
              ? err.message
              : t("createPost.locationPicker.loadWardsError"),
          );
      } finally {
        if (active) setIsWardLoading(false);
      }
    }

    void loadWards();
    return () => {
      active = false;
    };
  }, [selectedCity, selectedProvince, isOpen]);

  const canSave = Boolean(selectedProvince && selectedCity && !isSaving);

  const locationName = useMemo(
    () =>
      [selectedWard?.name, selectedCity?.name, selectedProvince?.name]
        .filter(Boolean)
        .join(", "),
    [selectedCity?.name, selectedProvince?.name, selectedWard?.name],
  );

  const handleReset = useCallback(() => {
    setSelectedProvince(null);
    setSelectedCity(null);
    setSelectedWard(null);
    setProvinces([]);
    setCities([]);
    setWards([]);
    setError(null);
  }, []);

  function handleProvinceSelect(province: RegionProvinceResponse) {
    setSelectedProvince(province);
    setSelectedCity(null);
    setSelectedWard(null);
    setCities([]);
    setWards([]);
  }

  function handleCitySelect(city: RegionCityResponse) {
    setSelectedCity(city);
    setSelectedWard(null);
    setWards([]);
  }

  async function handleSave() {
    if (!selectedProvince || !selectedCity) {
      setError(t("createPost.locationPicker.provinceCityRequired"));
      return;
    }

    setIsSaving(true);
    setError(null);

    try {
      const region = await createRegion(
        {
          city: selectedCity.name,
          cityCode: selectedCity.cityCode,
          province: selectedProvince.name,
          provinceCode: selectedProvince.provinceCode,
          ward: selectedWard?.name,
          wardCode: selectedWard?.wardCode,
        },
        "BLOG_LOCATION",
      );

      onApply({
        name:
          locationName ||
          region.city ||
          t("createPost.locationPicker.selected"),
        regionId: region.regionId,
      });
      onClose();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : t("createPost.locationPicker.saveError"),
      );
    } finally {
      setIsSaving(false);
    }
  }

  function handleClose() {
    if (isSaving) return;
    handleReset();
    onClose();
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && handleClose()}>
      <DialogContent className="flex max-h-[70vh] flex-col gap-0 p-0">
        <header className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <DialogTitle className="text-lg font-bold text-espresso">
            {t("createPost.locationPicker.title")}
          </DialogTitle>
          <Button
            className="h-9 bg-espresso px-5 text-sm font-black hover:bg-primary-container"
            disabled={!canSave}
            onClick={handleSave}
            type="button"
          >
            {isSaving ? (
              <>
                <LoaderCircleIcon className="size-4 animate-spin" />
                {t("createPost.locationPicker.saving")}
              </>
            ) : (
              t("common.done")
            )}
          </Button>
        </header>

        <div className="flex flex-col gap-5 overflow-y-auto px-6 py-5">
          <p className="text-sm text-muted">
            {t("createPost.locationPicker.hint")}
          </p>

          <SearchableDropdown
            emptyLabel={t("profileEdit.address.provinceEmpty")}
            isLoading={isProvinceLoading}
            label={t("createPost.locationPicker.province")}
            onSelect={handleProvinceSelect}
            options={provinces}
            placeholder={t("createPost.locationPicker.provincePlaceholder")}
            selectedCode={selectedProvince?.provinceCode}
            selectedName={selectedProvince?.name}
            valueKey="provinceCode"
          />

          <SearchableDropdown
            disabled={!selectedProvince}
            emptyLabel={t("profileEdit.address.cityEmpty")}
            isLoading={isCityLoading}
            label={t("createPost.locationPicker.city")}
            onSelect={handleCitySelect}
            options={cities}
            placeholder={t("createPost.locationPicker.cityPlaceholder")}
            selectedCode={selectedCity?.cityCode}
            selectedName={selectedCity?.name}
            valueKey="cityCode"
          />

          <SearchableDropdown
            disabled={!selectedCity}
            emptyLabel={t("profileEdit.address.wardEmpty")}
            isLoading={isWardLoading}
            label={t("createPost.locationPicker.ward")}
            onSelect={setSelectedWard}
            options={wards}
            placeholder={t("createPost.locationPicker.wardPlaceholder")}
            selectedCode={selectedWard?.wardCode}
            selectedName={selectedWard?.name}
            valueKey="wardCode"
          />

          {locationName ? (
            <div className="flex items-center gap-3 rounded-xl border border-espresso bg-espresso/5 p-4">
              <MapPinIcon className="size-5 shrink-0 text-espresso" />
              <div className="min-w-0">
                <span className="text-xs font-bold uppercase tracking-wider text-espresso">
                  {t("createPost.locationPicker.selected")}
                </span>
                <p className="truncate text-sm font-bold text-espresso">
                  {locationName}
                </p>
              </div>
            </div>
          ) : null}

          {error ? (
            <p className="rounded-md border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm font-medium text-destructive">
              {error}
            </p>
          ) : null}
        </div>
      </DialogContent>
    </Dialog>
  );
}
