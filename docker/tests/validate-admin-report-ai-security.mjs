import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { createRequire } from 'node:module';
import { existsSync, mkdtempSync, readFileSync, rmSync, utimesSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const require = createRequire(import.meta.url);
const workflowPath = new URL('../cafestory-admin-report-ai-resolution-n8n-workflow.json', import.meta.url);
const composePath = new URL('../docker-compose.n8n.yml', import.meta.url);
const workflow = JSON.parse(readFileSync(workflowPath, 'utf8'));
const compose = readFileSync(composePath, 'utf8');
const secret = 'test-only-report-ai-hmac-secret';
const correlationId = crypto.randomUUID();
const staticData = {};
const nonceDirectory = mkdtempSync(join(tmpdir(), 'cafestory-report-ai-nonce-'));
const hash = `sha256:${'a'.repeat(64)}`;
process.on('exit', () => rmSync(nonceDirectory, { recursive: true, force: true }));

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

const signatureHeaders = (body, timestamp, nonce) => {
  const bodyHash = crypto.createHash('sha256').update(canonicalize(body), 'utf8').digest('hex');
  const signature = crypto
    .createHmac('sha256', secret)
    .update(`${timestamp}\n${nonce}\n${bodyHash}`, 'utf8')
    .digest('hex');
  return {
    'x-cafestory-contract-version': body.contractVersion,
    'x-cafestory-correlation-id': body.correlationId,
    'x-cafestory-timestamp': timestamp,
    'x-cafestory-nonce': nonce,
    'x-cafestory-body-sha256': bodyHash,
    'x-cafestory-signature': signature,
  };
};

const executeCodeNode = async (nodeName, json, dollar = undefined) => {
  const node = workflow.nodes.find((candidate) => candidate.name === nodeName);
  assert.ok(node, `Missing workflow node: ${nodeName}`);
  const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
  const execute = new AsyncFunction(
    '$json',
    '$env',
    '$getWorkflowStaticData',
    '$',
    'require',
    node.parameters.jsCode,
  );
  return execute(
    json,
    {
      ADMIN_REPORT_AI_HMAC_SECRET: secret,
      ADMIN_REPORT_AI_NONCE_DIR: nonceDirectory,
      OPENAI_DECISION_MODEL: 'gpt-4o-mini',
    },
    () => staticData,
    dollar,
    require,
  );
};

const evidenceItem = (evidenceId, evidenceKind, value, intendedUse = 'CONTEXT_ONLY') => ({
  evidenceId,
  envelopeVersion: '1.0.0-rc.1',
  evidenceKind,
  subject: {
    targetType: 'BLOG',
    targetAlias: 'target-blog-security-test',
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
    collectorVersion: 's2-security-test',
    capturedAt: '2026-07-30T09:00:00Z',
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
    reasonCodes: ['SYNTHETIC_SECURITY_TEST'],
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

const reportId = crypto.randomUUID();
const targetId = crypto.randomUUID();
const requestBody = {
  contractVersion: '2.0',
  correlationId,
  idempotencyKey: 'a'.repeat(64),
  requestedAt: '2026-07-30T09:00:00Z',
  automationMode: 'A0_RECOMMEND_ONLY',
  reportId,
  targetType: 'BLOG',
  targetId,
  reasonCode: 'SPAM',
  reportClaim: {
    reportId,
    status: 'OPEN',
    reasonCode: 'SPAM',
    reasonCatalogVersion: 'IRC-2.0.0-proposed.1',
    description: 'Synthetic security test claim.',
    trustLevel: 'UNTRUSTED_REPORTER_CLAIM',
  },
  targetSnapshot: {
    targetType: 'BLOG',
    targetId,
    targetAlias: 'target-blog-security-test',
    snapshotVersion: '1',
    capturedAt: '2026-07-30T09:00:00Z',
    observableFields: {
      contentText: 'Synthetic security test content.',
      imageUrls: [],
      status: 'ACTIVE',
      createdAt: '2026-07-30T08:00:00',
      updatedAt: '2026-07-30T08:30:00',
    },
    snapshotHash: hash,
  },
  evidence: [
    evidenceItem(
      'EV-TARGET-IDENTITY',
      'TARGET_IDENTITY',
      { targetType: 'BLOG', targetAlias: 'target-blog-security-test' },
    ),
    evidenceItem('EV-TARGET-STATE', 'TARGET_STATE', { status: 'ACTIVE' }),
    evidenceItem(
      'EV-TARGET-CONTENT',
      'TARGET_TEXT_CONTENT',
      { sanitizedText: 'Synthetic security test content.' },
      'RULE_EVALUATION_CANDIDATE',
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
      evaluationCeiling: 'NEEDS_MANUAL_REVIEW',
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
    availableEvidenceKinds: ['TARGET_IDENTITY', 'TARGET_STATE', 'TARGET_TEXT_CONTENT'],
    missingRequirements: [],
    currentEvaluationCeiling: 'NEEDS_MANUAL_REVIEW',
  },
  executionConstraints: {
    recommendationOnly: true,
    allowedCandidateActions: ['NO_ACTION', 'KEEP_VISIBLE', 'HIDE', 'REMOVE'],
    criticalMissingBehavior: 'NEEDS_MANUAL_REVIEW',
    criticalEvidenceMissing: false,
  },
  existingModerationResult: {
    wholeDouble: 1.0,
    fraction: 0.000001,
    large: 1.0e30,
  },
};

assert.equal(
  canonicalize(requestBody.existingModerationResult),
  '{"fraction":0.000001,"large":1e+30,"wholeDouble":1}',
);

const timestamp = String(Math.floor(Date.now() / 1000));
const nonce = crypto.randomUUID();
const validHeaders = signatureHeaders(requestBody, timestamp, nonce);
const validated = await executeCodeNode('Validate Contract V2 And Build Request', {
  headers: validHeaders,
  body: requestBody,
});
assert.equal(validated[0].json.requestContext.correlationId, correlationId);
assert.equal(validated[0].json.openaiRequest.text.format.strict, true);

await assert.rejects(
  executeCodeNode('Validate Contract V2 And Build Request', {
    headers: validHeaders,
    body: requestBody,
  }),
  /nonce replay detected/,
);

const nonceFilePath = join(
  nonceDirectory,
  `${crypto.createHash('sha256').update(nonce, 'utf8').digest('hex')}.nonce`,
);
assert.equal(existsSync(nonceFilePath), true);
const expiredNonceTime = new Date(Date.now() - 301_000);
utimesSync(nonceFilePath, expiredNonceTime, expiredNonceTime);
const acceptedAfterExpiry = await executeCodeNode('Validate Contract V2 And Build Request', {
  headers: validHeaders,
  body: requestBody,
});
assert.equal(acceptedAfterExpiry[0].json.requestContext.correlationId, correlationId);

const concurrentNonce = crypto.randomUUID();
const concurrentHeaders = signatureHeaders(requestBody, timestamp, concurrentNonce);
const concurrentResults = await Promise.allSettled([
  executeCodeNode('Validate Contract V2 And Build Request', {
    headers: concurrentHeaders,
    body: requestBody,
  }),
  executeCodeNode('Validate Contract V2 And Build Request', {
    headers: concurrentHeaders,
    body: requestBody,
  }),
]);
assert.equal(concurrentResults.filter((result) => result.status === 'fulfilled').length, 1);
assert.equal(concurrentResults.filter((result) => result.status === 'rejected').length, 1);
assert.match(
  String(concurrentResults.find((result) => result.status === 'rejected').reason),
  /nonce replay detected/,
);

const staleTimestamp = String(Math.floor(Date.now() / 1000) - 121);
await assert.rejects(
  executeCodeNode('Validate Contract V2 And Build Request', {
    headers: signatureHeaders(requestBody, staleTimestamp, crypto.randomUUID()),
    body: requestBody,
  }),
  /timestamp outside allowed window/,
);

const tamperedBody = { ...requestBody, automationMode: 'A1_FORBIDDEN' };
await assert.rejects(
  executeCodeNode('Validate Contract V2 And Build Request', {
    headers: signatureHeaders(requestBody, timestamp, crypto.randomUUID()),
    body: tamperedBody,
  }),
  /body hash/,
);

const invalidSignatureHeaders = signatureHeaders(requestBody, timestamp, crypto.randomUUID());
invalidSignatureHeaders['x-cafestory-signature'] = '0'.repeat(64);
await assert.rejects(
  executeCodeNode('Validate Contract V2 And Build Request', {
    headers: invalidSignatureHeaders,
    body: requestBody,
  }),
  /signature/,
);

const providerResult = {
  recommendationState: 'RESOLVE',
  reportDecision: 'RESOLVE',
  targetAction: 'HIDE',
  findings: [
    {
      ruleId: 'CSR.SPAM.001',
      ruleVersion: '1.0.0-proposed.2',
      outcome: 'SUPPORTED',
      evidenceIds: ['EV-TARGET-CONTENT'],
      counterEvidenceIds: [],
      missingEvidenceIds: [],
      violationLikelihood: 'HIGH',
      rationale: 'Derived explanation, not evidence.',
    },
  ],
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
  labels: ['spam'],
  explanation: 'Recommendation rationale, not evidence.',
};
const dollar = (nodeName) => {
  assert.equal(nodeName, 'Validate Contract V2 And Build Request');
  return { first: () => ({ json: { requestContext: requestBody } }) };
};
const normalized = await executeCodeNode(
  'Validate And Normalize Recommendation V2',
  { output_text: JSON.stringify(providerResult), model: 'gpt-4o-mini' },
  dollar,
);
const signedOutput = normalized[0].json;
assert.equal(signedOutput.response.correlationId, correlationId);
assert.match(signedOutput.responseHeaders.nonce, /^[0-9a-f-]{36}$/);
assert.equal(
  signedOutput.responseHeaders.bodyHash,
  crypto.createHash('sha256').update(canonicalize(signedOutput.response), 'utf8').digest('hex'),
);
assert.equal(
  signedOutput.responseHeaders.signature,
  crypto
    .createHmac('sha256', secret)
    .update(
      `${signedOutput.responseHeaders.timestamp}\n${signedOutput.responseHeaders.nonce}\n${signedOutput.responseHeaders.bodyHash}`,
      'utf8',
    )
    .digest('hex'),
);

const responseNode = workflow.nodes.find((node) => node.name === 'Respond To Backend');
assert.deepEqual(responseNode.parameters.responseBody, '={{ $json.response }}');
assert.equal(responseNode.parameters.options.responseHeaders.entries.length, 6);
assert.equal(workflow.active, false);
assert.match(compose, /NODE_FUNCTION_ALLOW_BUILTIN:\s*"crypto,fs"/);
assert.match(compose, /ADMIN_REPORT_AI_NONCE_DIR:\s*"\/home\/node\/\.n8n\/report-ai-nonces"/);
assert.doesNotMatch(JSON.stringify(workflow), new RegExp(secret));

console.log('ADMIN_REPORT_AI_N8N_SECURITY=PASS');
console.log('VALID_REQUEST=PASS');
console.log('REPLAY_REJECTED=PASS');
console.log('EXPIRED_NONCE_REACCEPTED=PASS');
console.log('ATOMIC_REPLAY_CLAIM=PASS');
console.log('STALE_TIMESTAMP_REJECTED=PASS');
console.log('TAMPERED_BODY_REJECTED=PASS');
console.log('INVALID_SIGNATURE_REJECTED=PASS');
console.log('SIGNED_RESPONSE=PASS');
console.log('WORKFLOW_ACTIVE=false');
