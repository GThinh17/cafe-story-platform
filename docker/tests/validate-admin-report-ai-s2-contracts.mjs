import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { createRequire } from 'node:module';
import { mkdtempSync, readFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
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

const readJson = (path) => JSON.parse(readFileSync(path, 'utf8'));
const runtimeSchemaPath = join(
  repositoryRoot,
  'docker/contracts/admin-report-ai-runtime-request-s2.schema.json',
);
const providerSchemaPath = join(
  repositoryRoot,
  'docker/contracts/admin-report-ai-provider-output-s2.schema.json',
);
const workflowPath = join(
  repositoryRoot,
  'docker/cafestory-admin-report-ai-resolution-n8n-workflow.json',
);
const buildCodePath = join(
  repositoryRoot,
  'docker/n8n-code/admin-report-ai-resolution/validate-contract-v2-and-build-request.js',
);

const runtimeSchema = readJson(runtimeSchemaPath);
const providerSchema = readJson(providerSchemaPath);
const workflow = readJson(workflowPath);
const ajv = new Ajv2020({ allErrors: true, strict: true });
addFormats(ajv);
const validateRuntime = ajv.compile(runtimeSchema);
const validateProvider = ajv.compile(providerSchema);
const clone = (value) => structuredClone(value);
const hash = 'sha256:' + 'a'.repeat(64);

const evidence = (evidenceId, evidenceKind, value, intendedUse = 'CONTEXT_ONLY') => ({
  evidenceId,
  envelopeVersion: '1.0.0-rc.1',
  evidenceKind,
  subject: {
    targetType: 'BLOG',
    targetAlias: 'target-blog-test',
    snapshotVersion: '1',
    snapshotHash: hash,
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
    capturedAt: '2026-07-29T14:00:00Z',
    transformations: [],
  },
  integrity: {
    canonicalization: 'JCS',
    digestAlgorithm: 'SHA-256',
    payloadDigest: hash,
    sourceDigest: hash,
  },
  availability: {
    status: 'AVAILABLE',
  },
  quality: {
    level: 'HIGH',
    reasonCodes: ['SYNTHETIC_TEST'],
  },
  privacy: {
    classification: 'PUBLIC_CONTENT',
    containsPersonalData: false,
    redactionStatus: 'NOT_REQUIRED',
    retentionClass: 'REPORT_EVIDENCE_90D',
  },
  intendedUse,
  collectedForRuleIds: ['CSR.SPAM.001'],
  payload: {
    representation: 'INLINE',
    mediaType: 'application/json',
    value,
    truncated: false,
  },
});

const runtimeRequest = () => ({
  contractVersion: '2.0',
  correlationId: crypto.randomUUID(),
  idempotencyKey: crypto.randomBytes(32).toString('hex'),
  requestedAt: '2026-07-29T14:00:00Z',
  automationMode: 'A1_AUTO_HIDE_BLOG_COMMENT',
  reportId: crypto.randomUUID(),
  targetType: 'BLOG',
  targetId: crypto.randomUUID(),
  reasonCode: 'SPAM',
  reportClaim: {
    reportId: crypto.randomUUID(),
    status: 'OPEN',
    reasonCode: 'SPAM',
    reasonCatalogVersion: 'IRC-2.0.0-proposed.1',
    description: 'Synthetic claim.',
    trustLevel: 'UNTRUSTED_REPORTER_CLAIM',
  },
  targetSnapshot: {
    targetType: 'BLOG',
    targetId: crypto.randomUUID(),
    targetAlias: 'target-blog-test',
    snapshotVersion: '1',
    capturedAt: '2026-07-29T14:00:00Z',
    observableFields: {
      contentText: 'Synthetic target text.',
      imageUrls: [],
      status: 'ACTIVE',
      createdAt: '2026-07-29T13:00:00',
      updatedAt: '2026-07-29T13:30:00',
    },
    snapshotHash: hash,
  },
  evidence: [
    evidence(
      'EV-TARGET-IDENTITY',
      'TARGET_IDENTITY',
      { targetType: 'BLOG', targetAlias: 'target-blog-test' },
    ),
    evidence(
      'EV-TARGET-STATE',
      'TARGET_STATE',
      { status: 'ACTIVE' },
    ),
    evidence(
      'EV-TARGET-CONTENT',
      'TARGET_TEXT_CONTENT',
      { sanitizedText: 'Synthetic target text.' },
      'RULE_EVALUATION_CANDIDATE',
    ),
    evidence(
      'EV-TARGET-AUTHOR',
      'TARGET_AUTHOR_CONTEXT',
      { authorType: 'BLOG_AUTHOR', userId: crypto.randomUUID(), userName: 'author', accountStatus: true },
    ),
    evidence(
      'EV-TARGET-REPORT-HISTORY',
      'TARGET_REPORT_HISTORY',
      { sameTargetOpenReportCount: 2, currentReportStatus: 'OPEN', reasonCode: 'SPAM', reasonSeverity: 4 },
    ),
    evidence(
      'EV-TARGET-MODERATION-HISTORY',
      'TARGET_MODERATION_HISTORY',
      { decision: 'VIOLATION', score: 91, tags: ['spam'], aiStatus: 'COMPLETED', resolved: false },
    ),
    evidence(
      'EV-TARGET-MEDIA',
      'TARGET_MEDIA_REFERENCE',
      {
        verificationMethod: 'PLATFORM_URL_METADATA_ONLY',
        referenceCount: 1,
        imageUrls: ['https://example.com/platform-image.jpg'],
        invalidUrlCount: 0,
        contentFetched: false,
        visionScanned: false,
      },
    ),
  ],
  policyContext: {
    contextSchemaVersion: 'RRC-1.0.0-rc.1',
    policyVersion: 'PF-2.0.0-proposed.1',
    policyStatus: 'PROPOSED',
    ruleCatalogVersion: 'RC-2.0.0-proposed.2',
    ruleCatalogStatus: 'PROPOSED',
    requirementMatrixVersion: '1.0.0-rc.1',
    evidenceKindCatalogVersion: '1.0.0-rc.1',
    evaluationMode: 'PROPOSED_EVALUATION_ONLY',
    candidateRules: [{
      ruleId: 'CSR.SPAM.001',
      ruleVersion: '1.0.0-proposed.2',
      ruleStatus: 'PROPOSED',
      ruleFamily: 'SPAM',
      ruleType: 'VIOLATION',
      material: true,
      applicableTargetTypes: ['BLOG', 'COMMENT'],
      requirementProfileIds: ['RP-TEXT-CONTEXT'],
      requiredEvidenceKinds: ['TARGET_IDENTITY', 'TARGET_STATE', 'TARGET_TEXT_CONTENT'],
      conditionalRequirements: [],
      semanticRequirementCodes: ['SEM-EXCEPTION-CONTEXT', 'SEM-COMPLETE-EVALUATION-SCOPE'],
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
    }],
    availableEvidenceKinds: [
      'TARGET_IDENTITY',
      'TARGET_STATE',
      'TARGET_TEXT_CONTENT',
      'TARGET_AUTHOR_CONTEXT',
      'TARGET_REPORT_HISTORY',
      'TARGET_MODERATION_HISTORY',
      'TARGET_MEDIA_REFERENCE',
    ],
    missingRequirements: [],
    currentEvaluationCeiling: 'RESOLVE_OR_REJECT',
  },
  executionConstraints: {
    recommendationOnly: true,
    allowedCandidateActions: ['NO_ACTION', 'KEEP_VISIBLE', 'HIDE', 'REMOVE'],
    criticalMissingBehavior: 'NEEDS_MANUAL_REVIEW',
    criticalEvidenceMissing: false,
  },
});

const providerOutput = () => ({
  recommendationState: 'RESOLVE',
  reportDecision: 'RESOLVE',
  targetAction: 'HIDE',
  findings: [{
    ruleId: 'CSR.SPAM.001',
    ruleVersion: '1.0.0-proposed.2',
    outcome: 'SUBSTANTIATED',
    evidenceIds: ['EV-TARGET-CONTENT'],
    counterEvidenceIds: [],
    missingEvidenceIds: [],
    violationLikelihood: 'HIGH',
    rationale: 'Synthetic bounded result.',
  }],
  evidenceSummary: {
    usedEvidenceIds: ['EV-TARGET-CONTENT'],
    counterEvidenceIds: [],
    missingEvidenceIds: [],
  },
  blockedReasons: [],
  evidenceQuality: 'HIGH',
  evidenceSufficiency: 'SUFFICIENT',
  violationLikelihood: 'HIGH',
  harmSeverity: 'MEDIUM',
  labels: ['SYNTHETIC'],
  explanation: 'Synthetic bounded result.',
});

const validRuntime = runtimeRequest();
assert.equal(validateRuntime(validRuntime), true, JSON.stringify(validateRuntime.errors));
const validProvider = providerOutput();
assert.equal(validateProvider(validProvider), true, JSON.stringify(validateProvider.errors));

const runtimeUnknown = clone(validRuntime);
runtimeUnknown.unknownAuthority = true;
assert.equal(validateRuntime(runtimeUnknown), false, 'Runtime unknown property must be rejected');
const runtimeNestedUnknown = clone(validRuntime);
runtimeNestedUnknown.reportClaim.unknownAuthority = true;
assert.equal(
  validateRuntime(runtimeNestedUnknown),
  false,
  'Runtime nested unknown property must be rejected',
);
const tooManyRules = clone(validRuntime);
tooManyRules.policyContext.candidateRules = Array.from(
  { length: 9 },
  (_, index) => ({
    ...clone(validRuntime.policyContext.candidateRules[0]),
    ruleId: `CSR.SPAM.${String(index + 1).padStart(3, '0')}`,
  }),
);
assert.equal(validateRuntime(tooManyRules), false, 'Nine candidate rules must be rejected');
const tooManyEvidence = clone(validRuntime);
tooManyEvidence.evidence = Array.from(
  { length: 33 },
  (_, index) => ({
    ...clone(validRuntime.evidence[0]),
    evidenceId: `EV-SYNTHETIC-${String(index + 1).padStart(3, '0')}`,
  }),
);
assert.equal(validateRuntime(tooManyEvidence), false, 'Thirty-three evidence items must be rejected');

const providerUnknown = clone(validProvider);
providerUnknown.authority = 'EXECUTE';
assert.equal(validateProvider(providerUnknown), false, 'Provider unknown property must be rejected');
const providerLongRationale = clone(validProvider);
providerLongRationale.findings[0].rationale = 'x'.repeat(4001);
assert.equal(validateProvider(providerLongRationale), false, 'Rationale > 4000 must be rejected');
const providerTooManyFindings = clone(validProvider);
providerTooManyFindings.findings = Array.from(
  { length: 9 },
  (_, index) => ({
    ...clone(validProvider.findings[0]),
    ruleId: `CSR.SPAM.${String(index + 1).padStart(3, '0')}`,
  }),
);
assert.equal(validateProvider(providerTooManyFindings), false, 'Nine findings must be rejected');

const secret = 'test-only-s2-contract-secret';
const nonceDirectory = mkdtempSync(join(tmpdir(), 'cafestory-report-ai-s2-contract-'));
process.on('exit', () => rmSync(nonceDirectory, { recursive: true, force: true }));
const canonicalize = (value) => {
  if (Array.isArray(value)) return `[${value.map(canonicalize).join(',')}]`;
  if (value && typeof value === 'object') {
    return `{${Object.keys(value)
      .sort()
      .map((key) => `${JSON.stringify(key)}:${canonicalize(value[key])}`)
      .join(',')}}`;
  }
  return JSON.stringify(value);
};
const buildNode = workflow.nodes.find(
  (node) => node.name === 'Validate Contract V2 And Build Request',
);
assert.ok(buildNode, 'Canonical workflow build node is missing');
assert.equal(
  buildNode.parameters.jsCode,
  readFileSync(buildCodePath, 'utf8').trim(),
  'Canonical workflow code drifted from reviewable source',
);
const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
const execute = new AsyncFunction('$json', '$env', '$', 'require', buildNode.parameters.jsCode);
const executeSigned = async (request) => {
  const timestamp = String(Math.floor(Date.now() / 1000));
  const nonce = crypto.randomUUID();
  const bodyHash = crypto.createHash('sha256').update(canonicalize(request), 'utf8').digest('hex');
  const signature = crypto
    .createHmac('sha256', secret)
    .update(`${timestamp}\n${nonce}\n${bodyHash}`, 'utf8')
    .digest('hex');
  return execute(
    {
      headers: {
        'x-cafestory-contract-version': request.contractVersion,
        'x-cafestory-correlation-id': request.correlationId,
        'x-cafestory-timestamp': timestamp,
        'x-cafestory-nonce': nonce,
        'x-cafestory-body-sha256': bodyHash,
        'x-cafestory-signature': signature,
      },
      body: request,
    },
    {
      ADMIN_REPORT_AI_HMAC_SECRET: secret,
      ADMIN_REPORT_AI_NONCE_DIR: nonceDirectory,
      OPENAI_DECISION_MODEL: 'gpt-4o-mini',
    },
    undefined,
    require,
  );
};
const builtItems = await executeSigned(validRuntime);
const builtEvidenceById = new Map(
  builtItems[0].json.providerInput.evidence.map((item) => [item.evidenceId, item]),
);
assert.equal(
  builtEvidenceById.get('EV-TARGET-MEDIA').sanitizedPayload.verificationMethod,
  'PLATFORM_URL_METADATA_ONLY',
);
assert.equal(
  builtEvidenceById.get('EV-TARGET-REPORT-HISTORY').sanitizedPayload.sameTargetOpenReportCount,
  2,
);
await assert.rejects(
  () => executeSigned(runtimeNestedUnknown),
  /Report claim contains unknown properties: unknownAuthority/,
  'n8n request boundary must reject nested unknown properties',
);
const embeddedProviderSchema = builtItems[0].json.openaiRequest.text.format.schema;
const providerSchemaForOpenAi = clone(providerSchema);
delete providerSchemaForOpenAi.$schema;
delete providerSchemaForOpenAi.$id;
delete providerSchemaForOpenAi.title;
assert.deepEqual(
  embeddedProviderSchema,
  providerSchemaForOpenAi,
  'Embedded provider schema drifted from canonical schema',
);

const schemaHash = (value) =>
  crypto.createHash('sha256').update(canonicalize(value), 'utf8').digest('hex');
console.log('S2_CONTRACT_SCHEMA_COMPILE=PASS');
console.log('S2_SCHEMA_BOUNDARIES=7/7 PASS');
console.log('S2_N8N_NESTED_BOUNDARY=PASS');
console.log('S2_PROVIDER_SCHEMA_PARITY=PASS');
console.log(`S2_RUNTIME_SCHEMA_SHA256=${schemaHash(runtimeSchema)}`);
console.log(`S2_PROVIDER_SCHEMA_SHA256=${schemaHash(providerSchema)}`);
