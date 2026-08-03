package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiCandidateRuleRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiRuleRequirementRequestDTO;
import com.cafestory.entity.enums.ReportTargetType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AdminReportAiPolicyCatalog {

    public static final String CONTEXT_SCHEMA_VERSION = "RRC-1.0.0-rc.1";
    public static final String POLICY_VERSION = "PF-2.0.0-proposed.1";
    public static final String POLICY_STATUS = "PROPOSED";
    public static final String RULE_CATALOG_VERSION = "RC-2.0.0-proposed.2";
    public static final String RULE_CATALOG_STATUS = "PROPOSED";
    public static final String REASON_CATALOG_VERSION = "IRC-2.0.0-proposed.1";
    public static final String REQUIREMENT_MATRIX_VERSION = "1.0.0-rc.1";
    public static final String EVIDENCE_KIND_CATALOG_VERSION = "1.0.0-rc.1";
    public static final String EVIDENCE_ENVELOPE_VERSION = "1.0.0-rc.1";
    public static final String EVALUATION_MODE = "PROPOSED_EVALUATION_ONLY";
    public static final String PROMPT_VERSION = "report-ai-v2-sprint2.2";
    public static final String WORKFLOW_VERSION = "cafestory-admin-report-ai-resolution-v2-s2.2";
    public static final String RULE_VERSION = "1.0.0-proposed.2";
    public static final String RULE_STATUS = "PROPOSED";
    public static final String EVALUATION_CEILING = "NEEDS_MANUAL_REVIEW";

    private static final List<ReportTargetType> RUNTIME_TARGET_TYPES =
            List.of(ReportTargetType.BLOG, ReportTargetType.COMMENT);
    private static final List<String> PROPOSED_ALLOWED_OUTCOMES =
            List.of(
                    "SUBSTANTIATED",
                    "NOT_SUBSTANTIATED",
                    "NOT_APPLICABLE",
                    "UNASSESSABLE",
                    "CONFLICTED",
                    "POLICY_INVALID");
    private static final List<String> PROPOSED_VIOLATION_ACTIONS =
            List.of("KEEP_VISIBLE", "HIDE", "REMOVE", "NO_ACTION");
    private static final List<String> PROPOSED_ROUTING_ACTIONS =
            List.of("KEEP_VISIBLE", "NO_ACTION");
    private static final Map<String, List<String>> REASON_RULES = reasonRules();
    private static final Map<String, RuleDefinition> RULE_DEFINITIONS = ruleDefinitions();

    private AdminReportAiPolicyCatalog() {
    }

    public static List<AdminReportAiCandidateRuleRequestDTO> candidateRules(String reasonCode) {
        return REASON_RULES.getOrDefault(normalize(reasonCode), List.of("CSR.ROUTE.002")).stream()
                .map(AdminReportAiPolicyCatalog::candidateRule)
                .toList();
    }

    public static boolean containsRule(String reasonCode, String ruleId) {
        return candidateRules(reasonCode).stream()
                .anyMatch(rule -> rule.getRuleId().equals(ruleId));
    }

    private static AdminReportAiCandidateRuleRequestDTO candidateRule(String ruleId) {
        RuleDefinition definition = RULE_DEFINITIONS.get(ruleId);
        return AdminReportAiCandidateRuleRequestDTO.builder()
                .ruleId(ruleId)
                .ruleVersion(RULE_VERSION)
                .ruleStatus(RULE_STATUS)
                .ruleFamily(definition.ruleFamily())
                .ruleType(definition.ruleType())
                .material(definition.material())
                .applicableTargetTypes(RUNTIME_TARGET_TYPES)
                .requirementProfileIds(definition.requirementProfileIds())
                .requiredEvidenceKinds(definition.requiredEvidenceKinds())
                .conditionalRequirements(definition.conditionalRequirements().stream()
                        .map(AdminReportAiPolicyCatalog::copyRequirement)
                        .toList())
                .semanticRequirementCodes(definition.semanticRequirementCodes())
                .counterEvidenceRequired(definition.counterEvidenceRequired())
                .exceptionCodes(definition.exceptionCodes())
                .evaluationCeiling(EVALUATION_CEILING)
                .allowedOutcomes(PROPOSED_ALLOWED_OUTCOMES)
                .allowedCandidateActions(
                        "VIOLATION".equals(definition.ruleType())
                                ? PROPOSED_VIOLATION_ACTIONS
                                : PROPOSED_ROUTING_ACTIONS)
                .build();
    }

    private static AdminReportAiRuleRequirementRequestDTO copyRequirement(
            AdminReportAiRuleRequirementRequestDTO requirement) {
        return AdminReportAiRuleRequirementRequestDTO.builder()
                .requirementCode(requirement.getRequirementCode())
                .evidenceKind(requirement.getEvidenceKind())
                .requirementType(requirement.getRequirementType())
                .trigger(requirement.getTrigger())
                .missingBehavior(requirement.getMissingBehavior())
                .build();
    }

    private static Map<String, RuleDefinition> ruleDefinitions() {
        Map<String, RuleDefinition> values = new LinkedHashMap<>();
        REASON_RULES.values().stream()
                .flatMap(List::stream)
                .distinct()
                .forEach(ruleId -> {
                    String family = ruleId.split("\\.")[1];
                    boolean routing = "ROUTE".equals(family);
                    boolean mediaRouting = "CSR.ROUTE.003".equals(ruleId);
                    List<String> requiredEvidenceKinds = mediaRouting
                            ? List.of("TARGET_IDENTITY", "TARGET_STATE", "TARGET_MEDIA_REFERENCE")
                            : List.of("TARGET_IDENTITY", "TARGET_STATE", "TARGET_TEXT_CONTENT");
                    List<String> profiles = mediaRouting
                            ? List.of("RP-MEDIA-CONDITIONAL")
                            : List.of("RP-TEXT-CONTEXT");
                    values.put(ruleId, new RuleDefinition(
                            family,
                            routing ? "ROUTING" : "VIOLATION",
                            !routing,
                            profiles,
                            requiredEvidenceKinds,
                            List.of(parentContextRequirement()),
                            semanticRequirements(family),
                            !routing,
                            routing ? List.of() : List.of("CONTEXTUAL_EXCEPTION")));
                });
        return Map.copyOf(values);
    }

    private static AdminReportAiRuleRequirementRequestDTO parentContextRequirement() {
        return AdminReportAiRuleRequirementRequestDTO.builder()
                .requirementCode("REQ-PARENT-CONTEXT-WHEN-MATERIAL")
                .evidenceKind("PARENT_BLOG_CONTEXT")
                .requirementType("CONDITIONAL")
                .trigger("COMMENT_MEANING_DEPENDS_ON_PARENT")
                .missingBehavior("NEEDS_MANUAL_REVIEW")
                .build();
    }

    private static List<String> semanticRequirements(String family) {
        return switch (family) {
            case "SAF" -> List.of("SEM-TARGETED-SUBJECT", "SEM-THREAT-CREDIBILITY", "SEM-EXCEPTION-CONTEXT");
            case "HATE" -> List.of(
                    "SEM-TARGETED-SUBJECT", "SEM-PROTECTED-CHARACTERISTIC", "SEM-EXCEPTION-CONTEXT");
            case "HAR" -> List.of("SEM-TARGETED-SUBJECT", "SEM-PATTERN-HISTORY", "SEM-EXCEPTION-CONTEXT");
            case "SEX" -> List.of("SEM-AGE-CONSENT", "SEM-EXCEPTION-CONTEXT");
            case "INT" -> List.of(
                    "SEM-IDENTITY-COMPARATOR", "SEM-AUTHORITATIVE-FACT",
                    "SEM-TRANSACTION-INTENT", "SEM-EXCEPTION-CONTEXT");
            case "IP" -> List.of(
                    "SEM-CLAIMANT-AUTHORITY", "SEM-LICENSE-PERMISSION",
                    "SEM-JURISDICTION", "SEM-EXCEPTION-CONTEXT");
            case "PRIV" -> List.of(
                    "SEM-PRIVACY-AUTHORIZATION", "SEM-JURISDICTION", "SEM-EXCEPTION-CONTEXT");
            case "COM" -> List.of("SEM-RESTRICTED-REGISTRY", "SEM-JURISDICTION", "SEM-EXCEPTION-CONTEXT");
            default -> List.of("SEM-EXCEPTION-CONTEXT", "SEM-COMPLETE-EVALUATION-SCOPE");
        };
    }

    private static Map<String, List<String>> reasonRules() {
        Map<String, List<String>> values = new LinkedHashMap<>();
        values.put("DISLIKE_CONTENT", List.of("CSR.ROUTE.001"));
        values.put("SPAM", List.of("CSR.SPAM.001"));
        values.put("BULLYING_OR_UNWANTED_CONTACT", List.of("CSR.HAR.002"));
        values.put("SCAM_OR_FRAUD", List.of("CSR.INT.003"));
        values.put("HARASSMENT", List.of("CSR.HAR.001", "CSR.HAR.002"));
        values.put("SELF_HARM_OR_ABNORMAL_EATING", List.of("CSR.SAF.003"));
        values.put("HATE_SPEECH", List.of("CSR.HATE.001"));
        values.put("VIOLENCE_HATE_OR_EXPLOITATION", List.of("CSR.SAF.002", "CSR.HATE.001"));
        values.put("VIOLENCE_THREAT", List.of("CSR.SAF.001"));
        values.put("NUDITY_OR_SEXUAL_ACTIVITY", List.of("CSR.SEX.001"));
        values.put("SEXUAL_CONTENT", List.of("CSR.SEX.001", "CSR.SEX.002"));
        values.put("PRIVACY_VIOLATION", List.of("CSR.PRIV.001"));
        values.put("SCAM_FRAUD_OR_SPAM", List.of("CSR.INT.003", "CSR.SPAM.001"));
        values.put("COPYRIGHT_VIOLATION", List.of("CSR.IP.001"));
        values.put("FALSE_INFORMATION", List.of("CSR.INT.002"));
        values.put("INTELLECTUAL_PROPERTY", List.of("CSR.IP.001", "CSR.IP.002"));
        values.put("IMPERSONATION", List.of("CSR.INT.001"));
        values.put("FAKE_OR_MISLEADING", List.of("CSR.INT.001", "CSR.INT.002"));
        values.put("INAPPROPRIATE_IMAGE", List.of("CSR.ROUTE.003"));
        values.put("RESTRICTED_GOODS", List.of("CSR.COM.001"));
        values.put("OFF_TOPIC_OR_IRRELEVANT", List.of("CSR.REL.001"));
        values.put("OTHER", List.of("CSR.ROUTE.002"));
        return Map.copyOf(values);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private record RuleDefinition(
            String ruleFamily,
            String ruleType,
            boolean material,
            List<String> requirementProfileIds,
            List<String> requiredEvidenceKinds,
            List<AdminReportAiRuleRequirementRequestDTO> conditionalRequirements,
            List<String> semanticRequirementCodes,
            boolean counterEvidenceRequired,
            List<String> exceptionCodes) {
    }
}
