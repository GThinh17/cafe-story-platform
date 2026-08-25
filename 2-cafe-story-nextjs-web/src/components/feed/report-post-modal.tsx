"use client";

import { ArrowLeftIcon, ChevronRightIcon, XIcon } from "lucide-react";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { useI18n } from "@/components/providers/locale-provider";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { createContentReport, getReportReasons } from "@/lib/api/reports";
import type { Locale, Translate, TranslationKey } from "@/lib/i18n";
import { cn } from "@/lib/utils";
import type { ReportReasonResponse } from "@/types/report";

type ReportPostModalProps = {
  blogId: string;
  onOpenChange: (open: boolean) => void;
  open: boolean;
};

type ReportReasonCopy = {
  descriptionKey?: TranslationKey;
  labelKey: TranslationKey;
};

/**
 * The backend already ships Vietnamese labels (`labelVi`/`descriptionVi`), so
 * Vietnamese prefers the server copy and English falls back to this map.
 */
const REPORT_REASON_COPY: Record<string, ReportReasonCopy> = {
  BULLYING_OR_UNWANTED_CONTACT: { labelKey: "report.reason.bullying" },
  DISLIKE_CONTENT: { labelKey: "report.reason.dislike" },
  FALSE_INFORMATION: { labelKey: "report.reason.falseInfo" },
  INTELLECTUAL_PROPERTY: {
    descriptionKey: "report.reason.ipHint",
    labelKey: "report.reason.ip",
  },
  NUDITY_OR_SEXUAL_ACTIVITY: { labelKey: "report.reason.nudity" },
  RESTRICTED_GOODS: { labelKey: "report.reason.restrictedGoods" },
  SCAM_FRAUD_OR_SPAM: { labelKey: "report.reason.scam" },
  SELF_HARM_OR_ABNORMAL_EATING: { labelKey: "report.reason.selfHarm" },
  VIOLENCE_HATE_OR_EXPLOITATION: { labelKey: "report.reason.violence" },
};

function reportReasonLabel(
  reason: ReportReasonResponse,
  locale: Locale,
  t: Translate,
) {
  if (locale === "vi") {
    return reason.labelVi;
  }

  const labelKey = REPORT_REASON_COPY[reason.code]?.labelKey;

  return labelKey ? t(labelKey) : reason.labelVi;
}

function reportReasonDescription(
  reason: ReportReasonResponse,
  locale: Locale,
  t: Translate,
) {
  if (locale === "vi" && reason.descriptionVi) {
    return reason.descriptionVi;
  }

  const descriptionKey = REPORT_REASON_COPY[reason.code]?.descriptionKey;

  return descriptionKey ? t(descriptionKey) : t("report.defaultHint");
}

export function ReportPostModal({
  blogId,
  onOpenChange,
  open,
}: ReportPostModalProps) {
  const { locale, t } = useI18n();
  const [description, setDescription] = useState("");
  const [error, setError] = useState("");
  const [isLoadingReasons, setIsLoadingReasons] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [reasons, setReasons] = useState<ReportReasonResponse[]>([]);
  const [selectedReason, setSelectedReason] = useState<ReportReasonResponse | null>(null);
  const [submittedReportId, setSubmittedReportId] = useState<string | null>(null);

  useEffect(() => {
    if (!open) {
      setDescription("");
      setError("");
      setSelectedReason(null);
      setSubmittedReportId(null);
      return;
    }

    let isActive = true;
    setIsLoadingReasons(true);
    setError("");

    getReportReasons("BLOG")
      .then((response) => {
        if (isActive) {
          setReasons(response);
        }
      })
      .catch((requestError) => {
        if (isActive) {
          setReasons([]);
          setError(
            requestError instanceof Error
              ? requestError.message
              : t("report.loadError"),
          );
        }
      })
      .finally(() => {
        if (isActive) {
          setIsLoadingReasons(false);
        }
      });

    return () => {
      isActive = false;
    };
  }, [open, t]);

  const isDescriptionRequired = Boolean(selectedReason?.requiresDescription);
  const canSubmitReport =
    Boolean(selectedReason) &&
    !isSubmitting &&
    (!isDescriptionRequired || Boolean(description.trim()));

  function handleBackToList() {
    setDescription("");
    setError("");
    setSelectedReason(null);
  }

  async function handleSubmitReport() {
    if (!selectedReason || !canSubmitReport) {
      return;
    }

    setIsSubmitting(true);
    setError("");

    try {
      const response = await createContentReport({
        description: description.trim() || undefined,
        reasonId: selectedReason.id,
        targetId: blogId,
        targetType: "BLOG",
      });

      setSubmittedReportId(response.id);
    } catch (requestError) {
      const message =
        requestError instanceof Error
          ? requestError.message
          : t("report.submitError");
      setError(message);
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  }

  function renderReasonList() {
    if (isLoadingReasons) {
      return (
        <div className="flex flex-col items-center justify-center gap-3 px-6 py-16 text-center">
          <span
            aria-hidden="true"
            className="size-6 animate-spin rounded-full border-2 border-primary/30 border-t-primary"
          />
          <p className="text-sm text-muted">{t("report.loadingReasons")}</p>
        </div>
      );
    }

    if (error && !reasons.length) {
      return (
        <div className="flex flex-col items-center justify-center gap-2 px-6 py-16 text-center">
          <p className="text-base font-bold text-foreground">
            {t("report.loadErrorTitle")}
          </p>
          <p className="text-sm text-muted">{error}</p>
        </div>
      );
    }

    if (!reasons.length) {
      return (
        <div className="flex flex-col items-center justify-center gap-2 px-6 py-16 text-center">
          <p className="text-base font-bold text-foreground">
            {t("report.emptyTitle")}
          </p>
          <p className="text-sm text-muted">
            {t("report.emptyDescription")}
          </p>
        </div>
      );
    }

    return (
      <div className="flex flex-col">
        <div className="px-6 pt-4 pb-6">
          <DialogTitle className="text-center text-lg font-bold">
            {t("report.listTitle")}
          </DialogTitle>
          <DialogDescription className="mt-2 text-center text-sm text-muted">
            {t("report.listDescription")}
          </DialogDescription>
        </div>
        <ul className="flex flex-col border-t border-line-soft">
          {reasons.map((reason) => (
            <li key={reason.id}>
              <button
                aria-label={reportReasonLabel(reason, locale, t)}
                className="flex w-full cursor-pointer items-center justify-between gap-3 border-b border-line-soft px-6 py-4 text-left text-sm font-semibold text-foreground outline-none"
                onClick={() => {
                  setDescription("");
                  setError("");
                  setSelectedReason(reason);
                }}
                type="button"
              >
                <span className="flex-1">
                  {reportReasonLabel(reason, locale, t)}
                </span>
                <ChevronRightIcon className="size-5 shrink-0 text-muted" />
              </button>
            </li>
          ))}
        </ul>
      </div>
    );
  }

  function renderReasonDetails() {
    if (!selectedReason) {
      return null;
    }

    if (submittedReportId) {
      return (
        <div className="flex flex-col items-center gap-4 px-6 py-12 text-center">
          <p className="text-lg font-bold text-foreground">
            {t("report.thanksTitle")}
          </p>
          <p className="max-w-sm text-sm text-muted">
            {t("report.thanksDescription")}
          </p>
          <Button
            className="mt-2 min-w-32"
            onClick={() => onOpenChange(false)}
            type="button"
          >
            {t("common.close")}
          </Button>
        </div>
      );
    }

    return (
      <div className="flex flex-col">
        <div className="px-6 pt-2 pb-6">
          <DialogTitle className="text-center text-lg font-bold">
            {reportReasonLabel(selectedReason, locale, t)}
          </DialogTitle>
          <DialogDescription className="mt-2 text-center text-sm text-muted">
            {reportReasonDescription(selectedReason, locale, t)}
          </DialogDescription>
        </div>

        <div className="flex flex-col gap-2 px-6 pb-4">
          <label className="text-xs font-bold text-foreground" htmlFor="report-details">
            {t(
              isDescriptionRequired
                ? "report.detailsLabelRequired"
                : "report.detailsLabelOptional",
            )}
          </label>
          <Textarea
            id="report-details"
            onChange={(event) => setDescription(event.target.value)}
            placeholder={t("report.detailsPlaceholder")}
            value={description}
          />
          {error ? (
            <p className="text-xs font-semibold text-destructive">{error}</p>
          ) : null}
        </div>

        <div className="flex items-center justify-end gap-2 border-t border-line-soft px-6 py-4">
          <Button
            onClick={handleBackToList}
            type="button"
            variant="outline"
          >
            {t("common.back")}
          </Button>
          <Button
            disabled={!canSubmitReport}
            onClick={handleSubmitReport}
            type="button"
          >
            {isSubmitting ? t("report.submitting") : t("report.submit")}
          </Button>
        </div>
      </div>
    );
  }

  return (
    <Dialog onOpenChange={onOpenChange} open={open}>
      <DialogContent className="w-[92vw] max-w-md overflow-hidden p-0">
        <div className="flex items-center gap-2 border-b border-line-soft px-4 py-3">
          {selectedReason && !submittedReportId ? (
            <button
              aria-label={t("report.backToReasons")}
              className={cn(
                "grid size-8 cursor-pointer place-items-center rounded-full text-foreground outline-none",
              )}
              onClick={handleBackToList}
              type="button"
            >
              <ArrowLeftIcon className="size-5" />
            </button>
          ) : (
            <span className="size-8" aria-hidden="true" />
          )}
          <p className="flex-1 text-center text-sm font-bold text-foreground">
            {t("report.headerTitle")}
          </p>
          <button
            aria-label={t("report.close")}
            className="grid size-8 cursor-pointer place-items-center rounded-full text-foreground outline-none"
            onClick={() => onOpenChange(false)}
            type="button"
          >
            <XIcon className="size-5" />
          </button>
        </div>

        <div className="max-h-[70vh] overflow-y-auto">
          {selectedReason ? renderReasonDetails() : renderReasonList()}
        </div>
      </DialogContent>
    </Dialog>
  );
}
