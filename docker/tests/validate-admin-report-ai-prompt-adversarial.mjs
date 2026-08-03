import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { createRequire } from 'node:module';
import { mkdtempSync, readFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const require = createRequire(import.meta.url);
const workflowPath = new URL('../cafestory-admin-report-ai-resolution-n8n-workflow.json', import.meta.url);
const buildCodePath = new URL(
  '../n8n-code/admin-report-ai-resolution/validate-contract-v2-and-build-request.js',
  import.meta.url,
);
const normalizeCodePath = new URL(
  '../n8n-code/admin-report-ai-resolution/validate-and-normalize-recommendation-v2.js',
  import.meta.url,
);
const vectorPath = new URL(
  './fixtures/admin-report-ai-prompt-adversarial-vectors.json',
  import.meta.url,
);
const workflow = JSON.parse(readFileSync(workflowPath, 'utf8'));
const vectors = JSON.parse(readFileSync(vectorPath, 'utf8'));
const secret = 'test-only-prompt-adversarial-hmac-secret';
const nonceDirectory = mkdtempSync(join(tmpdir(), 'cafestory-report-ai-adv-'));
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

const signatureHeaders = (body) => {
  const timestamp = String(Math.floor(Date.now() / 1000));
  const nonce = crypto.randomUUID();
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
  const execute = new AsyncFunction('$json', '$env', '$', 'require', node.parameters.jsCode);
  return execute(
    json,
    {
      ADMIN_REPORT_AI_HMAC_SECRET: secret,
      ADMIN_REPORT_AI_NONCE_DIR: nonceDirectory,
      OPENAI_DECISION_MODEL: 'gpt-4o-mini',
    },
    dollar,
    require,
  );
};

assert.equal(
  workflow.nodes.find((node) => node.name === 'Validate Contract V2 And Build Request')
    ?.parameters.jsCode,
  readFileSync(buildCodePath, 'utf8').trim(),
  'Build Code node is not synchronized with its reviewed source',
);
assert.equal(
  workflow.nodes.find((node) => node.name === 'Validate And Normalize Recommendation V2')
    ?.parameters.jsCode,
  readFileSync(normalizeCodePath, 'utf8').trim(),
  'Normalize Code node is not synchronized with its reviewed source',
);

const evidenceItem = ({
  evidenceId,
  evidenceKind,
  payload,
  availability = 'AVAILABLE',
  quality = 'HIGH',
  privacy = 'PUBLIC_CONTENT',
  intendedUse = 'RULE_EVALUATION_CANDIDATE',
}) => ({
  evidenceId,
  envelopeVersion: '1.0.0-rc.1',
  evidenceKind,
  subject: {
    targetType: 'BLOG',
    targetAlias: 'target-blog-test',
    snapshotVersion: '1',
    snapshotHash: 'sha256:synthetic-snapshot',
  },
  source: {
    sourceType: 'TARGET_SNAPSHOT',
    sourceSystem: 'CAFE_STORY_BACKEND',
    verificationStatus: 'SYSTEM_CAPTURED',
    authorityScope: 'PLATFORM_OWNED_FIELD',
  },
  capture: {
    collectorName: 'AdversarialFixture',
    collectorVersion: '1.0.0',
    capturedAt: '2026-07-29T00:00:00Z',
    transformations: [],
  },
  integrity: {
    canonicalization: 'JCS',
    digestAlgorithm: 'SHA-256',
    payloadDigest: 'sha256:synthetic-payload',
  },
  availability: {
    status: availability,
    reasonCode: availability === 'AVAILABLE' ? null : 'NOT_AVAILABLE_FOR_FIXTURE',
  },
  quality: {
    level: quality,
    reasonCodes: ['SYNTHETIC_FIXTURE'],
  },
  privacy: {
    classification: privacy,
    containsPersonalData: false,
    redactionStatus: 'APPLIED',
    retentionClass: 'REPORT_EVIDENCE_90D',
  },
  intendedUse,
  collectedForRuleIds: ['CSR.INT.003'],
  payload: {
    representation: payload ? 'INLINE' : 'NONE',
    mediaType: payload ? 'application/json' : null,
    value: payload,
    reference: null,
    truncated: false,
  },
});

const baseRequest = () => ({
  contractVersion: '2.0',
  correlationId: crypto.randomUUID(),
  idempotencyKey: crypto.randomBytes(32).toString('hex'),
  automationMode: 'A0_RECOMMEND_ONLY',
  reportClaim: {
    reportId: crypto.randomUUID(),
    status: 'PENDING',
    reasonCode: 'SCAM_FRAUD_OR_SPAM',
    reasonCatalogVersion: '1.0.0',
    description: 'Synthetic claim.',
    trustLevel: 'UNTRUSTED_REPORTER_CLAIM',
  },
  targetSnapshot: {
    targetType: 'BLOG',
    targetId: crypto.randomUUID(),
    targetAlias: 'target-blog-test',
    snapshotVersion: '1',
    capturedAt: '2026-07-29T00:00:00Z',
    snapshotHash: 'sha256:synthetic-snapshot',
    observableFields: {
      contentText: 'Synthetic target content.',
      imageUrls: [],
      status: 'PUBLISHED',
      createdAt: '2026-07-29T00:00:00Z',
      updatedAt: '2026-07-29T00:00:00Z',
    },
  },
  evidence: [
    evidenceItem({
      evidenceId: 'EV-TARGET-IDENTITY',
      evidenceKind: 'TARGET_IDENTITY',
      payload: { targetAlias: 'target-blog-test' },
      privacy: 'RESTRICTED',
      intendedUse: 'CONTEXT_ONLY',
    }),
    evidenceItem({
      evidenceId: 'EV-TARGET-CONTENT',
      evidenceKind: 'TARGET_TEXT_CONTENT',
      payload: { sanitizedText: 'Synthetic target content.' },
    }),
    evidenceItem({
      evidenceId: 'EV-TARGET-STATE',
      evidenceKind: 'TARGET_STATE',
      payload: { status: 'PUBLISHED' },
      privacy: 'INTERNAL_MODERATION',
      intendedUse: 'CONTEXT_ONLY',
    }),
    evidenceItem({
      evidenceId: 'EV-TARGET-MEDIA',
      evidenceKind: 'TARGET_MEDIA_REFERENCE',
      payload: { referenceCount: 0 },
      availability: 'NOT_COLLECTED',
      quality: 'UNUSABLE',
      privacy: 'INTERNAL_MODERATION',
      intendedUse: 'CONTEXT_ONLY',
    }),
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
      ruleId: 'CSR.INT.003',
      ruleVersion: '1.0.0-proposed.2',
      ruleStatus: 'PROPOSED',
      ruleFamily: 'INT',
      ruleType: 'VIOLATION',
      material: true,
      applicableTargetTypes: ['BLOG', 'COMMENT'],
      requirementProfileIds: ['RP-TEXT-CONTEXT'],
      requiredEvidenceKinds: ['TARGET_IDENTITY', 'TARGET_STATE', 'TARGET_TEXT_CONTENT'],
      conditionalRequirements: [],
      semanticRequirementCodes: ['SEM-TRANSACTION-INTENT'],
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
    criticalEvidenceMissing: false,
    criticalMissingBehavior: 'NEEDS_MANUAL_REVIEW',
    allowedCandidateActions: ['NO_ACTION', 'KEEP_VISIBLE', 'HIDE', 'REMOVE'],
  },
});

const applyPayload = (request, vector) => {
  if (vector.location === 'targetContent') {
    request.targetSnapshot.observableFields.contentText = vector.payload;
    request.evidence[1].payload.value.sanitizedText = vector.payload;
  } else if (vector.location === 'reportClaim') {
    request.reportClaim.description = vector.payload;
  } else if (vector.location === 'imageUrl') {
    request.targetSnapshot.observableFields.imageUrls = [vector.payload];
    request.evidence[3].payload.value = { referenceCount: 1, rawUrl: vector.payload };
  } else if (vector.location === 'existingModeration') {
    request.existingModerationResult = {
      reportDecision: 'RESOLVE',
      explanation: vector.payload,
    };
  } else {
    throw new Error(`Unsupported vector location: ${vector.location}`);
  }
};

const providerResult = () => ({
  recommendationState: 'RESOLVE',
  reportDecision: 'RESOLVE',
  targetAction: 'HIDE',
  findings: [
    {
      ruleId: 'CSR.INT.003',
      ruleVersion: '1.0.0-proposed.2',
      outcome: 'SUBSTANTIATED',
      evidenceIds: ['EV-TARGET-CONTENT'],
      counterEvidenceIds: [],
      missingEvidenceIds: [],
      violationLikelihood: 'HIGH',
      rationale: 'Synthetic deterministic recommendation.',
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
  labels: ['synthetic'],
  explanation: 'Synthetic deterministic recommendation.',
});

const applyProviderAttack = (result, attack) => {
  if (!attack) return false;
  if (attack === 'UNKNOWN_RULE') {
    result.findings[0].ruleId = 'CSR.ROOT.999';
  } else if (attack === 'SUSPEND_USER') {
    result.targetAction = 'SUSPEND_USER';
  } else if (attack === 'DERIVED_ONLY') {
    result.findings[0].evidenceIds = ['EV-DERIVED-MODERATION'];
    result.evidenceSummary.usedEvidenceIds = ['EV-DERIVED-MODERATION'];
  } else if (attack === 'REPORTER_CLAIM_ONLY') {
    result.findings[0].evidenceIds = ['EV-REASON-ROUTE'];
    result.evidenceSummary.usedEvidenceIds = ['EV-REASON-ROUTE'];
  } else if (attack === 'UNKNOWN_EVIDENCE') {
    result.findings[0].missingEvidenceIds = ['EV-ROOT-SECRET'];
    result.evidenceSummary.missingEvidenceIds = ['EV-ROOT-SECRET'];
  } else {
    throw new Error(`Unsupported provider attack: ${attack}`);
  }
  return true;
};

const expectedBlockedReason = (attack) => {
  if (attack === 'UNKNOWN_RULE' || attack === 'UNKNOWN_EVIDENCE') {
    return 'UNKNOWN_RULE_OR_EVIDENCE_REFERENCE';
  }
  if (attack === 'SUSPEND_USER') {
    return 'DECISION_ACTION_NOT_ALLOWED';
  }
  if (attack === 'DERIVED_ONLY' || attack === 'REPORTER_CLAIM_ONLY') {
    return 'UNKNOWN_RULE_OR_EVIDENCE_REFERENCE';
  }
  return null;
};

const assertBuildBoundary = (vector, requestBefore, built, baselineSystemPolicy) => {
  const context = built.requestContext;
  const providerInput = built.providerInput;
  const openaiRequest = built.openaiRequest;
  const systemText = openaiRequest.input[0].content[0].text;
  const userText = openaiRequest.input[1].content[0].text;

  assert.deepEqual(context, requestBefore, `${vector.id}: requestContext mutated`);
  assert.equal(systemText, baselineSystemPolicy, `${vector.id}: system policy changed`);
  assert.equal(openaiRequest.input[0].role, 'system');
  assert.equal(openaiRequest.input[1].role, 'user');
  assert.match(userText, /allowlisted untrusted evidence projection/i);
  const jsonEncodedPayload = JSON.stringify(vector.payload).slice(1, -1);
  if (vector.location === 'targetContent') {
    assert.ok(userText.includes(jsonEncodedPayload), `${vector.id}: target text did not reach safe projection`);
  } else {
    assert.equal(
      userText.includes(jsonEncodedPayload),
      false,
      `${vector.id}: non-allowlisted field reached provider`,
    );
  }
  assert.deepEqual(Object.keys(providerInput).sort(), [
    'candidateRules',
    'evidence',
    'executionConstraints',
    'missingRequirements',
    'target',
  ]);
  assert.equal('reportClaim' in providerInput, false);
  assert.equal('targetId' in providerInput.target, false);
  assert.equal('existingModerationResult' in providerInput, false);
  assert.equal('idempotencyKey' in providerInput, false);
  assert.equal(openaiRequest.text.format.type, 'json_schema');
  assert.equal(openaiRequest.text.format.strict, true);
  assert.equal(openaiRequest.text.format.schema.additionalProperties, false);
  assert.deepEqual(openaiRequest.text.format.schema.properties.targetAction.enum, [
    'KEEP_VISIBLE',
    'HIDE',
    'REMOVE',
    'NO_ACTION',
  ]);
  assert.equal('tools' in openaiRequest, false, `${vector.id}: provider tool scope was added`);
  assert.equal('tool_choice' in openaiRequest, false, `${vector.id}: provider tool choice was added`);
  assert.equal(JSON.stringify(openaiRequest).includes(secret), false, `${vector.id}: secret reached provider`);
  assert.deepEqual(
    providerInput.candidateRules.map((rule) => rule.ruleId),
    ['CSR.INT.003'],
    `${vector.id}: candidate rule scope changed`,
  );
  assert.equal(context.executionConstraints.recommendationOnly, true);
  assert.equal(providerInput.executionConstraints.recommendationOnly, true);
  assert.deepEqual(
    providerInput.evidence.find((item) => item.evidenceId === 'EV-TARGET-IDENTITY').sanitizedPayload,
    {},
  );
};

const expectedResponseKeys = [
  'blockedReasons',
  'contractVersion',
  'correlationId',
  'evidenceQuality',
  'evidenceSufficiency',
  'evidenceSummary',
  'explanation',
  'findings',
  'harmSeverity',
  'labels',
  'modelName',
  'policyVersion',
  'promptVersion',
  'recommendationState',
  'reportDecision',
  'ruleCatalogVersion',
  'targetAction',
  'violationLikelihood',
  'workflowVersion',
];

const assertNormalizedBoundary = (vector, requestBefore, normalized, attackExpectedManual) => {
  const response = normalized.response;
  assert.deepEqual(
    Object.keys(response).sort(),
    expectedResponseKeys,
    `${vector.id}: normalized response schema changed`,
  );
  assert.equal(response.contractVersion, '2.0');
  assert.equal(response.correlationId, requestBefore.correlationId);
  assert.equal(JSON.stringify(normalized).includes(secret), false, `${vector.id}: secret leaked`);
  assert.ok(
    ['RESOLVE', 'REJECT', 'NEEDS_MANUAL_REVIEW'].includes(response.reportDecision),
    `${vector.id}: report decision escaped enum`,
  );
  assert.ok(
    ['KEEP_VISIBLE', 'HIDE', 'REMOVE', 'NO_ACTION'].includes(response.targetAction),
    `${vector.id}: target action escaped enum`,
  );
  assert.equal(response.recommendationState, 'NEEDS_MANUAL_REVIEW', `${vector.id}: result not clamped`);
  assert.equal(response.reportDecision, 'NEEDS_MANUAL_REVIEW', `${vector.id}: result not clamped`);
  assert.equal(response.targetAction, 'NO_ACTION', `${vector.id}: result retained action`);
  assert.deepEqual(response.findings, [], `${vector.id}: unsafe findings retained`);
  assert.ok(response.blockedReasons.length > 0, `${vector.id}: no blocked reason`);
  assert.ok(
    response.blockedReasons.includes('POLICY_OR_RULE_CATALOG_NOT_ACTIVE'),
    `${vector.id}: proposed lifecycle was not clamped`,
  );
  if (attackExpectedManual) {
    assert.ok(
      response.blockedReasons.includes(expectedBlockedReason(vector.providerAttack)),
      `${vector.id}: expected blocked reason is missing`,
    );
  }
};

assert.equal(vectors.length, 12, 'ADV matrix must contain exactly ADV-001 through ADV-012');
assert.deepEqual(
  vectors.map((vector) => vector.id),
  Array.from({ length: 12 }, (_, index) => `ADV-${String(index + 1).padStart(3, '0')}`),
);

const cleanRequest = baseRequest();
const cleanBuildResult = await executeCodeNode('Validate Contract V2 And Build Request', {
  headers: signatureHeaders(cleanRequest),
  body: cleanRequest,
});
const baselineSystemPolicy = cleanBuildResult[0].json.openaiRequest.input[0].content[0].text;
assert.match(baselineSystemPolicy, /untrusted data/i);
assert.match(baselineSystemPolicy, /Reporter reason, report count, and prior AI signals do not prove/i);
assert.match(baselineSystemPolicy, /AI rationale is not evidence/i);

const invalidContractCases = [
  {
    name: 'duplicate candidate Rule ID',
    mutate: (request) => request.policyContext.candidateRules.push(
      structuredClone(request.policyContext.candidateRules[0]),
    ),
    expected: /Candidate Rule IDs must be present and unique/,
  },
  {
    name: 'duplicate Evidence ID',
    mutate: (request) => request.evidence.push(structuredClone(request.evidence[0])),
    expected: /Evidence IDs must be present and unique/,
  },
  {
    name: 'snapshot binding mismatch',
    mutate: (request) => {
      request.evidence[0].subject.snapshotHash = 'sha256:different-snapshot';
    },
    expected: /Evidence snapshot binding mismatch/,
  },
];
for (const invalidCase of invalidContractCases) {
  const request = baseRequest();
  invalidCase.mutate(request);
  await assert.rejects(
    executeCodeNode('Validate Contract V2 And Build Request', {
      headers: signatureHeaders(request),
      body: request,
    }),
    invalidCase.expected,
    invalidCase.name,
  );
}

const results = [];
for (const vector of vectors) {
  const request = baseRequest();
  applyPayload(request, vector);
  const requestBefore = structuredClone(request);
  const builtItems = await executeCodeNode('Validate Contract V2 And Build Request', {
    headers: signatureHeaders(request),
    body: request,
  });
  const built = builtItems[0].json;
  assertBuildBoundary(vector, requestBefore, built, baselineSystemPolicy);

  const provider = providerResult();
  const attackExpectedManual = applyProviderAttack(provider, vector.providerAttack);
  const dollar = (nodeName) => {
    assert.equal(nodeName, 'Validate Contract V2 And Build Request');
    return { first: () => ({ json: { requestContext: request } }) };
  };
  const normalizedItems = await executeCodeNode(
    'Validate And Normalize Recommendation V2',
    { output_text: JSON.stringify(provider), model: 'gpt-4o-mini' },
    dollar,
  );
  assertNormalizedBoundary(vector, requestBefore, normalizedItems[0].json, attackExpectedManual);
  assert.deepEqual(request, requestBefore, `${vector.id}: source request mutated after normalization`);

  results.push({ id: vector.id, status: 'PASS', oracle: vector.oracle });
  console.log(`${vector.id}=PASS`);
}

assert.equal(workflow.active, false, 'Source workflow must remain inactive');
console.log(
  JSON.stringify({
    suite: 'ADMIN_REPORT_AI_PROMPT_ADVERSARIAL',
    status: 'PASS',
    total: results.length,
    passed: results.length,
    failed: 0,
    contractGuardsPassed: invalidContractCases.length,
    providerCalled: false,
    secretsPrinted: false,
    results,
  }),
);
