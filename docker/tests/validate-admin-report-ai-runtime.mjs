import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { readFileSync } from 'node:fs';

const endpoint =
  process.env.ADMIN_REPORT_AI_WEBHOOK_URL ||
  'http://127.0.0.1:5678/webhook/cafestory-admin-report-ai-resolution';
const envPath = new URL('../.env', import.meta.url);

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
    'content-type': 'application/json',
    'x-cafestory-contract-version': body.contractVersion,
    'x-cafestory-correlation-id': body.correlationId,
    'x-cafestory-timestamp': timestamp,
    'x-cafestory-nonce': nonce,
    'x-cafestory-body-sha256': bodyHash,
    'x-cafestory-signature': signature,
  };
};

const createRequestBody = () => ({
  contractVersion: '2.0',
  correlationId: crypto.randomUUID(),
  idempotencyKey: crypto.randomBytes(32).toString('hex'),
  automationMode: 'A0_RECOMMEND_ONLY',
  reportClaim: {
    reportId: crypto.randomUUID(),
    reporterReasonCode: 'SPAM',
    claimText: 'Synthetic runtime verification claim for repeated promotional content.',
  },
  targetSnapshot: {
    targetType: 'BLOG',
    targetId: crypto.randomUUID(),
    content: 'Synthetic CafeStory runtime verification content. No real user data.',
  },
  evidence: [
    {
      evidenceId: 'EV-TARGET-CONTENT',
      evidenceKind: 'TARGET_CONTENT',
      sourceType: 'SYSTEM_SNAPSHOT',
      payload: {
        text: 'Synthetic CafeStory runtime verification content. No real user data.',
      },
    },
  ],
  policyContext: {
    policyVersion: 'PF-runtime-test',
    ruleCatalogVersion: 'RC-runtime-test',
    candidateRules: [{ ruleId: 'CSR.SPAM.001', ruleVersion: '1.0.0' }],
  },
  executionConstraints: {
    recommendationOnly: true,
    criticalEvidenceMissing: false,
  },
});

const send = async (body, headers) => {
  const response = await fetch(endpoint, {
    method: 'POST',
    headers,
    body: JSON.stringify(body),
    signal: AbortSignal.timeout(90_000),
  });
  return response;
};

const expectRejected = async (name, body, headers) => {
  const response = await send(body, headers);
  const responseText = await response.text();
  let responseJson = {};
  try {
    responseJson = JSON.parse(responseText);
  } catch {
    // n8n 2.28.6 currently returns HTTP 200 with an empty body when a Code node rejects.
  }
  const hasSignedResponse =
    Boolean(response.headers.get('x-cafestory-signature')) &&
    responseJson.contractVersion === '2.0' &&
    typeof responseJson.correlationId === 'string';
  assert.equal(hasSignedResponse, false, `${name} unexpectedly returned a signed Contract V2 response`);
  return {
    transportStatus: response.status,
    emptyBody: responseText.length === 0,
    signedResponseAbsent: true,
  };
};

const body = createRequestBody();
const timestamp = String(Math.floor(Date.now() / 1000));
const nonce = crypto.randomUUID();
const headers = signatureHeaders(body, timestamp, nonce);

const validResponse = await send(body, headers);
assert.equal(validResponse.status, 200, 'Valid signed request must return 200');
const responseBody = await validResponse.json();
assert.equal(responseBody.contractVersion, '2.0');
assert.equal(responseBody.correlationId, body.correlationId);
assert.ok(['RESOLVE', 'REJECT', 'NEEDS_MANUAL_REVIEW'].includes(responseBody.reportDecision));
assert.ok(['KEEP_VISIBLE', 'HIDE', 'REMOVE', 'NO_ACTION'].includes(responseBody.targetAction));
assert.equal('confidenceScore' in responseBody, false);
assert.equal('riskScore' in responseBody, false);

const responseTimestamp = validResponse.headers.get('x-cafestory-timestamp');
const responseNonce = validResponse.headers.get('x-cafestory-nonce');
const responseBodyHash = validResponse.headers.get('x-cafestory-body-sha256');
const responseSignature = validResponse.headers.get('x-cafestory-signature');
assert.equal(validResponse.headers.get('x-cafestory-contract-version'), '2.0');
assert.equal(validResponse.headers.get('x-cafestory-correlation-id'), body.correlationId);
assert.ok(responseTimestamp && responseNonce && responseBodyHash && responseSignature);

const expectedResponseHash = crypto
  .createHash('sha256')
  .update(canonicalize(responseBody), 'utf8')
  .digest('hex');
assert.equal(responseBodyHash, expectedResponseHash);
const expectedResponseSignature = crypto
  .createHmac('sha256', secret)
  .update(`${responseTimestamp}\n${responseNonce}\n${responseBodyHash}`, 'utf8')
  .digest('hex');
assert.equal(responseSignature, expectedResponseSignature);

const replayStatus = await expectRejected('replay', body, headers);

const staleBody = createRequestBody();
const staleTimestamp = String(Math.floor(Date.now() / 1000) - 121);
const staleStatus = await expectRejected(
  'stale timestamp',
  staleBody,
  signatureHeaders(staleBody, staleTimestamp, crypto.randomUUID()),
);

const futureBody = createRequestBody();
const futureTimestamp = String(Math.floor(Date.now() / 1000) + 121);
const futureStatus = await expectRejected(
  'future timestamp',
  futureBody,
  signatureHeaders(futureBody, futureTimestamp, crypto.randomUUID()),
);

const invalidSignatureBody = createRequestBody();
const invalidSignatureHeaders = signatureHeaders(
  invalidSignatureBody,
  String(Math.floor(Date.now() / 1000)),
  crypto.randomUUID(),
);
invalidSignatureHeaders['x-cafestory-signature'] = '0'.repeat(64);
const invalidSignatureStatus = await expectRejected(
  'invalid signature',
  invalidSignatureBody,
  invalidSignatureHeaders,
);

const signedBeforeTamper = createRequestBody();
const tamperedHeaders = signatureHeaders(
  signedBeforeTamper,
  String(Math.floor(Date.now() / 1000)),
  crypto.randomUUID(),
);
const tamperedBody = { ...signedBeforeTamper, automationMode: 'A1_FORBIDDEN' };
const tamperedStatus = await expectRejected('tampered body', tamperedBody, tamperedHeaders);

const missingHeadersBody = createRequestBody();
const missingHeadersStatus = await expectRejected('missing headers', missingHeadersBody, {
  'content-type': 'application/json',
});

console.log(
  JSON.stringify({
    status: 'PASS',
    validRequestStatus: validResponse.status,
    providerResponseReceived: true,
    responseSignatureVerified: true,
    reportDecision: responseBody.reportDecision,
    targetAction: responseBody.targetAction,
    recommendationState: responseBody.recommendationState,
    modelNameConfigured: Boolean(responseBody.modelName),
    scoreFieldsAbsent: true,
    replayRejected: replayStatus,
    staleRejected: staleStatus,
    futureRejected: futureStatus,
    invalidSignatureRejected: invalidSignatureStatus,
    tamperedBodyRejected: tamperedStatus,
    missingHeadersRejected: missingHeadersStatus,
    secretValuesPrinted: false,
  }),
);
