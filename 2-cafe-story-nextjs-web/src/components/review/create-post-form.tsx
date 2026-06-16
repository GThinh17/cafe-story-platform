import {
  type FormEventHandler,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import {
  CheckIcon,
  ChevronDownIcon,
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
import type { ReviewComposerModel, ReviewDraftHint } from "@/types/review";

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
  return (
    <Card asChild>
      <form className="overflow-hidden" onSubmit={onSubmit}>
      <CardHeader className="flex items-start justify-between gap-4 border-b border-border px-6 py-5">
        <div className="min-w-0">
          <p className="text-xs font-black uppercase tracking-[0.12em] text-primary">
            Reviewer
          </p>
          <CardTitle className="mt-2 text-2xl font-black text-foreground">
            {composer.title}
          </CardTitle>
          <CardDescription className="mt-1 max-w-[440px] text-sm leading-6">
            {composer.subtitle}
          </CardDescription>
        </div>
        <Badge className="shrink-0 px-3 py-2 text-xs font-black" variant="secondary">
          Draft
        </Badge>
      </CardHeader>

      <CardContent className="space-y-6 p-6">
        <Card className="overflow-hidden bg-background shadow-none">
          <img
            alt={`${composer.selectedCafe} cafe preview`}
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
              Change photo
            </Button>
          </div>
        </Card>

        <FieldGroup className="grid gap-4 sm:grid-cols-2">
          <Field className="sm:col-span-2">
            <FieldLabel className="text-sm font-black">Cafe</FieldLabel>
            <Input
              defaultValue={composer.selectedCafe}
              type="text"
            />
          </Field>

          <Field>
            <FieldLabel className="text-sm font-black">Visit type</FieldLabel>
            <Input
              defaultValue={composer.visitType}
              type="text"
            />
          </Field>

          <Field>
            <FieldLabel className="text-sm font-black">Spend</FieldLabel>
            <Input
              defaultValue={composer.spend}
              type="text"
            />
          </Field>
        </FieldGroup>

        <section className="space-y-3">
          <p className="text-sm font-black">Rating</p>
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
          <FieldLabel className="text-sm font-black">Review</FieldLabel>
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
            <h2 className="text-sm font-black">AI tags</h2>
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
            Public
          </Badge>
        </section>
      </CardContent>

      <CardFooter className="flex flex-wrap items-center justify-end gap-3 border-t border-border bg-background px-6 py-4">
        <Button
          className="h-11 text-sm font-black"
          variant="outline"
          type="button"
        >
          Save draft
        </Button>
        <Button
          className="h-11 px-5 text-sm font-black"
          type="submit"
        >
          Publish review
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

type SearchableDropdownProps<
  T extends RegionProvinceResponse | RegionCityResponse | RegionWardResponse,
> = {
  disabled?: boolean;
  emptyLabel: string;
  isLoading?: boolean;
  label: string;
  onSelect: (option: T) => void;
  options: T[];
  placeholder: string;
  selectedCode?: string | null;
  selectedName?: string | null;
  valueKey: keyof T;
};

function SearchableDropdown<
  T extends RegionProvinceResponse | RegionCityResponse | RegionWardResponse,
>({
  disabled = false,
  emptyLabel,
  isLoading = false,
  label,
  onSelect,
  options,
  placeholder,
  selectedCode,
  selectedName,
  valueKey,
}: SearchableDropdownProps<T>) {
  const [isOpen, setIsOpen] = useState(false);
  const [query, setQuery] = useState("");
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const filtered = useMemo(() => {
    if (!query) return options;
    const lower = query.toLowerCase();
    return options.filter((o) => o.name.toLowerCase().includes(lower));
  }, [options, query]);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (
        containerRef.current &&
        !containerRef.current.contains(e.target as Node)
      ) {
        setIsOpen(false);
        setQuery("");
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    if (!isOpen) setQuery("");
  }, [isOpen]);

  function handleSelect(option: T) {
    onSelect(option);
    setIsOpen(false);
    setQuery("");
  }

  return (
    <div className="flex flex-col gap-1.5" ref={containerRef}>
      <span className="text-xs font-bold uppercase tracking-wider text-muted">
        {label}
      </span>

      {isLoading ? (
        <div className="flex h-10 items-center gap-2 rounded-md border border-line-soft bg-surface-muted px-3 text-sm text-muted">
          <LoaderCircleIcon className="size-4 animate-spin" />
          Loading...
        </div>
      ) : (
        <div className="relative">
          <button
            className={`flex h-10 w-full items-center justify-between rounded-md border px-3 text-sm transition ${
              disabled
                ? "cursor-not-allowed border-line-soft bg-surface-muted text-muted opacity-60"
                : selectedCode
                  ? "border-espresso bg-espresso/5 font-bold text-espresso"
                  : "border-line-soft bg-surface-muted text-muted hover:border-espresso"
            }`}
            disabled={disabled}
            onClick={() => {
              setIsOpen(!isOpen);
              setTimeout(() => inputRef.current?.focus(), 0);
            }}
            type="button"
          >
            <span className="truncate">
              {selectedName ?? placeholder}
            </span>
            <ChevronDownIcon
              className={`size-4 shrink-0 transition-transform ${isOpen ? "rotate-180" : ""}`}
            />
          </button>

          {isOpen && (
            <div className="absolute z-50 mt-1 w-full overflow-hidden rounded-md border border-line-soft bg-surface shadow-lg">
              <div className="border-b border-line-soft px-3 py-2">
                <input
                  autoComplete="off"
                  className="w-full bg-transparent text-sm text-espresso outline-none placeholder:text-muted"
                  onChange={(e) => setQuery(e.target.value)}
                  placeholder={`Search ${label.toLowerCase()}...`}
                  ref={inputRef}
                  type="text"
                  value={query}
                />
              </div>
              <div className="max-h-48 overflow-y-auto">
                {filtered.length > 0 ? (
                  filtered.map((option) => {
                    const code = String(option[valueKey]);
                    const isSelected = code === selectedCode;

                    return (
                      <button
                        className={`flex w-full items-center gap-2 px-3 py-2 text-left text-sm transition ${
                          isSelected
                            ? "bg-espresso/10 font-bold text-espresso"
                            : "text-espresso hover:bg-surface-muted"
                        }`}
                        key={code}
                        onClick={() => handleSelect(option)}
                        type="button"
                      >
                        {isSelected && (
                          <CheckIcon className="size-4 shrink-0" />
                        )}
                        <span className={isSelected ? "" : "pl-6"}>
                          {option.name}
                        </span>
                      </button>
                    );
                  })
                ) : (
                  <p className="px-3 py-3 text-sm text-muted">{emptyLabel}</p>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export function CreatePostLocationPicker({
  isOpen,
  onApply,
  onClose,
}: CreatePostLocationPickerProps) {
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
            err instanceof Error ? err.message : "Unable to load provinces.",
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
            err instanceof Error ? err.message : "Unable to load cities.",
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
            err instanceof Error ? err.message : "Unable to load wards.",
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
      setError("Province and city are required.");
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
        name: locationName || region.city || "Selected location",
        regionId: region.regionId,
      });
      onClose();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to save this location.",
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
          <DialogTitle className="font-serif text-lg font-semibold text-espresso">
            Select location
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
                Saving
              </>
            ) : (
              "Done"
            )}
          </Button>
        </header>

        <div className="flex flex-col gap-5 overflow-y-auto px-6 py-5">
          <p className="text-sm text-muted">
            Pick a post location. Ward is optional for blog posts.
          </p>

          <SearchableDropdown
            emptyLabel="No provinces found."
            isLoading={isProvinceLoading}
            label="Province"
            onSelect={handleProvinceSelect}
            options={provinces}
            placeholder="Select a province..."
            selectedCode={selectedProvince?.provinceCode}
            selectedName={selectedProvince?.name}
            valueKey="provinceCode"
          />

          <SearchableDropdown
            disabled={!selectedProvince}
            emptyLabel="No cities found for this province."
            isLoading={isCityLoading}
            label="City"
            onSelect={handleCitySelect}
            options={cities}
            placeholder="Select a city..."
            selectedCode={selectedCity?.cityCode}
            selectedName={selectedCity?.name}
            valueKey="cityCode"
          />

          <SearchableDropdown
            disabled={!selectedCity}
            emptyLabel="No wards found for this city."
            isLoading={isWardLoading}
            label="Ward (optional)"
            onSelect={setSelectedWard}
            options={wards}
            placeholder="Select a ward..."
            selectedCode={selectedWard?.wardCode}
            selectedName={selectedWard?.name}
            valueKey="wardCode"
          />

          {locationName ? (
            <div className="flex items-center gap-3 rounded-xl border border-espresso bg-espresso/5 p-4">
              <MapPinIcon className="size-5 shrink-0 text-espresso" />
              <div className="min-w-0">
                <span className="text-xs font-bold uppercase tracking-wider text-espresso">
                  Selected location
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
