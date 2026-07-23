package com.cafestory.service.serviceImplement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AdminReportAiPolicyCatalog {

    public static final String POLICY_VERSION = "PF-2.0.0-proposed.1";
    public static final String RULE_CATALOG_VERSION = "RC-2.0.0-proposed.1";
    public static final String REASON_CATALOG_VERSION = "IRC-2.0.0-proposed.1";
    public static final String PROMPT_VERSION = "report-ai-v2-sprint1.1";
    public static final String WORKFLOW_VERSION = "cafestory-admin-report-ai-resolution-v2";
    public static final String RULE_VERSION = "1.0.0-proposed.1";

    private static final Map<String, List<String>> REASON_RULES = reasonRules();

    private AdminReportAiPolicyCatalog() {
    }

    public static List<Map<String, Object>> candidateRules(String reasonCode) {
        return REASON_RULES.getOrDefault(normalize(reasonCode), List.of("CSR.ROUTE.002")).stream()
                .map(ruleId -> {
                    Map<String, Object> rule = new LinkedHashMap<>();
                    rule.put("ruleId", ruleId);
                    rule.put("ruleVersion", RULE_VERSION);
                    return rule;
                })
                .toList();
    }

    public static boolean containsRule(String reasonCode, String ruleId) {
        return candidateRules(reasonCode).stream()
                .anyMatch(rule -> rule.get("ruleId").equals(ruleId));
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
}
