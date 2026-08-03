package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiCandidateRuleRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiEvidenceItemRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiMissingRequirementRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiPolicyContextRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiResolutionRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionWebhookResponseDTO;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.ReportTargetType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class AdminReportAiSemanticValidator {

    private static final int MAX_CANDIDATE_RULES = 8;
    private static final int MAX_EVIDENCE_ITEMS = 32;
    private static final int MAX_FINDINGS = 8;
    private static final int MAX_REFERENCES = 32;
    private static final int MAX_BLOCKED_REASONS = 16;
    private static final int MAX_LABELS = 16;
    private static final int MAX_CODE_LENGTH = 128;
    private static final int MAX_TEXT_LENGTH = 4000;
    private static final Pattern CODE_PATTERN =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:-]{0,127}");
    private static final Set<String> FINDING_FIELDS = Set.of(
            "ruleId",
            "ruleVersion",
            "outcome",
            "evidenceIds",
            "counterEvidenceIds",
            "missingEvidenceIds",
            "violationLikelihood",
            "rationale");
    private static final Set<String> EVIDENCE_SUMMARY_FIELDS =
            Set.of("usedEvidenceIds", "counterEvidenceIds", "missingEvidenceIds");
    private static final Set<String> COMPLETE_OUTCOMES =
            Set.of("SUBSTANTIATED", "NOT_SUBSTANTIATED", "NOT_APPLICABLE");
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
        LinkedHashSet<String> blockedReasons = new LinkedHashSet<>();
        List<String> providerBlockedReasons = response.getBlockedReasons() == null
                ? List.of()
                : response.getBlockedReasons();

        if (!validCodeList(providerBlockedReasons, MAX_BLOCKED_REASONS, true)
                || !validCodeList(response.getLabels(), MAX_LABELS, true)
                || !validText(response.getExplanation(), 1, MAX_TEXT_LENGTH)
                || response.getFindings() == null
                || response.getFindings().size() > MAX_FINDINGS) {
            blockedReasons.add("PROVIDER_OUTPUT_BOUND_EXCEEDED");
        }
        response.setExplanation(normalizeText(response.getExplanation()));
        response.setLabels(normalizeCodes(response.getLabels(), MAX_LABELS));

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
        if (!EVIDENCE_QUALITY.contains(response.getEvidenceQuality())) {
            response.setEvidenceQuality("UNUSABLE");
        }
        if (!EVIDENCE_SUFFICIENCY.contains(response.getEvidenceSufficiency())) {
            response.setEvidenceSufficiency("UNASSESSABLE");
        }
        if (!VIOLATION_LIKELIHOOD.contains(response.getViolationLikelihood())) {
            response.setViolationLikelihood("UNKNOWN");
        }
        if (!HARM_SEVERITY.contains(response.getHarmSeverity())) {
            response.setHarmSeverity("UNKNOWN");
        }

        AdminReportAiPolicyContextRequestDTO policyContext = request.getPolicyContext();
        boolean ruleContextValid = validRuleContext(policyContext, request.getTargetType());
        if (!ruleContextValid) {
            blockedReasons.add("RULE_CONTEXT_SCHEMA_MISMATCH");
        }
        if (!isRuntimePolicyActive(policyContext)) {
            blockedReasons.add("POLICY_OR_RULE_CATALOG_NOT_ACTIVE");
        }

        Map<String, AdminReportAiCandidateRuleRequestDTO> candidateById =
                candidateRulesById(policyContext);
        if (candidateById == null) {
            blockedReasons.add("DUPLICATE_CANDIDATE_RULE_ID");
            candidateById = Map.of();
        } else if (!ruleContextValid) {
            candidateById = Map.of();
        }

        EvidenceIndex evidenceIndex = indexEvidence(request);
        if (!evidenceIndex.envelopeValid()) {
            blockedReasons.add("EVIDENCE_ENVELOPE_INVALID");
        }
        if (evidenceIndex.duplicateId()) {
            blockedReasons.add("DUPLICATE_EVIDENCE_ID");
        }
        if (evidenceIndex.snapshotBindingMismatch()) {
            blockedReasons.add("EVIDENCE_SNAPSHOT_BINDING_MISMATCH");
        }

        Set<String> missingRequirementIds = missingRequirementIds(policyContext);
        Set<String> allowedMissingReferences = new HashSet<>(evidenceIndex.byId().keySet());
        allowedMissingReferences.addAll(missingRequirementIds);
        response.setEvidenceSummary(normalizeEvidenceSummary(
                response.getEvidenceSummary(),
                evidenceIndex.byId().keySet(),
                allowedMissingReferences,
                blockedReasons));

        FindingAnalysis findingAnalysis = analyzeFindings(
                response.getFindings(),
                candidateById,
                evidenceIndex.byId(),
                allowedMissingReferences);
        if (!findingAnalysis.boundsValid()) {
            blockedReasons.add("PROVIDER_OUTPUT_BOUND_EXCEEDED");
        }
        if (!findingAnalysis.referencesValid()) {
            blockedReasons.add("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
        }
        if (!findingAnalysis.ruleVersionsMatch()) {
            blockedReasons.add("RULE_VERSION_MISMATCH");
        }
        if (!findingAnalysis.outcomesAllowed()) {
            blockedReasons.add("RULE_CONTEXT_SCHEMA_MISMATCH");
        }
        if (!findingAnalysis.independentEvidenceBasis()) {
            blockedReasons.add("UNSUPPORTED_EVIDENCE_BASIS");
        }
        if (!findingAnalysis.counterEvidenceResolved()) {
            blockedReasons.add("MATERIAL_COUNTER_EVIDENCE_UNRESOLVED");
        }

        if (hasMissingEvidenceKindBurden(candidateById.values(), evidenceIndex.byId().values())
                || hasEvidenceKindMissingRequirement(policyContext)) {
            blockedReasons.add("EVIDENCE_KIND_REQUIREMENT_MISSING");
        }
        if (hasSemanticMissingRequirement(policyContext)) {
            blockedReasons.add("SEMANTIC_REQUIREMENT_MISSING");
        }

        boolean nonManualRequested =
                response.getReportDecision() != AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW;
        if (nonManualRequested
                && !hasCompleteMaterialScope(candidateById.values(), findingAnalysis.outcomesByRule())) {
            blockedReasons.add("INCOMPLETE_RULE_EVALUATION_SCOPE");
        }
        if (!decisionMatchesAggregation(
                response.getReportDecision(), candidateById, findingAnalysis.outcomesByRule())) {
            blockedReasons.add("ACTION_BURDEN_NOT_SATISFIED");
        }
        if (!actionBurdenSatisfied(
                response.getReportDecision(),
                response.getTargetAction(),
                candidateById,
                findingAnalysis.outcomesByRule())) {
            blockedReasons.add("ACTION_BURDEN_NOT_SATISFIED");
        }
        if (nonManualRequested
                && !isDecisionActionAllowed(
                request.getTargetType(),
                response.getReportDecision(),
                response.getTargetAction())) {
            blockedReasons.add("DECISION_ACTION_NOT_ALLOWED");
        }
        if (nonManualRequested
                && exceedsEvaluationCeiling(
                policyContext,
                response.getReportDecision(),
                candidateById,
                findingAnalysis.outcomesByRule())) {
            blockedReasons.add("RULE_EVALUATION_CEILING_EXCEEDED");
        }
        if (nonManualRequested
                && !response.getReportDecision().name().equals(response.getRecommendationState())) {
            blockedReasons.add("ACTION_BURDEN_NOT_SATISFIED");
        }

        if (!"SUFFICIENT".equals(response.getEvidenceSufficiency())) {
            blockedReasons.add("EVIDENCE_NOT_SUFFICIENT");
        }
        boolean criticalEvidenceMissing = request.getExecutionConstraints() != null
                && Boolean.TRUE.equals(request.getExecutionConstraints().get("criticalEvidenceMissing"));
        if (criticalEvidenceMissing) {
            blockedReasons.add("CRITICAL_EVIDENCE_MISSING");
        }

        boolean manual = !blockedReasons.isEmpty()
                || response.getReportDecision() == AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW;
        if (manual) {
            response.setRecommendationState("NEEDS_MANUAL_REVIEW");
            response.setReportDecision(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
            response.setTargetAction(AdminReportAiTargetAction.NO_ACTION);
            response.setEvidenceSufficiency(
                    criticalEvidenceMissing || "SUFFICIENT".equals(response.getEvidenceSufficiency())
                            ? "UNASSESSABLE"
                            : response.getEvidenceSufficiency());
            response.setFindings(List.of());
        }
        providerBlockedReasons.stream()
                .filter(AdminReportAiSemanticValidator::validCode)
                .forEach(blockedReasons::add);
        response.setBlockedReasons(blockedReasons.stream().limit(MAX_BLOCKED_REASONS).toList());
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

    private static boolean validRuleContext(
            AdminReportAiPolicyContextRequestDTO context,
            ReportTargetType targetType) {
        if (context == null
                || !AdminReportAiPolicyCatalog.CONTEXT_SCHEMA_VERSION.equals(context.getContextSchemaVersion())
                || !AdminReportAiPolicyCatalog.POLICY_VERSION.equals(context.getPolicyVersion())
                || !AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION.equals(context.getRuleCatalogVersion())
                || !AdminReportAiPolicyCatalog.REQUIREMENT_MATRIX_VERSION.equals(
                context.getRequirementMatrixVersion())
                || !AdminReportAiPolicyCatalog.EVIDENCE_KIND_CATALOG_VERSION.equals(
                context.getEvidenceKindCatalogVersion())
                || context.getCandidateRules() == null
                || context.getCandidateRules().isEmpty()
                || context.getCandidateRules().size() > MAX_CANDIDATE_RULES) {
            return false;
        }
        for (AdminReportAiCandidateRuleRequestDTO rule : context.getCandidateRules()) {
            if (rule == null
                    || !validCode(rule.getRuleId())
                    || !validCode(rule.getRuleVersion())
                    || !validCode(rule.getRuleStatus())
                    || !validCode(rule.getRuleFamily())
                    || !Set.of("VIOLATION", "ROUTING").contains(rule.getRuleType())
                    || rule.getMaterial() == null
                    || rule.getApplicableTargetTypes() == null
                    || !rule.getApplicableTargetTypes().contains(targetType)
                    || !validCodeList(rule.getRequirementProfileIds(), MAX_REFERENCES, false)
                    || !validCodeList(rule.getRequiredEvidenceKinds(), MAX_REFERENCES, false)
                    || !validCodeList(rule.getSemanticRequirementCodes(), MAX_REFERENCES, false)
                    || rule.getConditionalRequirements() == null
                    || rule.getCounterEvidenceRequired() == null
                    || !validCodeList(rule.getExceptionCodes(), MAX_REFERENCES, false)
                    || !validCode(rule.getEvaluationCeiling())
                    || !validCodeList(rule.getAllowedOutcomes(), MAX_FINDINGS, false)
                    || !validCodeList(rule.getAllowedCandidateActions(), MAX_LABELS, false)) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, AdminReportAiCandidateRuleRequestDTO> candidateRulesById(
            AdminReportAiPolicyContextRequestDTO policyContext) {
        if (policyContext == null || policyContext.getCandidateRules() == null) {
            return null;
        }
        Map<String, AdminReportAiCandidateRuleRequestDTO> byId = new LinkedHashMap<>();
        for (AdminReportAiCandidateRuleRequestDTO rule : policyContext.getCandidateRules()) {
            if (rule == null || rule.getRuleId() == null || byId.put(rule.getRuleId(), rule) != null) {
                return null;
            }
        }
        return byId;
    }

    private static EvidenceIndex indexEvidence(AdminReportAiResolutionRequestDTO request) {
        List<AdminReportAiEvidenceItemRequestDTO> evidence = request.getEvidence();
        if (evidence == null || evidence.isEmpty() || evidence.size() > MAX_EVIDENCE_ITEMS) {
            return new EvidenceIndex(Map.of(), false, false, false);
        }
        Map<String, AdminReportAiEvidenceItemRequestDTO> byId = new LinkedHashMap<>();
        boolean envelopeValid = true;
        boolean duplicateId = false;
        boolean snapshotBindingMismatch = false;
        String targetSnapshotHash = request.getTargetSnapshot() == null
                ? null
                : stringValue(request.getTargetSnapshot().get("snapshotHash"));
        String targetSnapshotVersion = request.getTargetSnapshot() == null
                ? null
                : stringValue(request.getTargetSnapshot().get("snapshotVersion"));
        for (AdminReportAiEvidenceItemRequestDTO item : evidence) {
            if (!validEvidenceEnvelope(item)) {
                envelopeValid = false;
                continue;
            }
            if (byId.put(item.getEvidenceId(), item) != null) {
                duplicateId = true;
            }
            if (item.getSubject() == null
                    || targetSnapshotHash == null
                    || targetSnapshotVersion == null
                    || !targetSnapshotHash.equals(item.getSubject().getSnapshotHash())
                    || !targetSnapshotVersion.equals(item.getSubject().getSnapshotVersion())
                    || request.getTargetType() != item.getSubject().getTargetType()) {
                snapshotBindingMismatch = true;
            }
        }
        return new EvidenceIndex(byId, envelopeValid, duplicateId, snapshotBindingMismatch);
    }

    private static boolean validEvidenceEnvelope(AdminReportAiEvidenceItemRequestDTO item) {
        return item != null
                && validCode(item.getEvidenceId())
                && AdminReportAiPolicyCatalog.EVIDENCE_ENVELOPE_VERSION.equals(item.getEnvelopeVersion())
                && validCode(item.getEvidenceKind())
                && item.getSubject() != null
                && item.getSource() != null
                && item.getCapture() != null
                && item.getIntegrity() != null
                && item.getAvailability() != null
                && item.getQuality() != null
                && item.getPrivacy() != null
                && validCode(item.getIntendedUse())
                && validCodeList(item.getCollectedForRuleIds(), MAX_CANDIDATE_RULES, false)
                && item.getPayload() != null;
    }

    private static FindingAnalysis analyzeFindings(
            List<Map<String, Object>> findings,
            Map<String, AdminReportAiCandidateRuleRequestDTO> candidateById,
            Map<String, AdminReportAiEvidenceItemRequestDTO> evidenceById,
            Set<String> allowedMissingReferences) {
        if (findings == null || findings.size() > MAX_FINDINGS) {
            return FindingAnalysis.invalid();
        }
        boolean boundsValid = true;
        boolean referencesValid = true;
        boolean versionsMatch = true;
        boolean outcomesAllowed = true;
        boolean independentBasis = true;
        boolean counterResolved = true;
        Map<String, String> outcomesByRule = new LinkedHashMap<>();
        for (Map<String, Object> finding : findings) {
            if (finding == null || !finding.keySet().equals(FINDING_FIELDS)) {
                boundsValid = false;
                continue;
            }
            String ruleId = stringValue(finding.get("ruleId"));
            String ruleVersion = stringValue(finding.get("ruleVersion"));
            String outcome = stringValue(finding.get("outcome"));
            String likelihood = stringValue(finding.get("violationLikelihood"));
            String rationale = stringValue(finding.get("rationale"));
            List<String> evidenceIds = referenceList(finding.get("evidenceIds"));
            List<String> counterEvidenceIds = referenceList(finding.get("counterEvidenceIds"));
            List<String> missingEvidenceIds = referenceList(finding.get("missingEvidenceIds"));
            if (!validCode(ruleId)
                    || !validCode(ruleVersion)
                    || !VIOLATION_LIKELIHOOD.contains(likelihood)
                    || !validText(rationale, 1, MAX_TEXT_LENGTH)
                    || evidenceIds == null
                    || counterEvidenceIds == null
                    || missingEvidenceIds == null) {
                boundsValid = false;
                continue;
            }
            AdminReportAiCandidateRuleRequestDTO rule = candidateById.get(ruleId);
            if (rule == null
                    || !evidenceById.keySet().containsAll(evidenceIds)
                    || !evidenceById.keySet().containsAll(counterEvidenceIds)
                    || !allowedMissingReferences.containsAll(missingEvidenceIds)) {
                referencesValid = false;
            }
            if (rule != null
                    && (rule.getRuleVersion() == null || !rule.getRuleVersion().equals(ruleVersion))) {
                versionsMatch = false;
            }
            if (rule == null || !rule.getAllowedOutcomes().contains(outcome)) {
                outcomesAllowed = false;
            }
            if (outcomesByRule.put(ruleId, outcome) != null) {
                boundsValid = false;
            }
            if ("SUBSTANTIATED".equals(outcome)
                    && evidenceIds.stream()
                    .map(evidenceById::get)
                    .noneMatch(AdminReportAiSemanticValidator::isIndependentUsableEvidence)) {
                independentBasis = false;
            }
            if ("SUBSTANTIATED".equals(outcome)
                    && rule != null
                    && Boolean.TRUE.equals(rule.getCounterEvidenceRequired())
                    && !counterEvidenceIds.isEmpty()) {
                counterResolved = false;
            }
        }
        return new FindingAnalysis(
                boundsValid,
                referencesValid,
                versionsMatch,
                outcomesAllowed,
                independentBasis,
                counterResolved,
                outcomesByRule);
    }

    private static boolean hasCompleteMaterialScope(
            Iterable<AdminReportAiCandidateRuleRequestDTO> candidates,
            Map<String, String> outcomesByRule) {
        boolean hasMaterialRule = false;
        for (AdminReportAiCandidateRuleRequestDTO rule : candidates) {
            if (!Boolean.TRUE.equals(rule.getMaterial())) {
                continue;
            }
            hasMaterialRule = true;
            if (!isCompleteOutcome(outcomesByRule.get(rule.getRuleId()))) {
                return false;
            }
        }
        return hasMaterialRule;
    }

    private static boolean decisionMatchesAggregation(
            AdminReportAiReportDecision decision,
            Map<String, AdminReportAiCandidateRuleRequestDTO> candidateById,
            Map<String, String> outcomesByRule) {
        List<AdminReportAiCandidateRuleRequestDTO> materialRules = candidateById.values().stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getMaterial()))
                .toList();
        if (decision == AdminReportAiReportDecision.RESOLVE) {
            return materialRules.stream().anyMatch(rule ->
                    "VIOLATION".equals(rule.getRuleType())
                            && "SUBSTANTIATED".equals(outcomesByRule.get(rule.getRuleId())))
                    && materialRules.stream().allMatch(rule ->
                    isCompleteOutcome(outcomesByRule.get(rule.getRuleId())));
        }
        if (decision == AdminReportAiReportDecision.REJECT) {
            return !materialRules.isEmpty() && materialRules.stream().allMatch(rule -> {
                String outcome = outcomesByRule.get(rule.getRuleId());
                return "NOT_SUBSTANTIATED".equals(outcome) || "NOT_APPLICABLE".equals(outcome);
            });
        }
        return true;
    }

    private static boolean actionBurdenSatisfied(
            AdminReportAiReportDecision decision,
            AdminReportAiTargetAction action,
            Map<String, AdminReportAiCandidateRuleRequestDTO> candidateById,
            Map<String, String> outcomesByRule) {
        String actionName = action == null ? null : action.name();
        if (decision == AdminReportAiReportDecision.RESOLVE) {
            return candidateById.values().stream()
                    .filter(rule -> "SUBSTANTIATED".equals(outcomesByRule.get(rule.getRuleId())))
                    .allMatch(rule -> rule.getAllowedCandidateActions().contains(actionName));
        }
        if (decision == AdminReportAiReportDecision.REJECT) {
            return candidateById.values().stream()
                    .filter(rule -> Boolean.TRUE.equals(rule.getMaterial()))
                    .allMatch(rule -> rule.getAllowedCandidateActions().contains(actionName));
        }
        return action == AdminReportAiTargetAction.NO_ACTION || action == AdminReportAiTargetAction.NONE;
    }

    private static boolean exceedsEvaluationCeiling(
            AdminReportAiPolicyContextRequestDTO context,
            AdminReportAiReportDecision decision,
            Map<String, AdminReportAiCandidateRuleRequestDTO> candidateById,
            Map<String, String> outcomesByRule) {
        if (context == null || !"RESOLVE_OR_REJECT".equals(context.getCurrentEvaluationCeiling())) {
            return true;
        }
        if (decision != AdminReportAiReportDecision.RESOLVE) {
            return false;
        }
        return candidateById.values().stream()
                .filter(rule -> "SUBSTANTIATED".equals(outcomesByRule.get(rule.getRuleId())))
                .anyMatch(rule -> !"RESOLVE_OR_REJECT".equals(rule.getEvaluationCeiling()));
    }

    private static boolean hasMissingEvidenceKindBurden(
            Iterable<AdminReportAiCandidateRuleRequestDTO> candidates,
            Iterable<AdminReportAiEvidenceItemRequestDTO> evidence) {
        for (AdminReportAiCandidateRuleRequestDTO rule : candidates) {
            if (!Boolean.TRUE.equals(rule.getMaterial())) {
                continue;
            }
            for (String requiredKind : rule.getRequiredEvidenceKinds()) {
                boolean present = false;
                for (AdminReportAiEvidenceItemRequestDTO item : evidence) {
                    if (requiredKind.equals(item.getEvidenceKind())
                            && item.getCollectedForRuleIds().contains(rule.getRuleId())
                            && isIndependentUsableEvidence(item)) {
                        present = true;
                        break;
                    }
                }
                if (!present) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasEvidenceKindMissingRequirement(
            AdminReportAiPolicyContextRequestDTO context) {
        return context != null
                && context.getMissingRequirements() != null
                && context.getMissingRequirements().stream()
                .anyMatch(item -> item != null
                        && "MISSING".equals(item.getStatus())
                        && item.getEvidenceKind() != null);
    }

    private static boolean hasSemanticMissingRequirement(
            AdminReportAiPolicyContextRequestDTO context) {
        if (context == null
                || context.getMissingRequirements() == null
                || context.getCandidateRules() == null) {
            return false;
        }
        Set<String> semanticCodes = new HashSet<>();
        context.getCandidateRules().forEach(rule -> {
            if (rule != null && rule.getSemanticRequirementCodes() != null) {
                semanticCodes.addAll(rule.getSemanticRequirementCodes());
            }
        });
        return context.getMissingRequirements().stream()
                .filter(item -> item != null && "MISSING".equals(item.getStatus()))
                .map(AdminReportAiMissingRequirementRequestDTO::getRequirementCode)
                .anyMatch(semanticCodes::contains);
    }

    private static Set<String> missingRequirementIds(AdminReportAiPolicyContextRequestDTO context) {
        if (context == null || context.getMissingRequirements() == null) {
            return Set.of();
        }
        Set<String> ids = new HashSet<>();
        for (AdminReportAiMissingRequirementRequestDTO item : context.getMissingRequirements()) {
            if (item != null && validCode(item.getMissingRequirementId())) {
                ids.add(item.getMissingRequirementId());
            }
        }
        return ids;
    }

    private static Map<String, Object> normalizeEvidenceSummary(
            Map<String, Object> summary,
            Set<String> evidenceIds,
            Set<String> allowedMissingReferences,
            Set<String> blockedReasons) {
        if (summary == null || !summary.keySet().equals(EVIDENCE_SUMMARY_FIELDS)) {
            blockedReasons.add("PROVIDER_OUTPUT_BOUND_EXCEEDED");
            return Map.of(
                    "usedEvidenceIds", List.of(),
                    "counterEvidenceIds", List.of(),
                    "missingEvidenceIds", List.of());
        }
        List<String> used = referenceList(summary.get("usedEvidenceIds"));
        List<String> counter = referenceList(summary.get("counterEvidenceIds"));
        List<String> missing = referenceList(summary.get("missingEvidenceIds"));
        if (used == null || counter == null || missing == null) {
            blockedReasons.add("PROVIDER_OUTPUT_BOUND_EXCEEDED");
            return Map.of(
                    "usedEvidenceIds", List.of(),
                    "counterEvidenceIds", List.of(),
                    "missingEvidenceIds", List.of());
        }
        if (!evidenceIds.containsAll(used)
                || !evidenceIds.containsAll(counter)
                || !allowedMissingReferences.containsAll(missing)) {
            blockedReasons.add("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
        }
        return Map.of(
                "usedEvidenceIds", used.stream().filter(evidenceIds::contains).toList(),
                "counterEvidenceIds", counter.stream().filter(evidenceIds::contains).toList(),
                "missingEvidenceIds", missing.stream().filter(allowedMissingReferences::contains).toList());
    }

    private static List<String> referenceList(Object value) {
        if (!(value instanceof List<?> values)
                || values.size() > MAX_REFERENCES) {
            return null;
        }
        List<String> result = values.stream().map(AdminReportAiSemanticValidator::stringValue).toList();
        return validCodeList(result, MAX_REFERENCES, true) ? result : null;
    }

    private static boolean isIndependentUsableEvidence(AdminReportAiEvidenceItemRequestDTO evidence) {
        if (evidence == null
                || evidence.getAvailability() == null
                || !"AVAILABLE".equals(evidence.getAvailability().getStatus())
                || evidence.getQuality() == null
                || "UNUSABLE".equals(evidence.getQuality().getLevel())
                || evidence.getSource() == null) {
            return false;
        }
        String sourceType = evidence.getSource().getSourceType();
        return sourceType != null && Set.of(
                "PLATFORM_RECORD",
                "TARGET_SNAPSHOT",
                "VERIFIED_MEDIA_OBSERVATION",
                "AUTHORITATIVE_EXTERNAL_REFERENCE").contains(sourceType);
    }

    private static boolean isCompleteOutcome(String outcome) {
        return outcome != null && COMPLETE_OUTCOMES.contains(outcome);
    }

    private static boolean isRuntimePolicyActive(AdminReportAiPolicyContextRequestDTO policyContext) {
        return policyContext != null
                && "ACTIVE".equals(policyContext.getPolicyStatus())
                && "ACTIVE".equals(policyContext.getRuleCatalogStatus())
                && "ACTIVE_RUNTIME".equals(policyContext.getEvaluationMode())
                && policyContext.getCandidateRules() != null
                && !policyContext.getCandidateRules().isEmpty()
                && policyContext.getCandidateRules().stream()
                .allMatch(rule -> rule != null && "ACTIVE".equals(rule.getRuleStatus()));
    }

    private static boolean validCodeList(List<String> values, int maximum, boolean allowEmpty) {
        if (values == null || values.size() > maximum || (!allowEmpty && values.isEmpty())) {
            return false;
        }
        return values.stream().allMatch(AdminReportAiSemanticValidator::validCode)
                && new HashSet<>(values).size() == values.size();
    }

    private static boolean validCode(String value) {
        return value != null
                && value.length() <= MAX_CODE_LENGTH
                && CODE_PATTERN.matcher(value).matches();
    }

    private static boolean validText(String value, int minimum, int maximum) {
        return value != null && value.length() >= minimum && value.length() <= maximum;
    }

    private static String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return "Manual review is required.";
        }
        return value.length() <= MAX_TEXT_LENGTH ? value : value.substring(0, MAX_TEXT_LENGTH);
    }

    private static List<String> normalizeCodes(List<String> values, int maximum) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(AdminReportAiSemanticValidator::validCode)
                .distinct()
                .limit(maximum)
                .toList();
    }

    private static String stringValue(Object value) {
        return value instanceof String text ? text : null;
    }

    private record EvidenceIndex(
            Map<String, AdminReportAiEvidenceItemRequestDTO> byId,
            boolean envelopeValid,
            boolean duplicateId,
            boolean snapshotBindingMismatch) {
    }

    private record FindingAnalysis(
            boolean boundsValid,
            boolean referencesValid,
            boolean ruleVersionsMatch,
            boolean outcomesAllowed,
            boolean independentEvidenceBasis,
            boolean counterEvidenceResolved,
            Map<String, String> outcomesByRule) {

        private static FindingAnalysis invalid() {
            return new FindingAnalysis(false, false, false, false, false, false, Map.of());
        }
    }

    public record ValidationResult(AdminReportAiResolutionWebhookResponseDTO response, boolean clampedToManual) {
    }
}
