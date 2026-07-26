package com.cafestory.service.serviceImplement;

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

        AdminReportAiSemanticValidator.validate(request, response);

        assertThat(response.getBlockedReasons()).containsExactly("CRITICAL_EVIDENCE_MISSING");
        assertThat(response.getReportDecision()).isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
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
            assertThat(invalid.getBlockedReasons()).contains("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
            assertThat(invalid.getFindings()).isEmpty();
        }
    }

    @Test
    void validate_nullEvidenceAndNullFindingReferencesAreAccepted_TC007() {
        AdminReportAiResolutionRequestDTO request = request();
        request.setEvidence(null);
        AdminReportAiResolutionWebhookResponseDTO response = response(request);
        response.getFindings().getFirst().put("evidenceIds", null);
        response.getFindings().getFirst().put("counterEvidenceIds", null);

        AdminReportAiSemanticValidator.ValidationResult result =
                AdminReportAiSemanticValidator.validate(request, response);

        assertThat(result.clampedToManual()).isFalse();
    }

    @Test
    void isDecisionActionAllowed_coversManualTargetAndDecisionMatrix_TC008() {
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
    void policyCatalog_normalizesKnownUnknownAndNullReasons_TC009() {
        assertThat(AdminReportAiPolicyCatalog.candidateRules(" scam_fraud_or_spam "))
                .extracting(rule -> rule.get("ruleId"))
                .containsExactly("CSR.INT.003", "CSR.SPAM.001");
        assertThat(AdminReportAiPolicyCatalog.candidateRules("not-catalogued"))
                .extracting(rule -> rule.get("ruleId"))
                .containsExactly("CSR.ROUTE.002");
        assertThat(AdminReportAiPolicyCatalog.candidateRules(null))
                .extracting(rule -> rule.get("ruleId"))
                .containsExactly("CSR.ROUTE.002");
        assertThat(AdminReportAiPolicyCatalog.containsRule("SPAM", "CSR.SPAM.001")).isTrue();
        assertThat(AdminReportAiPolicyCatalog.containsRule("SPAM", "CSR.INT.003")).isFalse();
    }

    private AdminReportAiResolutionRequestDTO request() {
        AdminReportAiResolutionRequestDTO request = new AdminReportAiResolutionRequestDTO();
        request.setContractVersion("2.0");
        request.setCorrelationId(UUID.randomUUID());
        request.setReasonCode("SCAM_FRAUD_OR_SPAM");
        request.setTargetType(ReportTargetType.BLOG);
        request.setEvidence(List.of(new LinkedHashMap<>(Map.of(
                "evidenceId", "EV-TARGET-CONTENT",
                "sourceType", "PLATFORM_RECORD"))));
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
                "ruleId", "CSR.INT.003",
                "evidenceIds", List.of("EV-TARGET-CONTENT"),
                "counterEvidenceIds", List.of()))));
        response.setBlockedReasons(List.of());
        response.setEvidenceQuality("HIGH");
        response.setEvidenceSufficiency("SUFFICIENT");
        response.setViolationLikelihood("HIGH");
        response.setHarmSeverity("MEDIUM");
        response.setPolicyVersion(AdminReportAiPolicyCatalog.POLICY_VERSION);
        response.setRuleCatalogVersion(AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION);
        response.setPromptVersion(AdminReportAiPolicyCatalog.PROMPT_VERSION);
        response.setWorkflowVersion(AdminReportAiPolicyCatalog.WORKFLOW_VERSION);
        return response;
    }
}
