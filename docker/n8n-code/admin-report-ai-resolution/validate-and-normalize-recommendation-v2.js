const crypto = require('crypto');
const request = $('Validate Contract V2 And Build Request').first().json.requestContext;
function outputText(response) {
  if (typeof response.output_text === 'string') return response.output_text;
  for (const item of Array.isArray(response.output) ? response.output : []) {
    for (const part of Array.isArray(item.content) ? item.content : []) {
      if (typeof part.text === 'string') return part.text;
    }
  }
  return '';
}
const text = outputText($json).trim();
if (!text) throw new Error('OpenAI response did not include output_text');
const result = JSON.parse(text);
const candidateById = new Map(
  request.policyContext.candidateRules.map((item) => [String(item.ruleId), item]),
);
const allowedRules = new Set(candidateById.keys());
const evidenceById = new Map(request.evidence.map((item) => [String(item.evidenceId), item]));
const allowedEvidence = new Set(evidenceById.keys());
const allowedMissing = new Set([
  ...allowedEvidence,
  ...(Array.isArray(request.policyContext.missingRequirements)
    ? request.policyContext.missingRequirements.map((item) => String(item.missingRequirementId))
    : []),
]);
const findings = Array.isArray(result.findings) ? result.findings : [];
const codePattern = /^[A-Za-z0-9][A-Za-z0-9._:-]{0,127}$/;
const isBoundedUniqueCodes = (values, maximum) =>
  Array.isArray(values) &&
  values.length <= maximum &&
  values.every((value) => codePattern.test(String(value))) &&
  new Set(values.map(String)).size === values.length;
const findingFields = new Set([
  'ruleId',
  'ruleVersion',
  'outcome',
  'evidenceIds',
  'counterEvidenceIds',
  'missingEvidenceIds',
  'violationLikelihood',
  'rationale',
]);
const exactFindingShape = (finding) =>
  finding &&
  typeof finding === 'object' &&
  !Array.isArray(finding) &&
  Object.keys(finding).length === findingFields.size &&
  Object.keys(finding).every((key) => findingFields.has(key));
const hasUnknownEvidence = (references, allowlist = allowedEvidence) =>
  !Array.isArray(references) || references.some((id) => !allowlist.has(String(id)));
const schemaBoundViolation =
  !Array.isArray(result.findings) ||
  result.findings.length > 8 ||
  !isBoundedUniqueCodes(result.blockedReasons, 16) ||
  !isBoundedUniqueCodes(result.labels, 16) ||
  typeof result.explanation !== 'string' ||
  result.explanation.length < 1 ||
  result.explanation.length > 4000 ||
  findings.some(
    (finding) =>
      !exactFindingShape(finding) ||
      !codePattern.test(String(finding.ruleId || '')) ||
      !codePattern.test(String(finding.ruleVersion || '')) ||
      ![
        'SUBSTANTIATED',
        'NOT_SUBSTANTIATED',
        'NOT_APPLICABLE',
        'UNASSESSABLE',
        'CONFLICTED',
        'POLICY_INVALID',
      ].includes(finding.outcome) ||
      !['HIGH', 'MEDIUM', 'LOW', 'UNKNOWN'].includes(finding.violationLikelihood) ||
      typeof finding.rationale !== 'string' ||
      finding.rationale.length < 1 ||
      finding.rationale.length > 4000 ||
      !isBoundedUniqueCodes(finding.evidenceIds, 32) ||
      !isBoundedUniqueCodes(finding.counterEvidenceIds, 32) ||
      !isBoundedUniqueCodes(finding.missingEvidenceIds, 32),
  ) ||
  !result.evidenceSummary ||
  typeof result.evidenceSummary !== 'object' ||
  Array.isArray(result.evidenceSummary) ||
  !['usedEvidenceIds', 'counterEvidenceIds', 'missingEvidenceIds'].every((field) =>
    isBoundedUniqueCodes(result.evidenceSummary?.[field], 32),
  ) ||
  Object.keys(result.evidenceSummary || {}).some(
    (field) => !['usedEvidenceIds', 'counterEvidenceIds', 'missingEvidenceIds'].includes(field),
  );
const unknownReference =
  findings.some(
    (finding) =>
      !allowedRules.has(String(finding.ruleId)) ||
      hasUnknownEvidence(finding.evidenceIds) ||
      hasUnknownEvidence(finding.counterEvidenceIds) ||
      hasUnknownEvidence(finding.missingEvidenceIds, allowedMissing),
  ) ||
  !result.evidenceSummary ||
  hasUnknownEvidence(result.evidenceSummary.usedEvidenceIds) ||
  hasUnknownEvidence(result.evidenceSummary.counterEvidenceIds) ||
  hasUnknownEvidence(result.evidenceSummary.missingEvidenceIds, allowedMissing);
const duplicateRuleEvaluation =
  new Set(findings.map((finding) => String(finding.ruleId))).size !== findings.length;
const ruleVersionMismatch = findings.some((finding) => {
  const rule = candidateById.get(String(finding.ruleId));
  return rule && String(finding.ruleVersion) !== String(rule.ruleVersion);
});
const outcomeOutsideRule = findings.some((finding) => {
  const rule = candidateById.get(String(finding.ruleId));
  return !rule || !rule.allowedOutcomes.includes(finding.outcome);
});
const independentSourceTypes = new Set([
  'PLATFORM_RECORD',
  'TARGET_SNAPSHOT',
  'VERIFIED_MEDIA_OBSERVATION',
  'AUTHORITATIVE_EXTERNAL_REFERENCE',
]);
const isIndependentUsableEvidence = (id) => {
  const evidence = evidenceById.get(String(id));
  if (
    !evidence ||
    String(evidence.availability?.status) !== 'AVAILABLE' ||
    String(evidence.quality?.level) === 'UNUSABLE'
  ) {
    return false;
  }
  return independentSourceTypes.has(String(evidence.source?.sourceType || ''));
};
const unsupportedEvidenceBasis = findings.some(
  (finding) =>
    finding.outcome === 'SUBSTANTIATED' &&
    (!Array.isArray(finding.evidenceIds) || !finding.evidenceIds.some(isIndependentUsableEvidence)),
);
const unresolvedCounterEvidence = findings.some((finding) => {
  const rule = candidateById.get(String(finding.ruleId));
  return (
    finding.outcome === 'SUBSTANTIATED' &&
    rule?.counterEvidenceRequired === true &&
    Array.isArray(finding.counterEvidenceIds) &&
    finding.counterEvidenceIds.length > 0
  );
});
const materialRules = [...candidateById.values()].filter((rule) => rule.material === true);
const outcomesByRule = new Map(findings.map((finding) => [String(finding.ruleId), finding.outcome]));
const completeOutcomes = new Set(['SUBSTANTIATED', 'NOT_SUBSTANTIATED', 'NOT_APPLICABLE']);
const nonManualRequested = result.reportDecision !== 'NEEDS_MANUAL_REVIEW';
const incompleteScope =
  nonManualRequested &&
  (duplicateRuleEvaluation ||
    materialRules.length === 0 ||
    materialRules.some((rule) => !completeOutcomes.has(outcomesByRule.get(String(rule.ruleId)))));
const missingEvidenceKindBurden = materialRules.some((rule) =>
  rule.requiredEvidenceKinds.some(
    (kind) =>
      !request.evidence.some(
        (item) =>
          item.evidenceKind === kind &&
          item.availability?.status === 'AVAILABLE' &&
          item.quality?.level !== 'UNUSABLE' &&
          independentSourceTypes.has(String(item.source?.sourceType || '')) &&
          item.collectedForRuleIds.includes(rule.ruleId),
      ),
  ),
);
const missingRequirements = Array.isArray(request.policyContext.missingRequirements)
  ? request.policyContext.missingRequirements
  : [];
const semanticCodes = new Set(
  request.policyContext.candidateRules.flatMap((rule) => rule.semanticRequirementCodes || []),
);
const semanticRequirementMissing = missingRequirements.some(
  (item) => item.status === 'MISSING' && semanticCodes.has(item.requirementCode),
);
const evidenceKindRequirementMissing =
  missingEvidenceKindBurden ||
  missingRequirements.some((item) => item.status === 'MISSING' && item.evidenceKind);
const resolveAggregationValid =
  materialRules.some(
    (rule) =>
      rule.ruleType === 'VIOLATION' && outcomesByRule.get(String(rule.ruleId)) === 'SUBSTANTIATED',
  ) && materialRules.every((rule) => completeOutcomes.has(outcomesByRule.get(String(rule.ruleId))));
const rejectAggregationValid =
  materialRules.length > 0 &&
  materialRules.every((rule) =>
    ['NOT_SUBSTANTIATED', 'NOT_APPLICABLE'].includes(outcomesByRule.get(String(rule.ruleId))),
  );
const aggregationInvalid =
  nonManualRequested &&
  ((result.reportDecision === 'RESOLVE' && !resolveAggregationValid) ||
    (result.reportDecision === 'REJECT' && !rejectAggregationValid) ||
    !['RESOLVE', 'REJECT'].includes(result.reportDecision));
const actionBurdenAbsent =
  nonManualRequested &&
  ((result.reportDecision === 'RESOLVE' &&
    materialRules
      .filter((rule) => outcomesByRule.get(String(rule.ruleId)) === 'SUBSTANTIATED')
      .some((rule) => !rule.allowedCandidateActions.includes(result.targetAction))) ||
    (result.reportDecision === 'REJECT' &&
      materialRules.some((rule) => !rule.allowedCandidateActions.includes(result.targetAction))));
const ceilingExceeded =
  nonManualRequested &&
  (request.policyContext.currentEvaluationCeiling !== 'RESOLVE_OR_REJECT' ||
    (result.reportDecision === 'RESOLVE' &&
      materialRules
        .filter((rule) => outcomesByRule.get(String(rule.ruleId)) === 'SUBSTANTIATED')
        .some((rule) => rule.evaluationCeiling !== 'RESOLVE_OR_REJECT')));
const criticalMissing = request.executionConstraints.criticalEvidenceMissing === true;
const insufficient = result.evidenceSufficiency !== 'SUFFICIENT';
const invalidCombination =
  (result.reportDecision === 'NEEDS_MANUAL_REVIEW' && result.targetAction !== 'NO_ACTION') ||
  (result.reportDecision === 'REJECT' && result.targetAction !== 'KEEP_VISIBLE') ||
  (result.reportDecision === 'RESOLVE' && !['HIDE', 'REMOVE'].includes(result.targetAction));
const policyLifecycleBlocked =
  request.policyContext.policyStatus !== 'ACTIVE' ||
  request.policyContext.ruleCatalogStatus !== 'ACTIVE' ||
  request.policyContext.evaluationMode !== 'ACTIVE_RUNTIME' ||
  request.policyContext.candidateRules.some((rule) => rule.ruleStatus !== 'ACTIVE');
const blocked = Array.isArray(result.blockedReasons) ? result.blockedReasons.map(String) : [];
if (schemaBoundViolation) blocked.push('PROVIDER_OUTPUT_BOUND_EXCEEDED');
if (unknownReference) blocked.push('UNKNOWN_RULE_OR_EVIDENCE_REFERENCE');
if (ruleVersionMismatch) blocked.push('RULE_VERSION_MISMATCH');
if (outcomeOutsideRule) blocked.push('RULE_CONTEXT_SCHEMA_MISMATCH');
if (unsupportedEvidenceBasis) blocked.push('UNSUPPORTED_EVIDENCE_BASIS');
if (unresolvedCounterEvidence) blocked.push('MATERIAL_COUNTER_EVIDENCE_UNRESOLVED');
if (evidenceKindRequirementMissing) blocked.push('EVIDENCE_KIND_REQUIREMENT_MISSING');
if (semanticRequirementMissing) blocked.push('SEMANTIC_REQUIREMENT_MISSING');
if (incompleteScope) blocked.push('INCOMPLETE_RULE_EVALUATION_SCOPE');
if (aggregationInvalid || actionBurdenAbsent) blocked.push('ACTION_BURDEN_NOT_SATISFIED');
if (ceilingExceeded) blocked.push('RULE_EVALUATION_CEILING_EXCEEDED');
if (criticalMissing) blocked.push('CRITICAL_EVIDENCE_MISSING');
if (insufficient) blocked.push('EVIDENCE_NOT_SUFFICIENT');
if (invalidCombination) blocked.push('DECISION_ACTION_NOT_ALLOWED');
if (policyLifecycleBlocked) blocked.push('POLICY_OR_RULE_CATALOG_NOT_ACTIVE');
if (nonManualRequested && result.recommendationState !== result.reportDecision) {
  blocked.push('ACTION_BURDEN_NOT_SATISFIED');
}
const manual = blocked.length > 0;
const normalizedEvidenceSummary = {
  usedEvidenceIds: Array.isArray(result.evidenceSummary?.usedEvidenceIds)
    ? [...new Set(result.evidenceSummary.usedEvidenceIds.map(String))]
        .filter((id) => allowedEvidence.has(id))
        .slice(0, 32)
    : [],
  counterEvidenceIds: Array.isArray(result.evidenceSummary?.counterEvidenceIds)
    ? [...new Set(result.evidenceSummary.counterEvidenceIds.map(String))]
        .filter((id) => allowedEvidence.has(id))
        .slice(0, 32)
    : [],
  missingEvidenceIds: Array.isArray(result.evidenceSummary?.missingEvidenceIds)
    ? [...new Set(result.evidenceSummary.missingEvidenceIds.map(String))]
        .filter((id) => allowedMissing.has(id))
        .slice(0, 32)
    : [],
};
const response = {
  contractVersion: '2.0',
  correlationId: request.correlationId,
  recommendationState: manual ? 'NEEDS_MANUAL_REVIEW' : result.recommendationState,
  reportDecision: manual ? 'NEEDS_MANUAL_REVIEW' : result.reportDecision,
  targetAction: manual ? 'NO_ACTION' : result.targetAction,
  findings: manual ? [] : findings,
  evidenceSummary: normalizedEvidenceSummary,
  blockedReasons: [...new Set(blocked.filter((reason) => codePattern.test(reason)))].slice(0, 16),
  evidenceQuality: result.evidenceQuality,
  evidenceSufficiency:
    manual && result.evidenceSufficiency === 'SUFFICIENT'
      ? 'UNASSESSABLE'
      : result.evidenceSufficiency,
  violationLikelihood: result.violationLikelihood,
  harmSeverity: result.harmSeverity,
  labels: Array.isArray(result.labels)
    ? [...new Set(result.labels.map(String))]
        .filter((label) => codePattern.test(label))
        .slice(0, 16)
    : [],
  explanation: String(result.explanation || 'Manual review is required.').slice(0, 4000),
  modelName: $json.model || $env.OPENAI_DECISION_MODEL || 'gpt-4o-mini',
  policyVersion: request.policyContext.policyVersion,
  ruleCatalogVersion: request.policyContext.ruleCatalogVersion,
  promptVersion: 'report-ai-v2-sprint2.2',
  workflowVersion: 'cafestory-admin-report-ai-resolution-v2-s2.2',
};
const secret = String($env.ADMIN_REPORT_AI_HMAC_SECRET ?? '');
if (!secret) throw new Error('Admin Report AI HMAC secret is not configured');
const canonicalize = (value) => {
  if (Array.isArray(value)) return `[${value.map(canonicalize).join(',')}]`;
  if (value && typeof value === 'object') {
    return `{${Object.keys(value).sort().map((key) => `${JSON.stringify(key)}:${canonicalize(value[key])}`).join(',')}}`;
  }
  if (
    value === null ||
    typeof value === 'string' ||
    typeof value === 'boolean' ||
    (typeof value === 'number' && Number.isFinite(value))
  ) {
    return JSON.stringify(value);
  }
  throw new Error('Payload is not valid JCS JSON');
};
const responseTimestamp = String(Math.floor(Date.now() / 1000));
const responseNonce = crypto.randomUUID();
const responseBodyHash = crypto
  .createHash('sha256')
  .update(canonicalize(response), 'utf8')
  .digest('hex');
const responseSignature = crypto
  .createHmac('sha256', secret)
  .update(`${responseTimestamp}\n${responseNonce}\n${responseBodyHash}`, 'utf8')
  .digest('hex');
return [{
  json: {
    response,
    responseHeaders: {
      contractVersion: response.contractVersion,
      correlationId: String(response.correlationId),
      timestamp: responseTimestamp,
      nonce: responseNonce,
      bodyHash: responseBodyHash,
      signature: responseSignature,
    },
  },
}];
