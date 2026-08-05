package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiEvidenceItemRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiMissingRequirementRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiPolicyContextRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiResolutionRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionWebhookResponseDTO;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.ReportTargetType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class AdminReportAiSemanticValidatorTest {

    @Test
    void validate_validRecommendationPreservesDecisionAndFindings_TC001() {
        AdminReportAiResolutionRequestDTO request = request();
        AdminReportAiResolutionWebhookResponseDTO response = response(request);

        AdminReportAiSemanticValidator.ValidationResult result =
                AdminReportAiSemanticValidator.validate(request, response);

        assertThat(result.clampedToManual()).isFalse();
        assertThat(result.response().getReportDecision()).isEqualTo(AdminReportAiReportDecision.RESOLVE);
        assertThat(result.response().getTargetAction()).isEqualTo(AdminReportAiTargetAction.HIDE);
        assertThat(result.response().getFindings()).hasSize(1);
        assertThat(result.response().getBlockedReasons()).isEmpty();
    }

    @Test
    void validate_contractCorrelationAndVersionVariantsClampToManual_TC002() {
        AdminReportAiResolutionRequestDTO request = request();
        List<AdminReportAiResolutionWebhookResponseDTO> invalidResponses = new ArrayList<>();

        AdminReportAiResolutionWebhookResponseDTO badContract = response(request);
        badContract.setContractVersion("1.0");
        invalidResponses.add(badContract);

        AdminReportAiResolutionWebhookResponseDTO missingCorrelation = response(request);
        missingCorrelation.setCorrelationId(null);
        invalidResponses.add(missingCorrelation);

        AdminReportAiResolutionWebhookResponseDTO wrongCorrelation = response(request);
        wrongCorrelation.setCorrelationId(UUID.randomUUID());
        invalidResponses.add(wrongCorrelation);

        AdminReportAiResolutionWebhookResponseDTO badPolicy = response(request);
        badPolicy.setPolicyVersion("wrong");
        invalidResponses.add(badPolicy);

        AdminReportAiResolutionWebhookResponseDTO badRuleCatalog = response(request);
        badRuleCatalog.setRuleCatalogVersion("wrong");
        invalidResponses.add(badRuleCatalog);

        AdminReportAiResolutionWebhookResponseDTO badPrompt = response(request);
        badPrompt.setPromptVersion("wrong");
        invalidResponses.add(badPrompt);

        AdminReportAiResolutionWebhookResponseDTO badWorkflow = response(request);
        badWorkflow.setWorkflowVersion("wrong");
        invalidResponses.add(badWorkflow);

        for (AdminReportAiResolutionWebhookResponseDTO invalid : invalidResponses) {
            AdminReportAiSemanticValidator.ValidationResult result =
                    AdminReportAiSemanticValidator.validate(request, invalid);
            assertThat(result.clampedToManual()).isTrue();
            assertThat(result.response().getTargetAction()).isEqualTo(AdminReportAiTargetAction.NO_ACTION);
        }

        assertThat(invalidResponses.get(0).getBlockedReasons()).contains("CONTRACT_OR_CORRELATION_MISMATCH");
        assertThat(invalidResponses.get(3).getBlockedReasons()).contains("VERSION_PIN_MISMATCH");
    }

    @Test
    void validate_eachInvalidCategoricalValueClampsToManual_TC003() {
        AdminReportAiResolutionRequestDTO request = request();

        AdminReportAiResolutionWebhookResponseDTO badQuality = response(request);
        badQuality.setEvidenceQuality("UNKNOWN_VALUE");
        AdminReportAiResolutionWebhookResponseDTO badSufficiency = response(request);
        badSufficiency.setEvidenceSufficiency("UNKNOWN_VALUE");
        AdminReportAiResolutionWebhookResponseDTO badLikelihood = response(request);
        badLikelihood.setViolationLikelihood("UNKNOWN_VALUE");
        AdminReportAiResolutionWebhookResponseDTO badHarm = response(request);
        badHarm.setHarmSeverity("UNKNOWN_VALUE");

        for (AdminReportAiResolutionWebhookResponseDTO invalid :
                List.of(badQuality, badSufficiency, badLikelihood, badHarm)) {
            AdminReportAiSemanticValidator.ValidationResult result =
                    AdminReportAiSemanticValidator.validate(request, invalid);
            assertThat(result.clampedToManual()).isTrue();
            assertThat(result.response().getBlockedReasons()).contains("INVALID_CATEGORICAL_SEMANTICS");
        }
        assertThat(badQuality.getEvidenceQuality()).isEqualTo("UNUSABLE");
        assertThat(badSufficiency.getEvidenceSufficiency()).isEqualTo("UNASSESSABLE");
        assertThat(badLikelihood.getViolationLikelihood()).isEqualTo("UNKNOWN");
        assertThat(badHarm.getHarmSeverity()).isEqualTo("UNKNOWN");
    }

    @Test
    void validate_manualAndInsufficientEvidenceRemainNoAction_TC004() {
        AdminReportAiResolutionRequestDTO request = request();
        request.setExecutionConstraints(null);

        AdminReportAiResolutionWebhookResponseDTO manual = response(request);
        manual.setReportDecision(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        manual.setTargetAction(AdminReportAiTargetAction.NONE);
        AdminReportAiSemanticValidator.ValidationResult manualResult =
                AdminReportAiSemanticValidator.validate(request, manual);
        assertThat(manualResult.clampedToManual()).isTrue();
        assertThat(manual.getEvidenceSufficiency()).isEqualTo("UNASSESSABLE");
        assertThat(manual.getFindings()).isEmpty();

        AdminReportAiResolutionWebhookResponseDTO insufficient = response(request);
        insufficient.setEvidenceSufficiency("INSUFFICIENT");
        AdminReportAiSemanticValidator.validate(request, insufficient);
        assertThat(insufficient.getEvidenceSufficiency()).isEqualTo("INSUFFICIENT");
        assertThat(insufficient.getBlockedReasons()).contains("EVIDENCE_NOT_SUFFICIENT");
    }

    @Test
    void validate_criticalEvidenceAndExistingBlockedReasonsAreDistinct_TC005() {
        AdminReportAiResolutionRequestDTO request = request();
        request.setExecutionConstraints(Map.of("criticalEvidenceMissing", true));
        AdminReportAiResolutionWebhookResponseDTO response = response(request);
        response.setBlockedReasons(List.of("CRITICAL_EVIDENCE_MISSING"));
        response.setEvidenceSufficiency("INSUFFICIENT");

        AdminReportAiSemanticValidator.validate(request, response);

        assertThat(response.getBlockedReasons())
                .containsExactlyInAnyOrder("CRITICAL_EVIDENCE_MISSING", "EVIDENCE_NOT_SUFFICIENT");
        assertThat(response.getReportDecision()).isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(response.getTargetAction()).isEqualTo(AdminReportAiTargetAction.NO_ACTION);
        assertThat(response.getEvidenceSufficiency()).isEqualTo("UNASSESSABLE");
    }

    @Test
    void validate_invalidFindingShapesAndReferencesClampToManual_TC006() {
        AdminReportAiResolutionRequestDTO request = request();

        AdminReportAiResolutionWebhookResponseDTO nullFindings = response(request);
        nullFindings.setFindings(null);

        AdminReportAiResolutionWebhookResponseDTO emptyFindings = response(request);
        emptyFindings.setFindings(List.of());

        AdminReportAiResolutionWebhookResponseDTO unknownRule = response(request);
        unknownRule.getFindings().getFirst().put("ruleId", "UNKNOWN.RULE");

        AdminReportAiResolutionWebhookResponseDTO scalarEvidence = response(request);
        scalarEvidence.getFindings().getFirst().put("evidenceIds", "EV-TARGET-CONTENT");

        AdminReportAiResolutionWebhookResponseDTO unknownEvidence = response(request);
        unknownEvidence.getFindings().getFirst().put("evidenceIds", List.of("EV-UNKNOWN"));

        AdminReportAiResolutionWebhookResponseDTO scalarCounterEvidence = response(request);
        scalarCounterEvidence.getFindings().getFirst().put("counterEvidenceIds", "EV-TARGET-CONTENT");

        AdminReportAiResolutionWebhookResponseDTO unknownCounterEvidence = response(request);
        unknownCounterEvidence.getFindings().getFirst().put("counterEvidenceIds", List.of("EV-UNKNOWN"));

        for (AdminReportAiResolutionWebhookResponseDTO invalid : List.of(
                nullFindings,
                emptyFindings,
                unknownRule,
                scalarEvidence,
                unknownEvidence,
                scalarCounterEvidence,
                unknownCounterEvidence)) {
            AdminReportAiSemanticValidator.validate(request, invalid);
            assertThat(invalid.getBlockedReasons()).isNotEmpty();
            assertThat(invalid.getFindings()).isEmpty();
        }
        assertThat(unknownRule.getBlockedReasons()).contains("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
        assertThat(unknownEvidence.getBlockedReasons()).contains("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
        assertThat(unknownCounterEvidence.getBlockedReasons())
                .contains("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
        assertThat(scalarEvidence.getBlockedReasons()).contains("PROVIDER_OUTPUT_BOUND_EXCEEDED");
    }

    @Test
    void validate_nullEvidenceAndNullFindingReferencesFailClosed_TC007() {
        AdminReportAiResolutionRequestDTO request = request();
        request.setEvidence(null);
        AdminReportAiResolutionWebhookResponseDTO response = response(request);
        response.getFindings().getFirst().put("outcome", "UNASSESSABLE");
        response.getFindings().getFirst().put("evidenceIds", null);
        response.getFindings().getFirst().put("counterEvidenceIds", null);

        AdminReportAiSemanticValidator.ValidationResult result =
                AdminReportAiSemanticValidator.validate(request, response);

        assertThat(result.clampedToManual()).isTrue();
        assertThat(response.getBlockedReasons())
                .contains("EVIDENCE_ENVELOPE_INVALID", "PROVIDER_OUTPUT_BOUND_EXCEEDED");
    }

    @Test
    void validate_allEvidenceReferenceFieldsUseBackendAllowlist_TC008() {
        AdminReportAiResolutionRequestDTO request = request();

        AdminReportAiResolutionWebhookResponseDTO unknownMissing = response(request);
        unknownMissing.getFindings().getFirst().put("missingEvidenceIds", List.of("EV-UNKNOWN"));

        AdminReportAiResolutionWebhookResponseDTO unknownSummaryUsed = response(request);
        unknownSummaryUsed.setEvidenceSummary(Map.of(
                "usedEvidenceIds", List.of("EV-UNKNOWN"),
                "counterEvidenceIds", List.of(),
                "missingEvidenceIds", List.of()));

        AdminReportAiResolutionWebhookResponseDTO unknownSummaryCounter = response(request);
        unknownSummaryCounter.setEvidenceSummary(Map.of(
                "usedEvidenceIds", List.of(),
                "counterEvidenceIds", List.of("EV-UNKNOWN"),
                "missingEvidenceIds", List.of()));

        AdminReportAiResolutionWebhookResponseDTO unknownSummaryMissing = response(request);
        unknownSummaryMissing.setEvidenceSummary(Map.of(
                "usedEvidenceIds", List.of(),
                "counterEvidenceIds", List.of(),
                "missingEvidenceIds", List.of("EV-UNKNOWN")));

        AdminReportAiResolutionWebhookResponseDTO scalarSummaryReference = response(request);
        scalarSummaryReference.setEvidenceSummary(Map.of(
                "usedEvidenceIds", "EV-TARGET-CONTENT",
                "counterEvidenceIds", List.of(),
                "missingEvidenceIds", List.of()));

        for (AdminReportAiResolutionWebhookResponseDTO invalid : List.of(
                unknownMissing,
                unknownSummaryUsed,
                unknownSummaryCounter,
                unknownSummaryMissing,
                scalarSummaryReference)) {
            AdminReportAiSemanticValidator.validate(request, invalid);
            assertThat(invalid.getBlockedReasons()).isNotEmpty();
            assertThat(invalid.getFindings()).isEmpty();
        }
        assertThat(unknownMissing.getBlockedReasons()).contains("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
        assertThat(unknownSummaryUsed.getBlockedReasons()).contains("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
        assertThat(unknownSummaryCounter.getBlockedReasons())
                .contains("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
        assertThat(unknownSummaryMissing.getBlockedReasons())
                .contains("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
        assertThat(scalarSummaryReference.getBlockedReasons())
                .contains("PROVIDER_OUTPUT_BOUND_EXCEEDED");
    }

    @Test
    void validate_supportedFindingRequiresIndependentUsableEvidence_TC009() {
        AdminReportAiResolutionRequestDTO request = request();
        request.setEvidence(List.of(
                evidence("EV-REASON-ROUTE", "REPORTER_CLAIM", "LOW", "AVAILABLE"),
                evidence("EV-DERIVED-MODERATION", "DERIVED_SIGNAL", "LOW", "AVAILABLE"),
                evidence("EV-UNAVAILABLE", "PLATFORM_RECORD", "HIGH", "MISSING"),
                evidence("EV-UNUSABLE", "PLATFORM_RECORD", "UNUSABLE", "AVAILABLE"),
                evidence("EV-NO-SOURCE", null, "HIGH", "AVAILABLE")));

        for (String evidenceId : List.of(
                "EV-REASON-ROUTE",
                "EV-DERIVED-MODERATION",
                "EV-UNAVAILABLE",
                "EV-UNUSABLE",
                "EV-NO-SOURCE")) {
            AdminReportAiResolutionWebhookResponseDTO invalid = response(request);
            invalid.getFindings().getFirst().put("evidenceIds", List.of(evidenceId));

            AdminReportAiSemanticValidator.validate(request, invalid);

            assertThat(invalid.getBlockedReasons()).contains("UNSUPPORTED_EVIDENCE_BASIS");
            assertThat(invalid.getReportDecision())
                    .isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
            assertThat(invalid.getTargetAction()).isEqualTo(AdminReportAiTargetAction.NO_ACTION);
        }
    }

    @Test
    void validate_nonSupportedFindingDoesNotRequirePositiveEvidence_TC010() {
        AdminReportAiResolutionRequestDTO request = request();
        AdminReportAiResolutionWebhookResponseDTO response = response(request);
        response.setRecommendationState("REJECT");
        response.setReportDecision(AdminReportAiReportDecision.REJECT);
        response.setTargetAction(AdminReportAiTargetAction.KEEP_VISIBLE);
        response.getFindings().getFirst().put("outcome", "NOT_SUBSTANTIATED");
        response.getFindings().getFirst().put("evidenceIds", List.of());
        response.setEvidenceSummary(Map.of(
                "usedEvidenceIds", List.of(),
                "counterEvidenceIds", List.of(),
                "missingEvidenceIds", List.of()));

        AdminReportAiSemanticValidator.ValidationResult result =
                AdminReportAiSemanticValidator.validate(request, response);

        assertThat(result.clampedToManual()).isFalse();
    }

    @Test
    void validate_ruleVersionMismatchFailsClosed_SEM_S2_001() {
        AdminReportAiResolutionRequestDTO request = request();
        AdminReportAiResolutionWebhookResponseDTO response = response(request);
        response.getFindings().getFirst().put("ruleVersion", "1.0.0-invented");

        AdminReportAiSemanticValidator.validate(request, response);

        assertThat(response.getBlockedReasons()).contains("RULE_VERSION_MISMATCH");
        assertThat(response.getFindings()).isEmpty();
    }

    @Test
    void validate_materialCandidateOmittedFailsClosed_SEM_S2_002() {
        AdminReportAiResolutionRequestDTO request = request();
        AdminReportAiResolutionWebhookResponseDTO response = response(request);
        response.setFindings(List.of());

        AdminReportAiSemanticValidator.validate(request, response);

        assertThat(response.getBlockedReasons())
                .contains("INCOMPLETE_RULE_EVALUATION_SCOPE", "ACTION_BURDEN_NOT_SATISFIED");
    }

    @Test
    void validate_missingEvidenceAndSemanticRequirementsFailClosed_SEM_S2_003() {
        AdminReportAiResolutionRequestDTO request = request();
        request.setEvidence(request.getEvidence().stream()
                .filter(item -> !"TARGET_IDENTITY".equals(item.getEvidenceKind()))
                .toList());
        request.getPolicyContext().setMissingRequirements(List.of(
                AdminReportAiMissingRequirementRequestDTO.builder()
                        .missingRequirementId("ME-CSR-SPAM-001-TARGET-IDENTITY")
                        .ruleId("CSR.SPAM.001")
                        .requirementCode("REQ-TARGET-IDENTITY")
                        .evidenceKind("TARGET_IDENTITY")
                        .status("MISSING")
                        .reasonCode("REQUIRED_EVIDENCE_NOT_AVAILABLE")
                        .build(),
                AdminReportAiMissingRequirementRequestDTO.builder()
                        .missingRequirementId("ME-CSR-SPAM-001-SEM-COMPLETE")
                        .ruleId("CSR.SPAM.001")
                        .requirementCode("SEM-COMPLETE-EVALUATION-SCOPE")
                        .evidenceKind(null)
                        .status("MISSING")
                        .reasonCode("SEMANTIC_REQUIREMENT_NOT_PROVEN")
                        .build()));
        AdminReportAiResolutionWebhookResponseDTO response = response(request);
        response.getFindings().getFirst().put(
                "missingEvidenceIds",
                List.of("ME-CSR-SPAM-001-TARGET-IDENTITY", "ME-CSR-SPAM-001-SEM-COMPLETE"));

        AdminReportAiSemanticValidator.validate(request, response);

        assertThat(response.getBlockedReasons())
                .contains("EVIDENCE_KIND_REQUIREMENT_MISSING", "SEMANTIC_REQUIREMENT_MISSING");
        assertThat(response.getEvidenceSummary()).containsOnlyKeys(
                "usedEvidenceIds", "counterEvidenceIds", "missingEvidenceIds");
    }

    @Test
    void validate_unresolvedCounterEvidenceFailsClosed_SEM_S2_004() {
        AdminReportAiResolutionRequestDTO request = request();
        AdminReportAiResolutionWebhookResponseDTO response = response(request);
        response.getFindings().getFirst().put("counterEvidenceIds", List.of("EV-TARGET-STATE"));

        AdminReportAiSemanticValidator.validate(request, response);

        assertThat(response.getBlockedReasons()).contains("MATERIAL_COUNTER_EVIDENCE_UNRESOLVED");
    }

    @Test
    void validate_evaluationCeilingAndActionBurdenFailClosed_SEM_S2_005_006() {
        AdminReportAiResolutionRequestDTO ceilingRequest = request();
        ceilingRequest.getPolicyContext().setCurrentEvaluationCeiling("NEEDS_MANUAL_REVIEW");
        ceilingRequest.getPolicyContext().getCandidateRules().getFirst()
                .setEvaluationCeiling("NEEDS_MANUAL_REVIEW");
        AdminReportAiResolutionWebhookResponseDTO ceilingResponse = response(ceilingRequest);
        AdminReportAiSemanticValidator.validate(ceilingRequest, ceilingResponse);
        assertThat(ceilingResponse.getBlockedReasons()).contains("RULE_EVALUATION_CEILING_EXCEEDED");

        AdminReportAiResolutionRequestDTO actionRequest = request();
        actionRequest.getPolicyContext().getCandidateRules().getFirst()
                .setAllowedCandidateActions(List.of("KEEP_VISIBLE", "NO_ACTION"));
        AdminReportAiResolutionWebhookResponseDTO actionResponse = response(actionRequest);
        actionResponse.setRecommendationState("REJECT");
        AdminReportAiSemanticValidator.validate(actionRequest, actionResponse);
        assertThat(actionResponse.getBlockedReasons()).contains("ACTION_BURDEN_NOT_SATISFIED");
    }

    @Test
    void validate_allMaterialNotSubstantiatedAllowsReject_SEM_S2_007() {
        AdminReportAiResolutionRequestDTO request = request();
        AdminReportAiResolutionWebhookResponseDTO response = response(request);
        response.setRecommendationState("REJECT");
        response.setReportDecision(AdminReportAiReportDecision.REJECT);
        response.setTargetAction(AdminReportAiTargetAction.KEEP_VISIBLE);
        response.getFindings().getFirst().put("outcome", "NOT_APPLICABLE");
        response.getFindings().getFirst().put("evidenceIds", List.of());
        response.setEvidenceSummary(Map.of(
                "usedEvidenceIds", List.of(),
                "counterEvidenceIds", List.of(),
                "missingEvidenceIds", List.of()));

        AdminReportAiSemanticValidator.ValidationResult result =
                AdminReportAiSemanticValidator.validate(request, response);

        assertThat(result.clampedToManual()).isFalse();
        assertThat(response.getReportDecision()).isEqualTo(AdminReportAiReportDecision.REJECT);
    }

    @Test
    void validate_substantiatedPlusUnassessableMaterialRuleFailsClosed_SEM_S2_008() {
        AdminReportAiResolutionRequestDTO request = request();
        var secondRule = AdminReportAiPolicyCatalog.candidateRules("SCAM_OR_FRAUD").getFirst();
        secondRule.setRuleStatus("ACTIVE");
        secondRule.setEvaluationCeiling("RESOLVE_OR_REJECT");
        request.getPolicyContext().setCandidateRules(List.of(
                request.getPolicyContext().getCandidateRules().getFirst(),
                secondRule));
        request.getEvidence().forEach(item ->
                item.setCollectedForRuleIds(List.of("CSR.SPAM.001", secondRule.getRuleId())));
        AdminReportAiResolutionWebhookResponseDTO response = response(request);
        Map<String, Object> secondFinding = new LinkedHashMap<>(response.getFindings().getFirst());
        secondFinding.put("ruleId", secondRule.getRuleId());
        secondFinding.put("ruleVersion", secondRule.getRuleVersion());
        secondFinding.put("outcome", "UNASSESSABLE");
        secondFinding.put("evidenceIds", List.of());
        response.setFindings(List.of(response.getFindings().getFirst(), secondFinding));

        AdminReportAiSemanticValidator.validate(request, response);

        assertThat(response.getBlockedReasons()).contains("INCOMPLETE_RULE_EVALUATION_SCOPE");
        assertThat(response.getReportDecision())
                .isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
    }

    @Test
    void validate_boundsUnknownFieldsAndNormalizationFailClosed_SCHEMA_S2_001() {
        AdminReportAiResolutionRequestDTO request = request();
        AdminReportAiResolutionWebhookResponseDTO response = response(request);
        response.setBlockedReasons(null);
        response.setLabels(List.of("VALID", "VALID", "bad label"));
        response.setExplanation("x".repeat(4001));
        response.getFindings().getFirst().put("unknownAuthority", true);

        AdminReportAiSemanticValidator.validate(request, response);

        assertThat(response.getBlockedReasons()).contains("PROVIDER_OUTPUT_BOUND_EXCEEDED");
        assertThat(response.getLabels()).containsExactly("VALID");
        assertThat(response.getExplanation()).hasSize(4000);
        assertThat(response.getFindings()).isEmpty();

        AdminReportAiResolutionWebhookResponseDTO nullMetadata = response(request);
        nullMetadata.setLabels(null);
        nullMetadata.setExplanation(null);
        nullMetadata.setEvidenceSummary(null);
        nullMetadata.setFindings(java.util.Collections.singletonList(null));
        AdminReportAiSemanticValidator.validate(request, nullMetadata);
        assertThat(nullMetadata.getExplanation()).isEqualTo("Manual review is required.");
        assertThat(nullMetadata.getLabels()).isEmpty();
    }

    @Test
    void validate_malformedEnvelopeReferencesAndBurdenVariantsFailClosed_SCHEMA_S2_002() {
        AdminReportAiResolutionRequestDTO nullEvidenceItemRequest = request();
        nullEvidenceItemRequest.setEvidence(java.util.Collections.singletonList(null));
        AdminReportAiResolutionWebhookResponseDTO nullEvidenceItemResponse =
                response(nullEvidenceItemRequest);
        AdminReportAiSemanticValidator.validate(nullEvidenceItemRequest, nullEvidenceItemResponse);
        assertThat(nullEvidenceItemResponse.getBlockedReasons()).contains("EVIDENCE_ENVELOPE_INVALID");

        AdminReportAiResolutionRequestDTO missingPayloadRequest = request();
        missingPayloadRequest.getEvidence().getFirst().setPayload(null);
        AdminReportAiResolutionWebhookResponseDTO missingPayloadResponse = response(missingPayloadRequest);
        AdminReportAiSemanticValidator.validate(missingPayloadRequest, missingPayloadResponse);
        assertThat(missingPayloadResponse.getBlockedReasons()).contains("EVIDENCE_ENVELOPE_INVALID");

        AdminReportAiResolutionRequestDTO duplicateFindingRequest = request();
        AdminReportAiResolutionWebhookResponseDTO duplicateFindingResponse =
                response(duplicateFindingRequest);
        Map<String, Object> duplicate =
                new LinkedHashMap<>(duplicateFindingResponse.getFindings().getFirst());
        duplicateFindingResponse.setFindings(List.of(
                duplicateFindingResponse.getFindings().getFirst(),
                duplicate));
        AdminReportAiSemanticValidator.validate(duplicateFindingRequest, duplicateFindingResponse);
        assertThat(duplicateFindingResponse.getBlockedReasons())
                .contains("PROVIDER_OUTPUT_BOUND_EXCEEDED");

        AdminReportAiResolutionRequestDTO ruleCeilingRequest = request();
        ruleCeilingRequest.getPolicyContext().getCandidateRules().getFirst()
                .setEvaluationCeiling("NEEDS_MANUAL_REVIEW");
        AdminReportAiResolutionWebhookResponseDTO ruleCeilingResponse = response(ruleCeilingRequest);
        AdminReportAiSemanticValidator.validate(ruleCeilingRequest, ruleCeilingResponse);
        assertThat(ruleCeilingResponse.getBlockedReasons()).contains("RULE_EVALUATION_CEILING_EXCEEDED");

        AdminReportAiResolutionRequestDTO declaredMissingRequest = request();
        declaredMissingRequest.getPolicyContext().setMissingRequirements(List.of(
                AdminReportAiMissingRequirementRequestDTO.builder()
                        .missingRequirementId("ME-DECLARED-MISSING")
                        .ruleId("CSR.SPAM.001")
                        .requirementCode("REQ-TARGET-TEXT-CONTENT")
                        .evidenceKind("TARGET_TEXT_CONTENT")
                        .status("MISSING")
                        .reasonCode("DECLARED_MISSING")
                        .build()));
        AdminReportAiResolutionWebhookResponseDTO declaredMissingResponse =
                response(declaredMissingRequest);
        AdminReportAiSemanticValidator.validate(declaredMissingRequest, declaredMissingResponse);
        assertThat(declaredMissingResponse.getBlockedReasons())
                .contains("EVIDENCE_KIND_REQUIREMENT_MISSING");

        AdminReportAiResolutionRequestDTO duplicateReferenceRequest = request();
        AdminReportAiResolutionWebhookResponseDTO duplicateReferenceResponse =
                response(duplicateReferenceRequest);
        duplicateReferenceResponse.getFindings().getFirst().put(
                "evidenceIds",
                List.of("EV-TARGET-CONTENT", "EV-TARGET-CONTENT"));
        AdminReportAiSemanticValidator.validate(duplicateReferenceRequest, duplicateReferenceResponse);
        assertThat(duplicateReferenceResponse.getBlockedReasons())
                .contains("PROVIDER_OUTPUT_BOUND_EXCEEDED");

        AdminReportAiResolutionRequestDTO scalarIdentityRequest = request();
        AdminReportAiResolutionWebhookResponseDTO scalarIdentityResponse = response(scalarIdentityRequest);
        scalarIdentityResponse.getFindings().getFirst().put("ruleId", 42);
        AdminReportAiSemanticValidator.validate(scalarIdentityRequest, scalarIdentityResponse);
        assertThat(scalarIdentityResponse.getBlockedReasons()).contains("PROVIDER_OUTPUT_BOUND_EXCEEDED");

        AdminReportAiResolutionRequestDTO emptyContextRequest = request();
        emptyContextRequest.getPolicyContext().setCandidateRules(List.of());
        AdminReportAiResolutionWebhookResponseDTO emptyContextResponse = response(emptyContextRequest);
        emptyContextResponse.setTargetAction(null);
        AdminReportAiSemanticValidator.validate(emptyContextRequest, emptyContextResponse);
        assertThat(emptyContextResponse.getBlockedReasons())
                .contains("RULE_CONTEXT_SCHEMA_MISMATCH", "DECISION_ACTION_NOT_ALLOWED");

        AdminReportAiResolutionRequestDTO manualRequest = request();
        AdminReportAiResolutionWebhookResponseDTO manualResponse = response(manualRequest);
        manualResponse.setRecommendationState("NEEDS_MANUAL_REVIEW");
        manualResponse.setReportDecision(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        manualResponse.setTargetAction(AdminReportAiTargetAction.NO_ACTION);
        AdminReportAiSemanticValidator.validate(manualRequest, manualResponse);
        assertThat(manualResponse.getReportDecision())
                .isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);

        AdminReportAiResolutionRequestDTO nonMaterialRequest = request();
        nonMaterialRequest.getPolicyContext().getCandidateRules().getFirst().setMaterial(false);
        AdminReportAiResolutionWebhookResponseDTO nonMaterialResponse = response(nonMaterialRequest);
        AdminReportAiSemanticValidator.validate(nonMaterialRequest, nonMaterialResponse);
        assertThat(nonMaterialResponse.getBlockedReasons())
                .contains("INCOMPLETE_RULE_EVALUATION_SCOPE", "ACTION_BURDEN_NOT_SATISFIED");
    }

    @Test
    void validate_eachRequiredRuleContextFieldIsFailClosed_SCHEMA_S2_003() {
        List<Consumer<AdminReportAiPolicyContextRequestDTO>> invalidContexts = List.of(
                context -> context.setContextSchemaVersion(null),
                context -> context.setPolicyVersion("POLICY-OTHER"),
                context -> context.setRuleCatalogVersion("CATALOG-OTHER"),
                context -> context.setRequirementMatrixVersion("MATRIX-OTHER"),
                context -> context.setEvidenceKindCatalogVersion("EVIDENCE-OTHER"),
                context -> context.setCandidateRules(null),
                context -> context.setCandidateRules(java.util.Collections.singletonList(null)),
                context -> context.getCandidateRules().getFirst().setRuleId(null),
                context -> context.getCandidateRules().getFirst().setRuleVersion(null),
                context -> context.getCandidateRules().getFirst().setRuleStatus(null),
                context -> context.getCandidateRules().getFirst().setRuleFamily(null),
                context -> context.getCandidateRules().getFirst().setRuleType("UNKNOWN"),
                context -> context.getCandidateRules().getFirst().setMaterial(null),
                context -> context.getCandidateRules().getFirst().setApplicableTargetTypes(null),
                context -> context.getCandidateRules().getFirst()
                        .setApplicableTargetTypes(List.of(ReportTargetType.COMMENT)),
                context -> context.getCandidateRules().getFirst().setRequirementProfileIds(null),
                context -> context.getCandidateRules().getFirst().setRequiredEvidenceKinds(null),
                context -> context.getCandidateRules().getFirst().setSemanticRequirementCodes(null),
                context -> context.getCandidateRules().getFirst().setConditionalRequirements(null),
                context -> context.getCandidateRules().getFirst().setCounterEvidenceRequired(null),
                context -> context.getCandidateRules().getFirst().setExceptionCodes(null),
                context -> context.getCandidateRules().getFirst().setEvaluationCeiling(null),
                context -> context.getCandidateRules().getFirst().setAllowedOutcomes(null),
                context -> context.getCandidateRules().getFirst().setAllowedCandidateActions(null));

        for (Consumer<AdminReportAiPolicyContextRequestDTO> mutation : invalidContexts) {
            AdminReportAiResolutionRequestDTO request = request();
            mutation.accept(request.getPolicyContext());
            AdminReportAiResolutionWebhookResponseDTO response = response(request);

            AdminReportAiSemanticValidator.validate(request, response);

            assertThat(response.getBlockedReasons()).contains("RULE_CONTEXT_SCHEMA_MISMATCH");
            assertThat(response.getReportDecision())
                    .isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
            assertThat(response.getTargetAction()).isEqualTo(AdminReportAiTargetAction.NO_ACTION);
        }
    }

    @Test
    void isDecisionActionAllowed_coversManualTargetAndDecisionMatrix_TC011() {
        assertThat(AdminReportAiSemanticValidator.isDecisionActionAllowed(
                ReportTargetType.BLOG,
                AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW,
                AdminReportAiTargetAction.NO_ACTION)).isTrue();
        assertThat(AdminReportAiSemanticValidator.isDecisionActionAllowed(
                ReportTargetType.BLOG,
                AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW,
                AdminReportAiTargetAction.NONE)).isTrue();
        assertThat(AdminReportAiSemanticValidator.isDecisionActionAllowed(
                ReportTargetType.BLOG,
                AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW,
                AdminReportAiTargetAction.HIDE)).isFalse();

        assertThat(AdminReportAiSemanticValidator.isDecisionActionAllowed(
                ReportTargetType.USER,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE)).isFalse();
        assertThat(AdminReportAiSemanticValidator.isDecisionActionAllowed(
                ReportTargetType.CAFE_PAGE,
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.KEEP_VISIBLE)).isFalse();

        assertThat(AdminReportAiSemanticValidator.isDecisionActionAllowed(
                ReportTargetType.BLOG,
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.KEEP_VISIBLE)).isTrue();
        assertThat(AdminReportAiSemanticValidator.isDecisionActionAllowed(
                ReportTargetType.BLOG,
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.APPROVE)).isTrue();
        assertThat(AdminReportAiSemanticValidator.isDecisionActionAllowed(
                ReportTargetType.BLOG,
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.HIDE)).isFalse();

        assertThat(AdminReportAiSemanticValidator.isDecisionActionAllowed(
                ReportTargetType.COMMENT,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE)).isTrue();
        assertThat(AdminReportAiSemanticValidator.isDecisionActionAllowed(
                ReportTargetType.COMMENT,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.REMOVE)).isTrue();
        assertThat(AdminReportAiSemanticValidator.isDecisionActionAllowed(
                ReportTargetType.COMMENT,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.KEEP_VISIBLE)).isFalse();
    }

    @Test
    void policyCatalog_normalizesKnownUnknownAndNullReasons_TC012() {
        assertThat(AdminReportAiPolicyCatalog.candidateRules(" scam_fraud_or_spam "))
                .extracting(rule -> rule.getRuleId())
                .containsExactly("CSR.INT.003", "CSR.SPAM.001");
        assertThat(AdminReportAiPolicyCatalog.candidateRules("not-catalogued"))
                .extracting(rule -> rule.getRuleId())
                .containsExactly("CSR.ROUTE.002");
        assertThat(AdminReportAiPolicyCatalog.candidateRules(null))
                .extracting(rule -> rule.getRuleId())
                .containsExactly("CSR.ROUTE.002");
        assertThat(AdminReportAiPolicyCatalog.containsRule("SPAM", "CSR.SPAM.001")).isTrue();
        assertThat(AdminReportAiPolicyCatalog.containsRule("SPAM", "CSR.INT.003")).isFalse();

        var firstRead = AdminReportAiPolicyCatalog.candidateRules("SPAM").getFirst();
        assertThat(firstRead.getConditionalRequirements()).isEmpty();
        assertThat(firstRead.getRequirementProfileIds()).containsExactly("RP-TEXT-CONTEXT");
    }

    @Test
    void validate_proposedLifecycleDuplicateIdsAndSnapshotMismatchFailClosed_S201_TC013() {
        AdminReportAiResolutionRequestDTO proposed = request();
        proposed.getPolicyContext().setPolicyStatus("PROPOSED");
        proposed.getPolicyContext().setRuleCatalogStatus("PROPOSED");
        proposed.getPolicyContext().setEvaluationMode("PROPOSED_EVALUATION_ONLY");
        AdminReportAiResolutionWebhookResponseDTO proposedResponse = response(proposed);

        AdminReportAiSemanticValidator.validate(proposed, proposedResponse);

        assertThat(proposedResponse.getBlockedReasons())
                .contains("POLICY_OR_RULE_CATALOG_NOT_ACTIVE");
        assertThat(proposedResponse.getReportDecision())
                .isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(proposedResponse.getTargetAction()).isEqualTo(AdminReportAiTargetAction.NO_ACTION);

        AdminReportAiResolutionRequestDTO unknownEvaluationMode = request();
        unknownEvaluationMode.getPolicyContext().setEvaluationMode(null);
        AdminReportAiResolutionWebhookResponseDTO unknownEvaluationModeResponse = response(unknownEvaluationMode);
        AdminReportAiSemanticValidator.validate(unknownEvaluationMode, unknownEvaluationModeResponse);
        assertThat(unknownEvaluationModeResponse.getBlockedReasons())
                .contains("POLICY_OR_RULE_CATALOG_NOT_ACTIVE");

        AdminReportAiResolutionRequestDTO proposedRule = request();
        proposedRule.getPolicyContext().getCandidateRules().getFirst().setRuleStatus("PROPOSED");
        AdminReportAiResolutionWebhookResponseDTO proposedRuleResponse = response(proposedRule);
        AdminReportAiSemanticValidator.validate(proposedRule, proposedRuleResponse);
        assertThat(proposedRuleResponse.getBlockedReasons())
                .contains("POLICY_OR_RULE_CATALOG_NOT_ACTIVE");

        AdminReportAiResolutionRequestDTO duplicateRules = request();
        duplicateRules.getPolicyContext().setCandidateRules(List.of(
                duplicateRules.getPolicyContext().getCandidateRules().getFirst(),
                duplicateRules.getPolicyContext().getCandidateRules().getFirst()));
        AdminReportAiResolutionWebhookResponseDTO duplicateRuleResponse = response(duplicateRules);
        AdminReportAiSemanticValidator.validate(duplicateRules, duplicateRuleResponse);
        assertThat(duplicateRuleResponse.getBlockedReasons()).contains("DUPLICATE_CANDIDATE_RULE_ID");

        AdminReportAiResolutionRequestDTO duplicateEvidence = request();
        AdminReportAiEvidenceItemRequestDTO repeated = duplicateEvidence.getEvidence().getFirst();
        duplicateEvidence.setEvidence(List.of(repeated, repeated));
        AdminReportAiResolutionWebhookResponseDTO duplicateEvidenceResponse = response(duplicateEvidence);
        AdminReportAiSemanticValidator.validate(duplicateEvidence, duplicateEvidenceResponse);
        assertThat(duplicateEvidenceResponse.getBlockedReasons()).contains("DUPLICATE_EVIDENCE_ID");

        AdminReportAiResolutionRequestDTO mismatchedSnapshot = request();
        mismatchedSnapshot.getEvidence().getFirst().getSubject().setSnapshotHash("sha256:other-snapshot");
        AdminReportAiResolutionWebhookResponseDTO mismatchedSnapshotResponse = response(mismatchedSnapshot);
        AdminReportAiSemanticValidator.validate(mismatchedSnapshot, mismatchedSnapshotResponse);
        assertThat(mismatchedSnapshotResponse.getBlockedReasons())
                .contains("EVIDENCE_SNAPSHOT_BINDING_MISMATCH");

        AdminReportAiResolutionRequestDTO missingContextAndSnapshot = request();
        missingContextAndSnapshot.setPolicyContext(null);
        missingContextAndSnapshot.setTargetSnapshot(null);
        AdminReportAiResolutionWebhookResponseDTO missingContextResponse = response(missingContextAndSnapshot);
        AdminReportAiSemanticValidator.validate(missingContextAndSnapshot, missingContextResponse);
        assertThat(missingContextResponse.getBlockedReasons())
                .contains(
                        "POLICY_OR_RULE_CATALOG_NOT_ACTIVE",
                        "DUPLICATE_CANDIDATE_RULE_ID",
                        "EVIDENCE_SNAPSHOT_BINDING_MISMATCH");
    }

    private AdminReportAiResolutionRequestDTO request() {
        AdminReportAiResolutionRequestDTO request = new AdminReportAiResolutionRequestDTO();
        request.setContractVersion("2.0");
        request.setCorrelationId(UUID.randomUUID());
        request.setReasonCode("SPAM");
        request.setTargetType(ReportTargetType.BLOG);
        request.setTargetSnapshot(Map.of(
                "snapshotHash", "sha256:test-snapshot",
                "snapshotVersion", "1"));
        request.setPolicyContext(AdminReportAiPolicyContextRequestDTO.builder()
                .contextSchemaVersion(AdminReportAiPolicyCatalog.CONTEXT_SCHEMA_VERSION)
                .policyVersion(AdminReportAiPolicyCatalog.POLICY_VERSION)
                .policyStatus("ACTIVE")
                .ruleCatalogVersion(AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION)
                .ruleCatalogStatus("ACTIVE")
                .requirementMatrixVersion(AdminReportAiPolicyCatalog.REQUIREMENT_MATRIX_VERSION)
                .evidenceKindCatalogVersion(AdminReportAiPolicyCatalog.EVIDENCE_KIND_CATALOG_VERSION)
                .evaluationMode("ACTIVE_RUNTIME")
                .candidateRules(AdminReportAiPolicyCatalog.candidateRules(request.getReasonCode()))
                .availableEvidenceKinds(List.of("TARGET_TEXT_CONTENT"))
                .missingRequirements(List.of())
                .currentEvaluationCeiling("RESOLVE_OR_REJECT")
                .build());
        request.getPolicyContext().getCandidateRules().forEach(rule -> rule.setRuleStatus("ACTIVE"));
        request.getPolicyContext().getCandidateRules().forEach(rule ->
                rule.setEvaluationCeiling("RESOLVE_OR_REJECT"));
        request.setEvidence(List.of(
                evidence(
                        "EV-TARGET-IDENTITY",
                        "TARGET_IDENTITY",
                        "TARGET_SNAPSHOT",
                        "HIGH",
                        "AVAILABLE"),
                evidence(
                        "EV-TARGET-STATE",
                        "TARGET_STATE",
                        "TARGET_SNAPSHOT",
                        "HIGH",
                        "AVAILABLE"),
                evidence(
                        "EV-TARGET-CONTENT",
                        "TARGET_TEXT_CONTENT",
                        "TARGET_SNAPSHOT",
                        "HIGH",
                        "AVAILABLE")));
        request.setExecutionConstraints(Map.of("criticalEvidenceMissing", false));
        return request;
    }

    private AdminReportAiResolutionWebhookResponseDTO response(AdminReportAiResolutionRequestDTO request) {
        AdminReportAiResolutionWebhookResponseDTO response = new AdminReportAiResolutionWebhookResponseDTO();
        response.setContractVersion("2.0");
        response.setCorrelationId(request.getCorrelationId());
        response.setRecommendationState("RESOLVE");
        response.setReportDecision(AdminReportAiReportDecision.RESOLVE);
        response.setTargetAction(AdminReportAiTargetAction.HIDE);
        response.setFindings(List.of(new LinkedHashMap<>(Map.of(
                "ruleId", "CSR.SPAM.001",
                "ruleVersion", AdminReportAiPolicyCatalog.RULE_VERSION,
                "outcome", "SUBSTANTIATED",
                "evidenceIds", List.of("EV-TARGET-CONTENT"),
                "counterEvidenceIds", List.of(),
                "missingEvidenceIds", List.of(),
                "violationLikelihood", "HIGH",
                "rationale", "Synthetic bounded rationale."))));
        response.setEvidenceSummary(Map.of(
                "usedEvidenceIds", List.of("EV-TARGET-CONTENT"),
                "counterEvidenceIds", List.of(),
                "missingEvidenceIds", List.of()));
        response.setBlockedReasons(List.of());
        response.setEvidenceQuality("HIGH");
        response.setEvidenceSufficiency("SUFFICIENT");
        response.setViolationLikelihood("HIGH");
        response.setHarmSeverity("MEDIUM");
        response.setLabels(List.of("SYNTHETIC"));
        response.setExplanation("Synthetic bounded explanation.");
        response.setPolicyVersion(AdminReportAiPolicyCatalog.POLICY_VERSION);
        response.setRuleCatalogVersion(AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION);
        response.setPromptVersion(AdminReportAiPolicyCatalog.PROMPT_VERSION);
        response.setWorkflowVersion(AdminReportAiPolicyCatalog.WORKFLOW_VERSION);
        return response;
    }

    private AdminReportAiEvidenceItemRequestDTO evidence(
            String evidenceId,
            String sourceType,
            String quality,
            String availability) {
        return evidence(evidenceId, "TARGET_TEXT_CONTENT", sourceType, quality, availability);
    }

    private AdminReportAiEvidenceItemRequestDTO evidence(
            String evidenceId,
            String evidenceKind,
            String sourceType,
            String quality,
            String availability) {
        return AdminReportAiEvidenceItemRequestDTO.builder()
                .evidenceId(evidenceId)
                .envelopeVersion(AdminReportAiPolicyCatalog.EVIDENCE_ENVELOPE_VERSION)
                .evidenceKind(evidenceKind)
                .subject(AdminReportAiEvidenceItemRequestDTO.Subject.builder()
                        .targetType(ReportTargetType.BLOG)
                        .targetAlias("target-blog-test")
                        .snapshotVersion("1")
                        .snapshotHash("sha256:test-snapshot")
                        .build())
                .source(AdminReportAiEvidenceItemRequestDTO.Source.builder()
                        .sourceType(sourceType)
                        .sourceSystem("CAFE_STORY_BACKEND")
                        .verificationStatus("SYSTEM_CAPTURED")
                        .authorityScope("PLATFORM_OWNED_FIELD")
                        .build())
                .capture(AdminReportAiEvidenceItemRequestDTO.Capture.builder()
                        .collectorName("test")
                        .collectorVersion("1")
                        .transformations(List.of())
                        .build())
                .integrity(AdminReportAiEvidenceItemRequestDTO.Integrity.builder()
                        .canonicalization("JCS")
                        .digestAlgorithm("SHA-256")
                        .payloadDigest("sha256:test-payload")
                        .build())
                .availability(AdminReportAiEvidenceItemRequestDTO.Availability.builder()
                        .status(availability)
                        .build())
                .quality(AdminReportAiEvidenceItemRequestDTO.Quality.builder()
                        .level(quality)
                        .reasonCodes(List.of("TEST"))
                        .build())
                .privacy(AdminReportAiEvidenceItemRequestDTO.Privacy.builder()
                        .classification("PUBLIC_CONTENT")
                        .containsPersonalData(false)
                        .redactionStatus("NOT_REQUIRED")
                        .retentionClass("REPORT_EVIDENCE_90D")
                        .build())
                .intendedUse("RULE_EVALUATION_CANDIDATE")
                .collectedForRuleIds(List.of("CSR.SPAM.001"))
                .payload(AdminReportAiEvidenceItemRequestDTO.Payload.builder()
                        .representation("INLINE")
                        .mediaType("application/json")
                        .value(Map.of("sanitizedText", "test"))
                        .truncated(false)
                        .build())
                .build();
    }
}
