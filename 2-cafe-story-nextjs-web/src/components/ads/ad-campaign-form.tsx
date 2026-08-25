"use client";

import { useEffect, useMemo, useState } from "react";
import { PlusIcon, Trash2Icon, UploadIcon } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { createAdCampaign } from "@/lib/api/ads";
import { ApiError } from "@/lib/api/client";
import { uploadAdImageToCloudinary } from "@/lib/api/cloudinary";
import {
  getRegionCities,
  getRegionProvinces,
  getRegionWards,
  type RegionCityResponse,
  type RegionProvinceResponse,
  type RegionWardResponse,
} from "@/lib/api/regions";
import type { AdCampaignResponse, AdTargetRegionRequest } from "@/types/ads";
import type { CafePageResponse } from "@/types/cafe";
import type { PaymentResponse } from "@/types/payment";
import { useI18n } from "@/components/providers/locale-provider";

type AdCampaignFormProps = {
  cafePages: CafePageResponse[];
  initialPaymentId?: string | null;
  onCreated: (campaign: AdCampaignResponse) => void;
  payments: PaymentResponse[];
};

function errorMessage(error: unknown, fallback: string) {
  return error instanceof ApiError || error instanceof Error
    ? error.message
    : fallback;
}

export function AdCampaignForm({
  cafePages,
  initialPaymentId,
  onCreated,
  payments,
}: AdCampaignFormProps) {
  const { t } = useI18n();
  const [paymentId, setPaymentId] = useState(initialPaymentId || payments[0]?.paymentId || "");
  const [cafePageId, setCafePageId] = useState(cafePages[0]?.id || "");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [targetUrl, setTargetUrl] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [activateNow, setActivateNow] = useState(true);
  const [targetRegions, setTargetRegions] = useState<AdTargetRegionRequest[]>([]);
  const [provinces, setProvinces] = useState<RegionProvinceResponse[]>([]);
  const [cities, setCities] = useState<RegionCityResponse[]>([]);
  const [wards, setWards] = useState<RegionWardResponse[]>([]);
  const [provinceCode, setProvinceCode] = useState("");
  const [cityCode, setCityCode] = useState("");
  const [wardCode, setWardCode] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (initialPaymentId && payments.some((payment) => payment.paymentId === initialPaymentId)) {
      setPaymentId(initialPaymentId);
    } else if (!payments.some((payment) => payment.paymentId === paymentId)) {
      setPaymentId(payments[0]?.paymentId || "");
    }
  }, [initialPaymentId, paymentId, payments]);

  useEffect(() => {
    if (!cafePages.some((page) => page.id === cafePageId)) {
      setCafePageId(cafePages[0]?.id || "");
    }
  }, [cafePageId, cafePages]);

  useEffect(() => {
    let active = true;
    void getRegionProvinces()
      .then((items) => {
        if (active) setProvinces(items);
      })
      .catch(() => {
        if (active) setProvinces([]);
      });
    return () => {
      active = false;
    };
  }, []);

  const selectedProvince = useMemo(
    () => provinces.find((province) => province.provinceCode === provinceCode),
    [provinceCode, provinces],
  );
  const selectedCity = useMemo(
    () => cities.find((city) => city.cityCode === cityCode),
    [cities, cityCode],
  );
  const selectedWard = useMemo(
    () => wards.find((ward) => ward.wardCode === wardCode),
    [wardCode, wards],
  );

  async function chooseProvince(code: string) {
    setProvinceCode(code);
    setCityCode("");
    setWardCode("");
    setWards([]);
    try {
      setCities(await getRegionCities(code));
    } catch {
      setCities([]);
    }
  }

  async function chooseCity(code: string) {
    setCityCode(code);
    setWardCode("");
    try {
      setWards(await getRegionWards({ cityCode: code, provinceCode }));
    } catch {
      setWards([]);
    }
  }

  function addTargetRegion() {
    if (!selectedProvince) return;
    const region: AdTargetRegionRequest = {
      province: selectedProvince.name,
      city: selectedCity?.name || null,
      ward: selectedWard?.name || null,
      area: null,
    };
    const key = JSON.stringify(region);
    setTargetRegions((current) =>
      current.some((item) => JSON.stringify(item) === key) ? current : [...current, region],
    );
  }

  async function uploadCreative(file: File | null) {
    if (!file) return;
    setIsUploading(true);
    setError(null);
    try {
      setImageUrl(await uploadAdImageToCloudinary(file));
    } catch (uploadError) {
      setError(errorMessage(uploadError, t("adForm.createError")));
    } finally {
      setIsUploading(false);
    }
  }

  async function submitCampaign(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!paymentId || !cafePageId || !title.trim()) {
      setError(t("adForm.missingFields"));
      return;
    }
    setIsSubmitting(true);
    setError(null);
    try {
      const campaign = await createAdCampaign({
        activateNow,
        cafePageId,
        description: description.trim() || null,
        imageUrl: imageUrl || null,
        paymentId,
        targetRegions,
        targetUrl: targetUrl.trim() || null,
        title: title.trim(),
      });
      onCreated(campaign);
      setTitle("");
      setDescription("");
      setTargetUrl("");
      setImageUrl("");
      setTargetRegions([]);
    } catch (submitError) {
      setError(errorMessage(submitError, t("adForm.createError")));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="flex flex-col gap-5" onSubmit={submitCampaign}>
      {error ? (
        <Alert variant="destructive">
          <AlertTitle>{t("adForm.errorTitle")}</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      ) : null}
      <FieldGroup>
        <Field>
          <FieldLabel htmlFor="ad-payment">{t("adForm.payment")}</FieldLabel>
          <Select onValueChange={setPaymentId} value={paymentId}>
            <SelectTrigger className="w-full" id="ad-payment">
              <SelectValue placeholder={t("adForm.paymentPlaceholder")} />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                {payments.map((payment) => (
                  <SelectItem key={payment.paymentId} value={payment.paymentId}>
                    {payment.paymentId.slice(0, 8)} ·{" "}
                    {payment.amount ?? t("adForm.paymentFallbackLabel")}{" "}
                    {payment.currency ?? ""}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
          <FieldDescription>{t("adForm.paymentHint")}</FieldDescription>
        </Field>

        <Field>
          <FieldLabel htmlFor="ad-cafe-page">{t("adForm.cafePage")}</FieldLabel>
          <Select onValueChange={setCafePageId} value={cafePageId}>
            <SelectTrigger className="w-full" id="ad-cafe-page">
              <SelectValue placeholder={t("adForm.cafePagePlaceholder")} />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                {cafePages.map((page) => (
                  <SelectItem key={page.id} value={page.id}>{page.name}</SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </Field>

        <Field data-invalid={title.length > 160 || undefined}>
          <FieldLabel htmlFor="ad-title">{t("adForm.headline")}</FieldLabel>
          <Input
            aria-invalid={title.length > 160}
            id="ad-title"
            maxLength={160}
            onChange={(event) => setTitle(event.target.value)}
            placeholder={t("adForm.headlinePlaceholder")}
            required
            value={title}
          />
          <FieldDescription>
            {t("adForm.characterCount", { count: title.length, max: 160 })}
          </FieldDescription>
        </Field>

        <Field data-invalid={description.length > 1000 || undefined}>
          <FieldLabel htmlFor="ad-description">{t("adForm.description")}</FieldLabel>
          <Textarea
            aria-invalid={description.length > 1000}
            id="ad-description"
            maxLength={1000}
            onChange={(event) => setDescription(event.target.value)}
            placeholder={t("adForm.descriptionPlaceholder")}
            value={description}
          />
        </Field>

        <Field>
          <FieldLabel htmlFor="ad-target-url">{t("adForm.targetUrl")}</FieldLabel>
          <Input
            id="ad-target-url"
            onChange={(event) => setTargetUrl(event.target.value)}
            placeholder="https://example.com/menu"
            type="url"
            value={targetUrl}
          />
          <FieldDescription>{t("adForm.targetUrlHint")}</FieldDescription>
        </Field>

        <Field>
          <FieldLabel htmlFor="ad-image">{t("adForm.creative")}</FieldLabel>
          <Input
            accept="image/*"
            disabled={isUploading}
            id="ad-image"
            onChange={(event) => void uploadCreative(event.target.files?.[0] ?? null)}
            type="file"
          />
          <FieldDescription>
            {isUploading
              ? t("adForm.creativeUploading")
              : t("adForm.creativeHint")}
          </FieldDescription>
          {imageUrl ? <img alt={t("adForm.creativeAlt")} className="aspect-video w-full rounded-lg object-cover" src={imageUrl} /> : null}
        </Field>

        <Field>
          <FieldLabel>{t("adForm.targetRegions")}</FieldLabel>
          <div className="grid gap-2 md:grid-cols-3">
            <Select onValueChange={(value) => void chooseProvince(value)} value={provinceCode}>
              <SelectTrigger className="w-full"><SelectValue placeholder={t("adForm.province")} /></SelectTrigger>
              <SelectContent><SelectGroup>{provinces.map((province) => <SelectItem key={province.provinceCode} value={province.provinceCode}>{province.name}</SelectItem>)}</SelectGroup></SelectContent>
            </Select>
            <Select disabled={!provinceCode} onValueChange={(value) => void chooseCity(value)} value={cityCode}>
              <SelectTrigger className="w-full"><SelectValue placeholder={t("adForm.city")} /></SelectTrigger>
              <SelectContent><SelectGroup>{cities.map((city) => <SelectItem key={city.cityCode} value={city.cityCode}>{city.name}</SelectItem>)}</SelectGroup></SelectContent>
            </Select>
            <Select disabled={!cityCode} onValueChange={setWardCode} value={wardCode}>
              <SelectTrigger className="w-full"><SelectValue placeholder={t("adForm.ward")} /></SelectTrigger>
              <SelectContent><SelectGroup>{wards.map((ward) => <SelectItem key={ward.wardCode} value={ward.wardCode}>{ward.name}</SelectItem>)}</SelectGroup></SelectContent>
            </Select>
          </div>
          <Button disabled={!selectedProvince} onClick={addTargetRegion} type="button" variant="outline">
            <PlusIcon data-icon="inline-start" /> {t("adForm.addRegion")}
          </Button>
          <div className="flex flex-wrap gap-2">
            {targetRegions.map((region, index) => (
              <Badge key={`${region.province}-${region.city}-${region.ward}`} variant="secondary">
                {[region.province, region.city, region.ward].filter(Boolean).join(" · ")}
                <Button aria-label={t("adForm.removeRegion")} onClick={() => setTargetRegions((current) => current.filter((_, itemIndex) => itemIndex !== index))} size="icon-sm" type="button" variant="ghost">
                  <Trash2Icon />
                </Button>
              </Badge>
            ))}
          </div>
          <FieldDescription>{t("adForm.regionsHint")}</FieldDescription>
        </Field>

        <Field orientation="horizontal">
          <Checkbox checked={activateNow} id="activate-now" onCheckedChange={(checked) => setActivateNow(checked === true)} />
          <FieldLabel htmlFor="activate-now">{t("adForm.activateNow")}</FieldLabel>
        </Field>
      </FieldGroup>

      <Button disabled={isSubmitting || isUploading || payments.length === 0} type="submit">
        {isUploading ? <UploadIcon data-icon="inline-start" /> : <PlusIcon data-icon="inline-start" />}
        {isSubmitting ? t("adForm.submitting") : t("adForm.submit")}
      </Button>
    </form>
  );
}
