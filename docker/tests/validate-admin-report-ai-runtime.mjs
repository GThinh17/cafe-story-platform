import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { createRequire } from 'node:module';
import { readFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDirectory = dirname(fileURLToPath(import.meta.url));
const repositoryRoot = resolve(scriptDirectory, '../..');
const require = createRequire(import.meta.url);
const Ajv2020 = require(
  join(repositoryRoot, '2-cafe-story-nextjs-web/node_modules/ajv/dist/2020'),
).default;
const addFormats = require(
  join(repositoryRoot, '2-cafe-story-nextjs-web/node_modules/ajv-formats'),
).default;
const endpoint =
  process.env.ADMIN_REPORT_AI_WEBHOOK_URL ||
  'http://127.0.0.1:5678/webhook/cafestory-admin-report-ai-resolution';
const envPath = join(repositoryRoot, 'docker/.env');

const readDotEnvValue = (name) => {
  const content = readFileSync(envPath, 'utf8');
  const escaped = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const matches = [...content.matchAll(new RegExp(`^\\s*${escaped}\\s*=\\s*(.*)$`, 'gm'))];
  assert.ok(matches.length <= 1, `Duplicate ${name} entries`);
  if (!matches.length) return '';
  return matches[0][1].trim().replace(/^(['"])(.*)\1$/, '$2');
};

const secret = readDotEnvValue('ADMIN_REPORT_AI_HMAC_SECRET');
assert.ok(secret, 'ADMIN_REPORT_AI_HMAC_SECRET is required');

const readJson = (path) => JSON.parse(readFileSync(path, 'utf8'));
const runtimeSchema = readJson(
  join(repositoryRoot, 'docker/contracts/admin-report-ai-runtime-request-s2.schema.json'),
);
const providerSchema = readJson(
  join(repositoryRoot, 'docker/contracts/admin-report-ai-provider-output-s2.schema.json'),
);
const ajv = new Ajv2020({ allErrors: true, strict: true });
addFormats(ajv);
const validateRuntime = ajv.compile(runtimeSchema);
const validateProvider = ajv.compile(providerSchema);

const canonicalize = (value) => {
  if (Array.isArray(value)) return `[${value.map(canonicalize).join(',')}]`;
  if (value && typeof value === 'object') {
    return `{${Object.keys(value)
      .sort()
      .map((key) => `${JSON.stringify(key)}:${canonicalize(value[key])}`)
      .join(',')}}`;
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

const digest = (value) =>
  `sha256:${crypto.createHash('sha256').update(canonicalize(value), 'utf8').digest('hex')}`;

const signatureHeaders = (body) => {
  const timestamp = String(Math.floor(Date.now() / 1000));
  const nonce = crypto.randomUUID();
  const bodyHash = crypto.createHash('sha256').update(canonicalize(body), 'utf8').digest('hex');
  const signature = crypto
    .createHmac('sha256', secret)
    .update(`${timestamp}\n${nonce}\n${bodyHash}`, 'utf8')
    .digest('hex');
  return {
    'content-type': 'application/json',
    'x-cafestory-contract-version': body.contractVersion,
    'x-cafestory-correlation-id': body.correlationId,
    'x-cafestory-timestamp': timestamp,
    'x-cafestory-nonce': nonce,
    'x-cafestory-body-sha256': bodyHash,
    'x-cafestory-signature': signature,
  };
};

const candidateRule = {
  ruleId: 'CSR.INT.003',
  ruleVersion: '1.0.0-proposed.2',
  ruleStatus: 'ACTIVE',
  ruleFamily: 'INT',
  ruleType: 'VIOLATION',
  material: true,
  applicableTargetTypes: ['BLOG', 'COMMENT'],
  requirementProfileIds: ['RP-TEXT-CONTEXT'],
  requiredEvidenceKinds: ['TARGET_IDENTITY', 'TARGET_STATE', 'TARGET_TEXT_CONTENT'],
  conditionalRequirements: [],
  semanticRequirementCodes: [
    'SEM-IDENTITY-COMPARATOR',
    'SEM-AUTHORITATIVE-FACT',
    'SEM-TRANSACTION-INTENT',
    'SEM-EXCEPTION-CONTEXT',
  ],
  counterEvidenceRequired: true,
  exceptionCodes: ['CONTEXTUAL_EXCEPTION'],
  evaluationCeiling: 'RESOLVE_OR_REJECT',
  allowedOutcomes: [
    'SUBSTANTIATED',
    'NOT_SUBSTANTIATED',
    'NOT_APPLICABLE',
    'UNASSESSABLE',
    'CONFLICTED',
    'POLICY_INVALID',
  ],
  allowedCandidateActions: ['KEEP_VISIBLE', 'HIDE', 'REMOVE', 'NO_ACTION'],
};

const createEvidence = ({ evidenceId, evidenceKind, targetType, targetAlias, snapshotHash, value }) => ({
  evidenceId,
  envelopeVersion: '1.0.0-rc.1',
  evidenceKind,
  subject: {
    targetType,
    targetAlias,
    snapshotVersion: '1',
    snapshotHash,
  },
  source: {
    sourceType: 'TARGET_SNAPSHOT',
    sourceSystem: 'CAFE_STORY_BACKEND',
    verificationStatus: 'SYSTEM_CAPTURED',
    authorityScope: 'PLATFORM_OWNED_FIELD',
  },
  capture: {
    collectorName: 'cafestory-backend',
    collectorVersion: 's2.2',
    capturedAt: new Date().toISOString(),
    transformations: [],
  },
  integrity: {
    canonicalization: 'JCS',
    digestAlgorithm: 'SHA-256',
    payloadDigest: digest(value),
  },
  availability: { status: 'AVAILABLE' },
  quality: { level: 'HIGH', reasonCodes: ['SYNTHETIC_RUNTIME_TEST'] },
  privacy: {
    classification: 'PUBLIC_CONTENT',
    containsPersonalData: false,
    redactionStatus: 'NOT_REQUIRED',
    retentionClass: 'REPORT_EVIDENCE_90D',
  },
  intendedUse: 'RULE_EVALUATION_CANDIDATE',
  collectedForRuleIds: [candidateRule.ruleId],
  payload: {
    representation: 'INLINE',
    mediaType: 'application/json',
    value,
    truncated: false,
  },
});

const createRequestBody = ({ targetType, description, reasonLabel, contentText }) => {
  const reportId = crypto.randomUUID();
  const targetId = crypto.randomUUID();
  const targetAlias = `target-${targetType.toLowerCase()}-runtime`;
  const capturedAt = new Date().toISOString();
  const observableFields = {
    contentText,
    imageUrls: [],
    status: 'PUBLISHED',
    createdAt: capturedAt,
    updatedAt: capturedAt,
    ...(targetType === 'COMMENT'
      ? { parentBlogId: crypto.randomUUID(), parentBlogExcerpt: 'Bài viết về trải nghiệm cà phê.' }
      : {}),
  };
  const snapshotHash = digest({ targetType, targetId, observableFields });
  const evidence = [
    createEvidence({
      evidenceId: 'EV-TARGET-IDENTITY',
      evidenceKind: 'TARGET_IDENTITY',
      targetType,
      targetAlias,
      snapshotHash,
      value: { targetType, targetAlias },
    }),
    createEvidence({
      evidenceId: 'EV-TARGET-STATE',
      evidenceKind: 'TARGET_STATE',
      targetType,
      targetAlias,
      snapshotHash,
      value: { status: 'PUBLISHED' },
    }),
    createEvidence({
      evidenceId: 'EV-TARGET-CONTENT',
      evidenceKind: 'TARGET_TEXT_CONTENT',
      targetType,
      targetAlias,
      snapshotHash,
      value: { sanitizedText: contentText },
    }),
  ];
  return {
    contractVersion: '2.0',
    correlationId: crypto.randomUUID(),
    idempotencyKey: crypto.randomBytes(32).toString('hex'),
    requestedAt: capturedAt,
    automationMode: 'A0_RECOMMEND_ONLY',
    reportId,
    targetType,
    targetId,
    reasonCode: 'SCAM_OR_FRAUD',
    reasonLabel,
    reasonSeverity: 4,
    description,
    contentText,
    imageUrls: [],
    reportStatus: 'OPEN',
    existingModerationResult: null,
    sameTargetOpenReportCount: 1,
    reportClaim: {
      reportId,
      status: 'OPEN',
      reasonCode: 'SCAM_OR_FRAUD',
      reasonCatalogVersion: 'IRC-2.0.0-proposed.1',
      description,
      trustLevel: 'UNTRUSTED_REPORTER_CLAIM',
    },
    targetSnapshot: {
      targetType,
      targetId,
      targetAlias,
      snapshotVersion: '1',
      capturedAt,
      observableFields,
      snapshotHash,
    },
    evidence,
    policyContext: {
      contextSchemaVersion: 'RRC-1.0.0-rc.1',
      policyVersion: 'PF-2.0.0-proposed.1',
      policyStatus: 'ACTIVE',
      ruleCatalogVersion: 'RC-2.0.0-proposed.2',
      ruleCatalogStatus: 'ACTIVE',
      requirementMatrixVersion: '1.0.0-rc.1',
      evidenceKindCatalogVersion: '1.0.0-rc.1',
      evaluationMode: 'ACTIVE_RUNTIME',
      candidateRules: [structuredClone(candidateRule)],
      availableEvidenceKinds: ['TARGET_IDENTITY', 'TARGET_STATE', 'TARGET_TEXT_CONTENT'],
      missingRequirements: [],
      currentEvaluationCeiling: 'RESOLVE_OR_REJECT',
    },
    executionConstraints: {
      recommendationOnly: true,
      allowedCandidateActions: ['NO_ACTION', 'KEEP_VISIBLE', 'HIDE', 'REMOVE'],
      criticalMissingBehavior: 'NEEDS_MANUAL_REVIEW',
      criticalEvidenceMissing: false,
    },
  };
};

const providerFields = Object.keys(providerSchema.properties);
const providerProjection = (responseBody) =>
  Object.fromEntries(providerFields.map((field) => [field, responseBody[field]]));
const suppliedEvidenceIds = (body) => new Set(body.evidence.map((item) => item.evidenceId));
const allNarrative = (body) =>
  [body.explanation, ...body.findings.map((finding) => finding.rationale)].join('\n');

const verifyResponseSignature = (response, body, requestBody) => {
  const timestamp = response.headers.get('x-cafestory-timestamp');
  const nonce = response.headers.get('x-cafestory-nonce');
  const bodyHash = response.headers.get('x-cafestory-body-sha256');
  const signature = response.headers.get('x-cafestory-signature');
  assert.equal(response.headers.get('x-cafestory-contract-version'), '2.0');
  assert.equal(response.headers.get('x-cafestory-correlation-id'), requestBody.correlationId);
  assert.ok(timestamp && nonce && bodyHash && signature, 'Signed response headers are required');
  const expectedBodyHash = crypto
    .createHash('sha256')
    .update(canonicalize(body), 'utf8')
    .digest('hex');
  assert.equal(bodyHash, expectedBodyHash, 'Response body hash must match the signed body');
  const expectedSignature = crypto
    .createHmac('sha256', secret)
    .update(`${timestamp}\n${nonce}\n${bodyHash}`, 'utf8')
    .digest('hex');
  assert.equal(signature, expectedSignature, 'Response HMAC must be valid');
};

const cases = [
  {
    name: 'english-description-english-reason',
    expectedLanguage: 'en',
    request: createRequestBody({
      targetType: 'BLOG',
      description: 'This blog promotes a fake voucher and requests an OTP.',
      reasonLabel: 'Scam or fraud',
      contentText:
        'Claim a free coffee voucher at cafe-reward.example and enter your OTP to receive it now.',
    }),
  },
  {
    name: 'vietnamese-comment-scam',
    expectedLanguage: 'vi',
    request: createRequestBody({
      targetType: 'COMMENT',
      description: 'Bình luận này có dấu hiệu lừa đảo và yêu cầu mã OTP.',
      reasonLabel: 'Lừa đảo hoặc gian lận',
      contentText:
        'Nhận voucher cà phê miễn phí, đăng nhập tại cafe-qua-tang.example và gửi mã OTP để nhận ngay.',
    }),
  },
  {
    name: 'english-description-vietnamese-reason',
    expectedLanguage: 'en',
    request: createRequestBody({
      targetType: 'BLOG',
      description: 'This post contains a phishing link and asks for an OTP.',
      reasonLabel: 'Nội dung lừa đảo hoặc gây hiểu nhầm',
      contentText:
        'Open cafe-bonus.example, submit your account password and OTP, then claim a guaranteed reward.',
    }),
  },
];

const results = [];
for (const runtimeCase of cases) {
  assert.equal(
    validateRuntime(runtimeCase.request),
    true,
    `${runtimeCase.name} is not exact Contract V2: ${JSON.stringify(validateRuntime.errors)}`,
  );
  const before = structuredClone(runtimeCase.request);
  const response = await fetch(endpoint, {
    method: 'POST',
    headers: signatureHeaders(runtimeCase.request),
    body: JSON.stringify(runtimeCase.request),
    signal: AbortSignal.timeout(120_000),
  });
  const responseText = await response.text();
  assert.equal(response.status, 200, `${runtimeCase.name} returned ${response.status}: ${responseText}`);
  const responseBody = JSON.parse(responseText);
  assert.deepEqual(runtimeCase.request, before, `${runtimeCase.name} mutated the request object`);
  assert.equal(responseBody.contractVersion, '2.0');
  assert.equal(responseBody.correlationId, runtimeCase.request.correlationId);
  assert.equal(
    validateProvider(providerProjection(responseBody)),
    true,
    `${runtimeCase.name} provider output schema failed: ${JSON.stringify(validateProvider.errors)}`,
  );
  verifyResponseSignature(response, responseBody, runtimeCase.request);
  assert.ok(responseBody.findings.length > 0, `${runtimeCase.name} must return evidence-backed findings`);
  const evidenceIds = suppliedEvidenceIds(runtimeCase.request);
  for (const finding of responseBody.findings) {
    assert.equal(finding.ruleId, candidateRule.ruleId, 'Finding Rule ID must be supplied by Backend');
    for (const evidenceId of [...finding.evidenceIds, ...finding.counterEvidenceIds]) {
      assert.ok(evidenceIds.has(evidenceId), `Unknown Evidence ID: ${evidenceId}`);
    }
    assert.match(
      finding.rationale,
      /OTP|cafe-(?:reward|qua-tang|bonus)\.example|password|mật khẩu|voucher/i,
      `${runtimeCase.name} rationale must identify a concrete TARGET_TEXT_CONTENT indicator`,
    );
  }
  const narrative = allNarrative(responseBody);
  if (runtimeCase.expectedLanguage === 'vi') {
    assert.match(
      narrative,
      /\b(?:nội dung|bằng chứng|quan sát|lừa đảo|yêu cầu|suy luận)\b/iu,
      `${runtimeCase.name} explanation/rationale must be Vietnamese`,
    );
  } else {
    assert.match(
      narrative,
      /\b(?:evidence|observed|content|request|phishing|inference)\b/i,
      `${runtimeCase.name} explanation/rationale must be English`,
    );
    assert.doesNotMatch(
      narrative,
      /\b(?:nội dung|bằng chứng|quan sát|lừa đảo|yêu cầu|suy luận)\b/iu,
      `${runtimeCase.name} must not switch to Vietnamese because of the reason snapshot`,
    );
  }
  assert.equal('confidenceScore' in responseBody, false);
  assert.equal('riskScore' in responseBody, false);
  assert.equal('executionResult' in responseBody, false);
  assert.equal('executedAction' in responseBody, false);
  assert.doesNotMatch(
    narrative,
    /\b(?:has been hidden|has been removed|report was resolved|đã ẩn nội dung|đã gỡ nội dung|đã giải quyết báo cáo)\b/iu,
    `${runtimeCase.name} must not claim that AI mutated target or report state`,
  );
  results.push({
    name: runtimeCase.name,
    expectedLanguage: runtimeCase.expectedLanguage,
    reportDecision: responseBody.reportDecision,
    targetAction: responseBody.targetAction,
    findingCount: responseBody.findings.length,
    responseSignatureVerified: true,
    providerSchemaValid: true,
    evidenceReferencesBounded: true,
    mutationClaimAbsent: true,
    explanation: responseBody.explanation,
    findings: responseBody.findings.map((finding) => ({
      ruleId: finding.ruleId,
      ruleVersion: finding.ruleVersion,
      evidenceIds: finding.evidenceIds,
      rationale: finding.rationale,
    })),
  });
}

console.log(JSON.stringify({
  status: 'PASS',
  endpoint,
  exactContractV2: true,
  runtimeCases: results,
  scoreFieldsAbsent: true,
  executionFieldsAbsent: true,
  secretValuesPrinted: false,
}, null, 2));
