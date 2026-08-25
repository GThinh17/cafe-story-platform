"use client";

import { useCallback, useEffect, useState } from "react";
import { AlertTriangleIcon, Link2Icon, RefreshCwIcon } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { getAdminReportAiPolicyCopy } from "@/features/reports/admin-report-ai-policy-copy";
import { localizeApiError, useI18n, useUiText } from "@/features/i18n";
import { getReportAiPolicy } from "@/lib/api/admin";
import type {
  AdminReportAiPolicy,
  AdminReportAiPolicyRule,
  AdminReportAiResolution,
  ContentReport,
} from "@/types/admin";

type AdminReportAiPolicySheetProps = {
  open: boolean;
  report: ContentReport | null;
  latestResolution: AdminReportAiResolution | null;
  onOpenChange: (open: boolean) => void;
};

function CodeList({ values, emptyLabel }: { values: string[]; emptyLabel: string }) {
  if (!values.length) return <span className="text-muted">{emptyLabel}</span>;
  return (
    <div className="flex flex-wrap gap-1">
      {values.map((value) => (
        <Badge className="font-mono text-[10px]" variant="outline" key={value}>
          {value}
        </Badge>
      ))}
    </div>
  );
}

function TechnicalRow({ identifier, children }: { identifier: string; children: React.ReactNode }) {
  return (
    <div className="grid gap-1 border-b border-border py-2 sm:grid-cols-[150px_minmax(0,1fr)]">
      <dt className="text-xs font-bold uppercase text-muted">{identifier}</dt>
      <dd className="min-w-0 break-words font-mono text-xs text-foreground">{children}</dd>
    </div>
  );
}

export function AdminReportAiPolicySheet({
  open,
  report,
  latestResolution,
  onOpenChange,
}: AdminReportAiPolicySheetProps) {
  const { locale, t } = useI18n();
  const ui = useUiText();
  const [policy, setPolicy] = useState<AdminReportAiPolicy | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadPolicy = useCallback(async (signal?: AbortSignal) => {
    if (!report) return;
    setIsLoading(true);
    setError(null);
    try {
      setPolicy(await getReportAiPolicy(report.id, signal));
    } catch (requestError) {
      if (!signal?.aborted) {
        setPolicy(null);
        setError(localizeApiError(requestError, locale, t, "common.error.loadDetail"));
      }
    } finally {
      if (!signal?.aborted) setIsLoading(false);
    }
  }, [locale, report, t]);

  useEffect(() => {
    if (!open || !report) return;
    const controller = new AbortController();
    void loadPolicy(controller.signal);
    return () => controller.abort();
  }, [loadPolicy, open, report]);

  const findingCount = (rule: AdminReportAiPolicyRule) =>
    latestResolution?.findings?.filter((finding) => finding.ruleId === rule.ruleId).length ?? 0;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        className="z-[81] w-full overflow-y-auto sm:max-w-xl"
        overlayClassName="z-[80]"
      >
        <SheetHeader className="border-b border-border pr-12">
          <SheetTitle>{ui("AI policy")}</SheetTitle>
          <SheetDescription>
            {report
              ? ui("Policy candidates for report {id}.", { id: report.id })
              : ui("Policy candidates for this report.")}
          </SheetDescription>
        </SheetHeader>

        <div className="px-4 pb-6">
          {isLoading ? (
            <div className="flex flex-col gap-3" aria-label={ui("Loading policy...")}>
              <Skeleton className="h-8 w-48" />
              <Skeleton className="h-36 w-full" />
              <Skeleton className="h-36 w-full" />
            </div>
          ) : error ? (
            <div className="rounded-md border border-accent/30 bg-accent/10 p-4">
              <div className="flex items-start gap-2 text-sm text-accent">
                <AlertTriangleIcon className="mt-0.5 size-4 shrink-0" />
                <p>{error}</p>
              </div>
              <Button type="button" variant="outline" size="sm" className="mt-3" onClick={() => void loadPolicy()}>
                <RefreshCwIcon data-icon="inline-start" />
                {ui("Retry loading policy")}
              </Button>
            </div>
          ) : policy ? (
            <Tabs defaultValue="summary">
              <TabsList variant="line" className="w-full justify-start">
                <TabsTrigger value="summary">{ui("Summary")}</TabsTrigger>
                <TabsTrigger value="technical">{ui("Technical details")}</TabsTrigger>
              </TabsList>

              <TabsContent value="summary" className="mt-4">
                <div className="rounded-md border border-primary/20 bg-primary/5 p-3 text-sm">
                  <p className="font-bold text-espresso">{ui("Recommendation-only policy")}</p>
                  <p className="mt-1 text-muted">
                    {ui("This policy limits AI to recommendations. Only an admin can change content or report state.")}
                  </p>
                </div>
                {policy.candidateRules.length ? (
                  <div className="mt-4 flex flex-col gap-3">
                    {policy.candidateRules.map((rule) => {
                      const copy = getAdminReportAiPolicyCopy(rule.ruleId, locale);
                      const matches = findingCount(rule);
                      return (
                        <article className="rounded-md border border-border p-4" key={`${rule.ruleId}-${rule.ruleVersion}`}>
                          <div className="flex flex-wrap items-start justify-between gap-2">
                            <div>
                              <p className="font-mono text-xs font-bold text-primary">{rule.ruleId}</p>
                              <h3 className="mt-1 font-bold text-espresso">
                                {copy?.title ?? ui("Unknown policy rule")}
                              </h3>
                            </div>
                            <Badge variant="outline">{rule.ruleStatus}</Badge>
                          </div>
                          <p className="mt-3 text-sm leading-6 text-foreground">
                            {copy?.summary ?? ui("No localized description exists. Review the technical metadata below.")}
                          </p>
                          <div className="mt-3 flex flex-wrap gap-2">
                            <Badge variant="secondary">{rule.ruleFamily}</Badge>
                            <Badge variant="secondary">{rule.ruleType}</Badge>
                            {matches ? (
                              <Badge className="gap-1">
                                <Link2Icon className="size-3" />
                                {ui("Used by {count} current finding", { count: matches })}
                              </Badge>
                            ) : null}
                          </div>
                          <div className="mt-3 grid gap-3 text-xs sm:grid-cols-2">
                            <div>
                              <p className="font-bold uppercase text-muted">{ui("Required evidence")}</p>
                              <div className="mt-1"><CodeList values={rule.requiredEvidenceKinds} emptyLabel={ui("None")} /></div>
                            </div>
                            <div>
                              <p className="font-bold uppercase text-muted">{ui("Allowed actions")}</p>
                              <div className="mt-1"><CodeList values={rule.allowedCandidateActions} emptyLabel={ui("None")} /></div>
                            </div>
                          </div>
                        </article>
                      );
                    })}
                  </div>
                ) : (
                  <div className="mt-4 rounded-md border border-dashed border-border p-4 text-sm text-muted">
                    {ui("No candidate policy rule is available for this report.")}
                  </div>
                )}
              </TabsContent>

              <TabsContent value="technical" className="mt-4">
                <dl>
                  <TechnicalRow identifier="reportId">{policy.reportId}</TechnicalRow>
                  <TechnicalRow identifier="targetType">{policy.targetType}</TechnicalRow>
                  <TechnicalRow identifier="reasonCode">{policy.reasonCode || "—"}</TechnicalRow>
                  <TechnicalRow identifier="contextSchemaVersion">{policy.contextSchemaVersion}</TechnicalRow>
                  <TechnicalRow identifier="policyVersion">{policy.policyVersion}</TechnicalRow>
                  <TechnicalRow identifier="policyStatus">{policy.policyStatus}</TechnicalRow>
                  <TechnicalRow identifier="ruleCatalogVersion">{policy.ruleCatalogVersion}</TechnicalRow>
                  <TechnicalRow identifier="ruleCatalogStatus">{policy.ruleCatalogStatus}</TechnicalRow>
                  <TechnicalRow identifier="evaluationMode">{policy.evaluationMode}</TechnicalRow>
                  <TechnicalRow identifier="recommendationOnly">{String(policy.recommendationOnly)}</TechnicalRow>
                </dl>
                <div className="mt-5 flex flex-col gap-4">
                  {policy.candidateRules.map((rule) => (
                    <section className="rounded-md border border-border p-4" key={`technical-${rule.ruleId}`}>
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <p className="font-mono text-sm font-bold text-espresso">{rule.ruleId}</p>
                        <span className="font-mono text-xs text-muted">{rule.ruleVersion}</span>
                      </div>
                      <dl className="mt-3">
                        <TechnicalRow identifier="applicableTargetTypes"><CodeList values={rule.applicableTargetTypes} emptyLabel={ui("None")} /></TechnicalRow>
                        <TechnicalRow identifier="requirementProfileIds"><CodeList values={rule.requirementProfileIds} emptyLabel={ui("None")} /></TechnicalRow>
                        <TechnicalRow identifier="requiredEvidenceKinds"><CodeList values={rule.requiredEvidenceKinds} emptyLabel={ui("None")} /></TechnicalRow>
                        <TechnicalRow identifier="semanticRequirementCodes"><CodeList values={rule.semanticRequirementCodes} emptyLabel={ui("None")} /></TechnicalRow>
                        <TechnicalRow identifier="counterEvidenceRequired">{String(rule.counterEvidenceRequired)}</TechnicalRow>
                        <TechnicalRow identifier="exceptionCodes"><CodeList values={rule.exceptionCodes} emptyLabel={ui("None")} /></TechnicalRow>
                        <TechnicalRow identifier="evaluationCeiling">{rule.evaluationCeiling}</TechnicalRow>
                        <TechnicalRow identifier="allowedOutcomes"><CodeList values={rule.allowedOutcomes} emptyLabel={ui("None")} /></TechnicalRow>
                        <TechnicalRow identifier="allowedCandidateActions"><CodeList values={rule.allowedCandidateActions} emptyLabel={ui("None")} /></TechnicalRow>
                      </dl>
                      {rule.conditionalRequirements.length ? (
                        <div className="mt-3 rounded-md bg-surface p-3 font-mono text-xs">
                          {rule.conditionalRequirements.map((requirement) => (
                            <p className="break-words" key={requirement.requirementCode}>
                              {requirement.requirementCode}: {requirement.evidenceKind} / {requirement.requirementType} / {requirement.trigger} / {requirement.missingBehavior}
                            </p>
                          ))}
                        </div>
                      ) : null}
                      {findingCount(rule) ? (
                        <div className="mt-3 rounded-md border border-primary/20 bg-primary/5 p-3">
                          <p className="text-xs font-bold uppercase text-muted">{ui("Current findings for this Rule ID")}</p>
                          {latestResolution?.findings
                            ?.filter((finding) => finding.ruleId === rule.ruleId)
                            .map((finding) => (
                              <p className="mt-2 text-sm leading-6" key={`${finding.ruleId}-${finding.ruleVersion}`}>
                                {finding.rationale}
                              </p>
                            ))}
                        </div>
                      ) : null}
                    </section>
                  ))}
                </div>
              </TabsContent>
            </Tabs>
          ) : (
            <div className="rounded-md border border-dashed border-border p-4 text-sm text-muted">
              {ui("Policy data is unavailable.")}
            </div>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
}
