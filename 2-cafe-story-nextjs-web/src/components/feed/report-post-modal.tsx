"use client";

import { ArrowLeftIcon, ChevronRightIcon, XIcon } from "lucide-react";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { createContentReport, getReportReasons } from "@/lib/api/reports";
import { cn } from "@/lib/utils";
import type { ReportReasonResponse } from "@/types/report";

type ReportPostModalProps = {
  blogId: string;
  onOpenChange: (open: boolean) => void;
  open: boolean;
};

type ReportReasonCopy = {
  description?: string;
  label: string;
};

const REPORT_REASON_COPY: Record<string, ReportReasonCopy> = {
  BULLYING_OR_UNWANTED_CONTACT: {
    label: "Bullying or unwanted contact",
  },
  DISLIKE_CONTENT: {
    label: "I just don't like this content",
  },
  FALSE_INFORMATION: {
    label: "False information",
  },
  INTELLECTUAL_PROPERTY: {
    description:
      "Tell CafeStory what rights may be affected so the team can review the report accurately.",
    label: "Intellectual property",
  },
  NUDITY_OR_SEXUAL_ACTIVITY: {
    label: "Nudity or sexual activity",
  },
  RESTRICTED_GOODS: {
    label: "Selling or promoting restricted goods",
  },
  SCAM_FRAUD_OR_SPAM: {
    label: "Scam, fraud, or spam",
  },
  SELF_HARM_OR_ABNORMAL_EATING: {
    label: "Self-harm or disordered eating",
  },
  VIOLENCE_HATE_OR_EXPLOITATION: {
    label: "Violence, hate, or exploitation",
  },
};

function reportReasonLabel(reason: ReportReasonResponse) {
  return REPORT_REASON_COPY[reason.code]?.label ?? reason.labelVi;
}

function reportReasonDescription(reason: ReportReasonResponse) {
  return (
    REPORT_REASON_COPY[reason.code]?.description ??
    "Tell CafeStory a little more so the team can review this accurately."
  );
}

export function ReportPostModal({
  blogId,
  onOpenChange,
  open,
}: ReportPostModalProps) {
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
              : "Unable to load report reasons.",
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
  }, [open]);

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
          : "Unable to submit this report.";
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
          <p className="text-sm text-muted">Loading report reasons...</p>
        </div>
      );
    }

    if (error && !reasons.length) {
      return (
        <div className="flex flex-col items-center justify-center gap-2 px-6 py-16 text-center">
          <p className="text-base font-bold text-foreground">
            Unable to load report reasons
          </p>
          <p className="text-sm text-muted">{error}</p>
        </div>
      );
    }

    if (!reasons.length) {
      return (
        <div className="flex flex-col items-center justify-center gap-2 px-6 py-16 text-center">
          <p className="text-base font-bold text-foreground">
            No report reasons yet
          </p>
          <p className="text-sm text-muted">
            Please try again after report reasons have been configured.
          </p>
        </div>
      );
    }

    return (
      <div className="flex flex-col">
        <div className="px-6 pt-4 pb-6">
          <DialogTitle className="text-center text-lg font-bold">
            Why are you reporting this post?
          </DialogTitle>
          <DialogDescription className="mt-2 text-center text-sm text-muted">
            Your report is anonymous. If someone is in immediate danger, contact
            your local emergency services right away.
          </DialogDescription>
        </div>
        <ul className="flex flex-col border-t border-line-soft">
          {reasons.map((reason) => (
            <li key={reason.id}>
              <button
                aria-label={reportReasonLabel(reason)}
                className="flex w-full cursor-pointer items-center justify-between gap-3 border-b border-line-soft px-6 py-4 text-left text-sm font-semibold text-foreground outline-none"
                onClick={() => {
                  setDescription("");
                  setError("");
                  setSelectedReason(reason);
                }}
                type="button"
              >
                <span className="flex-1">{reportReasonLabel(reason)}</span>
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
            Thanks for your report
          </p>
          <p className="max-w-sm text-sm text-muted">
            Your report has been submitted and will be reviewed by CafeStory.
          </p>
          <Button
            className="mt-2 min-w-32"
            onClick={() => onOpenChange(false)}
            type="button"
          >
            Close
          </Button>
        </div>
      );
    }

    return (
      <div className="flex flex-col">
        <div className="px-6 pt-2 pb-6">
          <DialogTitle className="text-center text-lg font-bold">
            {reportReasonLabel(selectedReason)}
          </DialogTitle>
          <DialogDescription className="mt-2 text-center text-sm text-muted">
            {reportReasonDescription(selectedReason)}
          </DialogDescription>
        </div>

        <div className="flex flex-col gap-2 px-6 pb-4">
          <label className="text-xs font-bold text-foreground" htmlFor="report-details">
            Details {isDescriptionRequired ? "(required)" : "(optional)"}
          </label>
          <Textarea
            id="report-details"
            onChange={(event) => setDescription(event.target.value)}
            placeholder="Add report details..."
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
            Back
          </Button>
          <Button
            disabled={!canSubmitReport}
            onClick={handleSubmitReport}
            type="button"
          >
            {isSubmitting ? "Reporting..." : "Report"}
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
              aria-label="Back to reasons"
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
            Report
          </p>
          <button
            aria-label="Close report"
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
