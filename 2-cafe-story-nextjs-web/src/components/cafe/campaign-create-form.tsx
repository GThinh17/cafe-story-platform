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
import { useRouter } from "next/navigation";
import { ImagePlus } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { useCurrentUser } from "@/hooks/use-current-user";
import { createAdCampaign } from "@/lib/api/ad-campaigns";
import { getCafePagesByOwnerId } from "@/lib/api/cafes";
import { ApiError } from "@/lib/api/client";
import { uploadPostImageToCloudinary } from "@/lib/api/cloudinary";
import { getPayment } from "@/lib/api/payments";
import type { CafePageResponse } from "@/types/cafe";

const MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
const MAX_TITLE_LENGTH = 160;

type CampaignCreateFormProps = {
  paymentId: string;
};

type FormStatus = { error: string | null; success: string | null };

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) return error.message;
  return fallback;
}

export function CampaignCreateForm({ paymentId }: CampaignCreateFormProps) {
  const router = useRouter();
  const { user, isLoading: isUserLoading } = useCurrentUser();

  const [cafe, setCafe] = useState<CafePageResponse | null>(null);
  const [isLoadingCafe, setIsLoadingCafe] = useState(true);
  const [preloadError, setPreloadError] = useState<string | null>(null);
  const [isVerifyingPayment, setIsVerifyingPayment] = useState(Boolean(paymentId));

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [targetUrl, setTargetUrl] = useState("");
  const [priority, setPriority] = useState(1);
  const [activateNow, setActivateNow] = useState(true);

  const [imageFile, setImageFile] = useState<File | null>(null);
  const [isUploadingImage, setIsUploadingImage] = useState(false);
  const imageInputRef = useRef<HTMLInputElement | null>(null);

  const [status, setStatus] = useState<FormStatus>({ error: null, success: null });
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isUserLoading) return;
    if (!user?.userId) {
      setPreloadError("You must be signed in to create a campaign.");
      setIsLoadingCafe(false);
      return;
    }

    let isMounted = true;
    setIsLoadingCafe(true);

    getCafePagesByOwnerId(user.userId)
      .then((cafes) => {
        if (!isMounted) return;
        const active =
          cafes.find((c) => c.pageActive === true || c.status === "ACTIVE") ??
          cafes[0] ??
          null;
        setCafe(active);
        if (!active) {
          setPreloadError(
            "You need to create a cafe page before running an advertising campaign.",
          );
        }
      })
      .catch(() => {
        if (isMounted) setPreloadError("Unable to load your cafe page.");
      })
      .finally(() => {
        if (isMounted) setIsLoadingCafe(false);
      });

    return () => {
      isMounted = false;
    };
  }, [isUserLoading, user?.userId]);

  useEffect(() => {
    if (!paymentId) {
      setIsVerifyingPayment(false);
      return;
    }

    let isMounted = true;
    setIsVerifyingPayment(true);

    getPayment(paymentId)
      .then((payment) => {
        if (!isMounted) return;
        if (!payment.adFeeId) {
          setPreloadError(
            "This payment is not an advertising package. Please choose a valid ad-fee payment.",
          );
        } else if (String(payment.paymentStatus).toUpperCase() !== "PAID") {
          setPreloadError(
            `Payment status is ${payment.paymentStatus}. Please complete payment first.`,
          );
        }
      })
      .catch(() => {
        if (isMounted) {
          setPreloadError("Unable to verify the payment attached to this campaign.");
        }
      })
      .finally(() => {
        if (isMounted) setIsVerifyingPayment(false);
      });

    return () => {
      isMounted = false;
    };
  }, [paymentId]);

  useEffect(() => {
    return () => {
      if (imageUrl.startsWith("blob:")) {
        URL.revokeObjectURL(imageUrl);
      }
    };
  }, [imageUrl]);

  const isPreloading = isUserLoading || isLoadingCafe || isVerifyingPayment;
  const missingPaymentId = !paymentId;
  const canSubmit = useMemo(
    () =>
      !isPreloading &&
      !missingPaymentId &&
      !preloadError &&
      Boolean(cafe) &&
      title.trim().length > 0,
    [cafe, isPreloading, missingPaymentId, preloadError, title],
  );

  function pickImageFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      setStatus({ error: "Please choose an image file.", success: null });
      event.target.value = "";
      return;
    }
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      setStatus({ error: "Image must be 5MB or smaller.", success: null });
      event.target.value = "";
      return;
    }

    const previewUrl = URL.createObjectURL(file);
    setImageUrl((current) => {
      if (current.startsWith("blob:")) URL.revokeObjectURL(current);
      return previewUrl;
    });
    setImageFile(file);
    setStatus({ error: null, success: null });
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!cafe || !paymentId) return;
    if (!canSubmit) return;

    setIsSubmitting(true);
    setStatus({ error: null, success: null });

    try {
      let finalImageUrl = imageUrl.startsWith("blob:") ? "" : imageUrl.trim();

      if (imageFile) {
        setIsUploadingImage(true);
        finalImageUrl = await uploadPostImageToCloudinary(imageFile);
        setIsUploadingImage(false);
      }

      const created = await createAdCampaign({
        paymentId,
        cafePageId: cafe.id,
        title: title.trim(),
        description: description.trim() || undefined,
        imageUrl: finalImageUrl || undefined,
        targetUrl: targetUrl.trim() || undefined,
        priority: Number.isFinite(priority) ? priority : 1,
        activateNow,
      });

      setStatus({
        error: null,
        success: activateNow ? "Campaign is live." : "Campaign saved as draft.",
      });
      router.replace(`/cafes/${created.cafePageId}`);
    } catch (error) {
      setStatus({
        error: getErrorMessage(error, "Unable to create campaign."),
        success: null,
      });
    } finally {
      setIsUploadingImage(false);
      setIsSubmitting(false);
    }
  }

  return (
    <div className="space-y-6">
      <header className="space-y-2">
        <h1 className="text-2xl font-black text-foreground sm:text-3xl">
          Create advertising campaign
        </h1>
        <p className="max-w-[560px] text-sm leading-6 text-muted">
          Design a sponsored placement for your cafe page in the CafeStory feed.
          You have 10,000 impressions or 30 days of runtime.
        </p>
        {cafe ? (
          <p className="text-xs text-muted">
            Target cafe page:{" "}
            <Link
              className="font-semibold text-primary hover:underline"
              href={`/cafes/${cafe.id}`}
            >
              {cafe.name}
            </Link>
          </p>
        ) : null}
      </header>

      {missingPaymentId ? (
        <div className="rounded-md border border-border bg-surface-muted p-4 text-sm text-muted">
          Missing paymentId. Purchase a Feed Advertising Pack from the pricing
          plan to start a new campaign.
        </div>
      ) : null}

      {preloadError ? (
        <div className="rounded-md border border-destructive/40 bg-destructive/10 p-4 text-sm text-destructive">
          {preloadError}
        </div>
      ) : null}

      <form
        className="space-y-5 rounded-md border border-border bg-surface p-4 sm:p-5"
        onSubmit={handleSubmit}
      >
        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="campaignTitle">Campaign title</FieldLabel>
            <Input
              id="campaignTitle"
              maxLength={MAX_TITLE_LENGTH}
              onChange={(event) => setTitle(event.target.value)}
              placeholder="Weekend flat-white promo"
              required
              value={title}
            />
            <FieldDescription>
              Up to {MAX_TITLE_LENGTH} characters. Shows as the ad headline.
            </FieldDescription>
          </Field>

          <Field>
            <FieldLabel htmlFor="campaignDescription">Description</FieldLabel>
            <Textarea
              id="campaignDescription"
              onChange={(event) => setDescription(event.target.value)}
              placeholder="Give reviewers a reason to visit this week."
              rows={4}
              value={description}
            />
          </Field>

          <Field>
            <FieldLabel>Cover image</FieldLabel>
            <div className="grid gap-3 sm:grid-cols-[160px_minmax(0,1fr)] sm:items-start">
              <div className="grid h-32 place-items-center overflow-hidden rounded-md border border-dashed border-border bg-surface-muted">
                {imageUrl ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    alt=""
                    className="size-full object-cover"
                    src={imageUrl}
                  />
                ) : (
                  <ImagePlus className="size-6 text-muted" />
                )}
              </div>
              <div className="space-y-2">
                <Button
                  disabled={isSubmitting || isUploadingImage}
                  onClick={() => imageInputRef.current?.click()}
                  type="button"
                  variant="outline"
                >
                  {isUploadingImage ? "Uploading..." : "Choose image"}
                </Button>
                <FieldDescription>
                  JPG or PNG, up to 5MB. Recommended 1200×628 for feed banners.
                </FieldDescription>
                <Input
                  accept="image/*"
                  className="sr-only"
                  onChange={pickImageFile}
                  ref={imageInputRef}
                  type="file"
                />
              </div>
            </div>
          </Field>

          <Field>
            <FieldLabel htmlFor="campaignTargetUrl">Destination link</FieldLabel>
            <Input
              id="campaignTargetUrl"
              onChange={(event) => setTargetUrl(event.target.value)}
              placeholder="https://cafestory.vn/cafes/..."
              type="url"
              value={targetUrl}
            />
            <FieldDescription>
              Optional. Where users go when they tap the ad. Defaults to your
              cafe page when left empty.
            </FieldDescription>
          </Field>

          <Field>
            <FieldLabel htmlFor="campaignPriority">Priority</FieldLabel>
            <Input
              id="campaignPriority"
              inputMode="numeric"
              max={10}
              min={1}
              onChange={(event) =>
                setPriority(
                  Math.max(1, Math.min(10, Number(event.target.value) || 1)),
                )
              }
              type="number"
              value={priority}
            />
            <FieldDescription>
              Higher priority campaigns are surfaced earlier when multiple are
              eligible. Range 1–10.
            </FieldDescription>
          </Field>

          <label
            className="flex items-start justify-between gap-4"
            htmlFor="campaignActivateNow"
          >
            <span className="flex flex-col gap-1">
              <span className="text-sm font-medium text-foreground">
                Activate immediately
              </span>
              <span className="text-xs text-muted">
                Start delivering right after saving. Turn off to save as draft.
              </span>
            </span>
            <Switch
              checked={activateNow}
              disabled={isSubmitting}
              id="campaignActivateNow"
              onCheckedChange={setActivateNow}
            />
          </label>
        </FieldGroup>

        {status.error ? <FieldError>{status.error}</FieldError> : null}
        {status.success ? (
          <p className="text-sm font-medium text-primary">{status.success}</p>
        ) : null}

        <div className="flex flex-wrap gap-2">
          <Button disabled={!canSubmit || isSubmitting} type="submit">
            {isSubmitting ? "Creating..." : "Create campaign"}
          </Button>
          <Button
            disabled={isSubmitting}
            onClick={() => router.push(cafe ? `/cafes/${cafe.id}` : "/cafes/edit")}
            type="button"
            variant="ghost"
          >
            Do it later
          </Button>
        </div>
      </form>
    </div>
  );
}
