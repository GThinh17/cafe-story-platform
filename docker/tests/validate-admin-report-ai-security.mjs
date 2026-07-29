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

const requestBody = {
  contractVersion: '2.0',
  correlationId,
  idempotencyKey: 'a'.repeat(64),
  automationMode: 'A0_RECOMMEND_ONLY',
  reportClaim: { reportId: crypto.randomUUID() },
  targetSnapshot: { targetType: 'BLOG', targetId: crypto.randomUUID() },
  evidence: [{ evidenceId: 'EV-TARGET-CONTENT' }],
  policyContext: {
    policyVersion: 'PF-test',
    ruleCatalogVersion: 'RC-test',
    candidateRules: [{ ruleId: 'CSR.SPAM.001' }],
  },
  executionConstraints: {
    recommendationOnly: true,
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
      ruleVersion: '1.0.0',
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
