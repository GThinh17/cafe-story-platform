package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionWebhookResponseDTO;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.ReportTargetType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AdminReportAiSemanticValidator {

    private static final Set<String> EVIDENCE_QUALITY =
            Set.of("HIGH", "MEDIUM", "LOW", "UNUSABLE");
    private static final Set<String> EVIDENCE_SUFFICIENCY =
            Set.of("SUFFICIENT", "INSUFFICIENT", "CONFLICTED", "UNASSESSABLE");
    private static final Set<String> VIOLATION_LIKELIHOOD =
            Set.of("HIGH", "MEDIUM", "LOW", "UNKNOWN");
    private static final Set<String> HARM_SEVERITY =
            Set.of("CRITICAL", "HIGH", "MEDIUM", "LOW", "UNKNOWN");

    private AdminReportAiSemanticValidator() {
    }

    public static ValidationResult validate(
            AdminReportAiResolutionRequestDTO request,
            AdminReportAiResolutionWebhookResponseDTO response) {
        List<String> blockedReasons = new ArrayList<>(
                response.getBlockedReasons() == null ? List.of() : response.getBlockedReasons());

        if (!"2.0".equals(response.getContractVersion())
                || response.getCorrelationId() == null
                || !response.getCorrelationId().equals(request.getCorrelationId())) {
            blockedReasons.add("CONTRACT_OR_CORRELATION_MISMATCH");
        }
        if (!AdminReportAiPolicyCatalog.POLICY_VERSION.equals(response.getPolicyVersion())
                || !AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION.equals(response.getRuleCatalogVersion())
                || !AdminReportAiPolicyCatalog.PROMPT_VERSION.equals(response.getPromptVersion())
                || !AdminReportAiPolicyCatalog.WORKFLOW_VERSION.equals(response.getWorkflowVersion())) {
            blockedReasons.add("VERSION_PIN_MISMATCH");
        }
        if (!EVIDENCE_QUALITY.contains(response.getEvidenceQuality())
                || !EVIDENCE_SUFFICIENCY.contains(response.getEvidenceSufficiency())
                || !VIOLATION_LIKELIHOOD.contains(response.getViolationLikelihood())
                || !HARM_SEVERITY.contains(response.getHarmSeverity())) {
            blockedReasons.add("INVALID_CATEGORICAL_SEMANTICS");
        }

        Set<String> evidenceIds = new HashSet<>();
        if (request.getEvidence() != null) {
            request.getEvidence().forEach(item -> evidenceIds.add(String.valueOf(item.get("evidenceId"))));
        }
        if (response.getReportDecision() != AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW
                && !validFindings(request.getReasonCode(), response.getFindings(), evidenceIds)) {
            blockedReasons.add("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
        }
        if (!"SUFFICIENT".equals(response.getEvidenceSufficiency())) {
            blockedReasons.add("EVIDENCE_NOT_SUFFICIENT");
        }
        if (request.getExecutionConstraints() != null
                && Boolean.TRUE.equals(request.getExecutionConstraints().get("criticalEvidenceMissing"))) {
            blockedReasons.add("CRITICAL_EVIDENCE_MISSING");
        }

        boolean manual = !blockedReasons.isEmpty()
                || response.getReportDecision() == AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW;
        if (manual) {
            response.setRecommendationState("NEEDS_MANUAL_REVIEW");
            response.setReportDecision(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
            response.setTargetAction(AdminReportAiTargetAction.NO_ACTION);
            response.setEvidenceSufficiency(
                    "SUFFICIENT".equals(response.getEvidenceSufficiency())
                            ? "UNASSESSABLE"
                            : response.getEvidenceSufficiency());
            response.setFindings(List.of());
        }
        response.setBlockedReasons(blockedReasons.stream().distinct().toList());
        return new ValidationResult(response, manual);
    }

    public static boolean isDecisionActionAllowed(
            ReportTargetType targetType,
            AdminReportAiReportDecision decision,
            AdminReportAiTargetAction action) {
        if (decision == AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW) {
            return action == AdminReportAiTargetAction.NO_ACTION || action == AdminReportAiTargetAction.NONE;
        }
        if (targetType == ReportTargetType.USER || targetType == ReportTargetType.CAFE_PAGE) {
            return false;
        }
        if (decision == AdminReportAiReportDecision.REJECT) {
            return action == AdminReportAiTargetAction.KEEP_VISIBLE
                    || action == AdminReportAiTargetAction.APPROVE;
        }
        return action == AdminReportAiTargetAction.HIDE
                || action == AdminReportAiTargetAction.REMOVE;
    }

    private static boolean validFindings(
            String reasonCode,
            List<Map<String, Object>> findings,
            Set<String> evidenceIds) {
        if (findings == null || findings.isEmpty()) {
            return false;
        }
        for (Map<String, Object> finding : findings) {
            String ruleId = String.valueOf(finding.get("ruleId"));
            if (!AdminReportAiPolicyCatalog.containsRule(reasonCode, ruleId)) {
                return false;
            }
            if (!allKnownEvidence(finding.get("evidenceIds"), evidenceIds)
                    || !allKnownEvidence(finding.get("counterEvidenceIds"), evidenceIds)) {
                return false;
            }
        }
        return true;
    }

    private static boolean allKnownEvidence(Object references, Set<String> evidenceIds) {
        if (references == null) {
            return true;
        }
        if (!(references instanceof List<?> values)) {
            return false;
        }
        return values.stream().map(String::valueOf).allMatch(evidenceIds::contains);
    }

    public record ValidationResult(AdminReportAiResolutionWebhookResponseDTO response, boolean clampedToManual) {
    }
}
