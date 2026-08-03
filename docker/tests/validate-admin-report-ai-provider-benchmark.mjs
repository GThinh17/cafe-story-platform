import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import {createRequire} from 'node:module';
import {readFileSync} from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

import {
  buildDatasetProjection,
  buildDirectProjection,
  buildProviderRequest,
  buildRequestContext,
  buildStructuredOutputSchema,
  calculateCostUsd,
  hardSafetyEvaluation,
  normalizeUsage,
  outputText,
  parseDotEnvValue,
  projectedMaximumCostUsd,
  providerStopDecision,
  referenceBoundary,
  safeProviderError,
  sha256Text,
  summarizeRuns,
} from './lib/admin-report-ai-provider-benchmark.mjs';

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url));
const repositoryRoot = path.resolve(scriptDirectory, '../..');
const require = createRequire(import.meta.url);
const Ajv2020 = require(
  path.join(repositoryRoot, '2-cafe-story-nextjs-web/node_modules/ajv/dist/2020'),
).default;
const addFormats = require(
  path.join(repositoryRoot, '2-cafe-story-nextjs-web/node_modules/ajv-formats'),
).default;

const paths = {
  configSchema: 'docker/contracts/admin-report-ai-provider-benchmark-s2.schema.json',
  config: 'docker/contracts/admin-report-ai-provider-benchmark-s2.json',
  outputSchema:
    'docker/contracts/admin-report-ai-provider-evaluation-output-s2.schema.json',
  promptSchema: 'docker/contracts/admin-report-ai-prompt-pilot-s2.schema.json',
  promptSpec: 'docker/contracts/admin-report-ai-prompt-pilot-s2.json',
  assembler:
    'docker/n8n-code/admin-report-ai-resolution/assemble-prompt-pilot-s2-04.js',
  directFixtures: 'docker/tests/fixtures/admin-report-ai-prompt-pilot-v1/cases.json',
  promptManifest: 'docker/tests/fixtures/admin-report-ai-prompt-pilot-v1/manifest.json',
  dataset: 'docker/tests/fixtures/admin-report-ai-evaluation-v1/dataset.json',
  rubric: 'docker/tests/fixtures/admin-report-ai-evaluation-v1/rubric.json',
  datasetManifest: 'docker/tests/fixtures/admin-report-ai-evaluation-v1/manifest.json',
  benchmarkManifest:
    'docker/tests/fixtures/admin-report-ai-provider-benchmark-v1/manifest.json',
};
const absolute = (relativePath) => path.join(repositoryRoot, relativePath);
const text = (relativePath) => readFileSync(absolute(relativePath), 'utf8');
const json = (relativePath) => JSON.parse(text(relativePath));
const sha256 = (relativePath) =>
  crypto.createHash('sha256').update(readFileSync(absolute(relativePath))).digest('hex');

const configSchema = json(paths.configSchema);
const config = json(paths.config);
const outputSchema = json(paths.outputSchema);
const promptSpec = json(paths.promptSpec);
const directFixtures = json(paths.directFixtures);
const dataset = json(paths.dataset);
const manifest = json(paths.benchmarkManifest);
const assemblerSource = text(paths.assembler);

const ajv = new Ajv2020({allErrors: true, strict: true});
addFormats(ajv);
const validateConfig = ajv.compile(configSchema);
const validateOutput = ajv.compile(outputSchema);
assert.equal(validateConfig(config), true, JSON.stringify(validateConfig.errors));

for (const mutate of [
  (candidate) => {
    candidate.runtimeAuthority = true;
  },
  (candidate) => {
    candidate.providerCallAuthorizedBy = 'APPROVE_S2_05';
  },
  (candidate) => {
    candidate.requestPolicy.store = true;
  },
  (candidate) => {
    candidate.requestPolicy.maxProviderCalls = 37;
  },
  (candidate) => {
    candidate.scoringPolicy.providerQualityDenominator = 1;
  },
  (candidate) => {
    candidate.scoringPolicy.modelSelectionAuthority = true;
  },
  (candidate) => {
    candidate.modelCandidates[0].model = 'gpt-unpinned';
  },
  (candidate) => {
    candidate.extraAuthority = true;
  },
]) {
  const candidate = structuredClone(config);
  mutate(candidate);
  assert.equal(validateConfig(candidate), false, 'unsafe config mutation passed');
}

const validFinding = {
  ruleId: 'CSR.HAR.001',
  ruleVersion: '1.0.0-proposed.2',
  outcome: 'UNASSESSABLE',
  evidenceIds: ['EV-TARGET-TEXT'],
  counterEvidenceIds: [],
  missingEvidenceIds: ['MR-PARENT-BLOG-CONTEXT'],
  violationLikelihood: 'UNKNOWN',
  rationale: 'Synthetic bounded rationale.',
};
assert.equal(validateOutput(validFinding), true, JSON.stringify(validateOutput.errors));
for (const mutate of [
  (candidate) => {
    candidate.targetAction = 'REMOVE';
  },
  (candidate) => {
    candidate.ruleId = 'CSR.HAR.999';
  },
  (candidate) => {
    candidate.violationLikelihood = 0.92;
  },
  (candidate) => {
    candidate.evidenceIds = ['UNKNOWN EVIDENCE'];
  },
  (candidate) => {
    candidate.rationale = '';
  },
]) {
  const candidate = structuredClone(validFinding);
  mutate(candidate);
  assert.equal(validateOutput(candidate), false, 'unsafe output mutation passed');
}

assert.equal(
  parseDotEnvValue('OPENAI_API_KEY=plain\nOTHER=x', 'OPENAI_API_KEY'),
  'plain',
);
assert.equal(parseDotEnvValue(' OPENAI_API_KEY="quoted" ', 'OPENAI_API_KEY'), 'quoted');
assert.equal(parseDotEnvValue("OPENAI_API_KEY='single'", 'OPENAI_API_KEY'), 'single');
assert.equal(parseDotEnvValue('OTHER=value', 'OPENAI_API_KEY'), '');
assert.equal(outputText({output_text: ' direct '}), 'direct');
assert.equal(
  outputText({output: [{content: [{text: ' nested '}]}]}),
  'nested',
);
assert.equal(outputText({output: [{content: [{}]}]}), '');
assert.deepEqual(normalizeUsage(), {
  inputTokens: 0,
  cachedInputTokens: 0,
  cacheWriteTokens: 0,
  outputTokens: 0,
  totalTokens: 0,
});
const usage = normalizeUsage({
  input_tokens: 1000,
  input_tokens_details: {cached_tokens: 200, cache_write_tokens: 100},
  output_tokens: 50,
  total_tokens: 1050,
});
assert.deepEqual(usage, {
  inputTokens: 1000,
  cachedInputTokens: 200,
  cacheWriteTokens: 100,
  outputTokens: 50,
  totalTokens: 1050,
});
assert.equal(
  calculateCostUsd(usage, {
    inputPerMillionUsd: 5,
    cachedInputPerMillionUsd: 0.5,
    cacheWritePerMillionUsd: 6.25,
    outputPerMillionUsd: 30,
  }),
  0.005725,
);
assert.equal(
  calculateCostUsd(
    {...usage, cachedInputTokens: 900, cacheWriteTokens: 900},
    {
      inputPerMillionUsd: 1,
      cachedInputPerMillionUsd: 0.1,
      cacheWritePerMillionUsd: 1.25,
      outputPerMillionUsd: 6,
    },
  ),
  0.001515,
);
assert.equal(projectedMaximumCostUsd(config), 0.918);

const records = new Map(dataset.records.map((record) => [record.caseId, record]));
const bundles = new Map(dataset.evidenceBundles.map((bundle) => [bundle.bundleId, bundle]));
const projections = config.datasetCaseRefs.map((caseId) => {
  const record = records.get(caseId);
  assert.ok(record, `${caseId} is missing`);
  assert.equal(record.ruleId, 'CSR.HAR.001');
  const bundle = bundles.get(record.evidenceBundleId);
  assert.ok(bundle, `${record.evidenceBundleId} is missing`);
  return {record, projection: buildDatasetProjection(record, bundle)};
});
assert.deepEqual(
  projections.slice(0, 3).map(({record}) => record.caseClass),
  ['HARD_SAFETY', 'HARD_SAFETY', 'HARD_SAFETY'],
);
assert.deepEqual(
  projections.slice(3).map(({record}) => record.caseClass),
  ['SEMANTIC_CANDIDATE', 'DISAGREEMENT'],
);
assert.ok(
  projections.some(({projection}) =>
    projection.evidence.some(
      (item) => item.evidenceKind === 'PARENT_BLOG_CONTEXT',
    ),
  ),
);
assert.ok(
  projections.some(({projection}) =>
    projection.evidence.some(
      (item) => item.evidenceKind === 'TARGET_STATE',
    ),
  ),
);

const directFixture = directFixtures.cases.find(
  (fixture) => fixture.caseId === config.directFixtureCaseRefs[0],
);
assert.ok(directFixture);
const directProjection = buildDirectProjection(directFixture);
assert.equal(directProjection.caseClass, 'CONTRACT_STABILITY_ONLY');
assert.equal(directProjection.targetType, 'BLOG');

const inactiveContext = buildRequestContext(
  directFixture.requestContext,
  projections[0].projection,
);
assert.equal(inactiveContext.policyContext.policyStatus, 'DRAFT');
assert.equal(inactiveContext.policyContext.ruleCatalogStatus, 'DRAFT');
assert.equal(inactiveContext.priorAiOutput, null);
const proposedContext = buildRequestContext(
  directFixture.requestContext,
  directProjection,
);
assert.equal(proposedContext.policyContext.policyStatus, 'PROPOSED');

const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
const executeAssembler = new AsyncFunction(
  '$json',
  `${assemblerSource}\n//# sourceURL=assemble-prompt-pilot-s2-04.js`,
);
const prompts = [];
for (const projection of [
  ...projections.map((item) => item.projection),
  directProjection,
]) {
  const context = buildRequestContext(directFixture.requestContext, projection);
  const first = (
    await executeAssembler({promptPilotSpec: promptSpec, requestContext: context})
  )[0].json;
  const second = (
    await executeAssembler({
      promptPilotSpec: promptSpec,
      requestContext: structuredClone(context),
    })
  )[0].json;
  assert.equal(first.systemPrompt, second.systemPrompt);
  assert.equal(first.candidateVersion, config.promptCandidateVersion);
  assert.equal(first.runtimeAuthority, false);
  assert.deepEqual(first.selectedRuleIds, ['CSR.HAR.001']);
  assert.ok(!first.systemPrompt.includes(projection.untrustedReportClaim));
  assert.ok(!first.systemPrompt.includes(projection.targetSnapshot.contentText));
  prompts.push(first.systemPrompt);
}

const structuredOutputSchema = buildStructuredOutputSchema(outputSchema);
const collectKeys = (value, keys = []) => {
  if (Array.isArray(value)) {
    value.forEach((item) => collectKeys(item, keys));
  } else if (value && typeof value === 'object') {
    for (const [key, nested] of Object.entries(value)) {
      keys.push(key);
      collectKeys(nested, keys);
    }
  }
  return keys;
};
const structuredOutputKeys = collectKeys(structuredOutputSchema);
for (const unsupported of [
  '$schema',
  '$id',
  'title',
  'const',
  'minLength',
  'maxLength',
  'uniqueItems',
]) {
  assert.ok(
    !structuredOutputKeys.includes(unsupported),
    `Provider schema contains unsupported ${unsupported}`,
  );
}
assert.deepEqual(structuredOutputSchema.properties.ruleId.enum, ['CSR.HAR.001']);
assert.deepEqual(structuredOutputSchema.properties.ruleVersion.enum, [
  '1.0.0-proposed.2',
]);
assert.equal(structuredOutputSchema.additionalProperties, false);
assert.deepEqual(
  [...structuredOutputSchema.required].sort(),
  Object.keys(structuredOutputSchema.properties).sort(),
);

const providerRequest = buildProviderRequest({
  model: config.modelCandidates[0].model,
  systemPrompt: prompts[0],
  projection: projections[0].projection,
  outputSchema: structuredOutputSchema,
  requestPolicy: config.requestPolicy,
});
assert.equal(providerRequest.store, false);
assert.equal(providerRequest.reasoning.effort, 'low');
assert.equal(providerRequest.text.format.strict, true);
assert.equal(providerRequest.text.format.schema, structuredOutputSchema);
assert.ok(providerRequest.input[1].content[0].text.includes('untrusted'));
assert.equal(providerRequest.text.format.schema.properties.targetAction, undefined);
assert.ok(providerRequest.input[0].content[0].text.includes('Do not return targetAction'));

const referenceCase = projections[2].projection;
const referenceFinding = {
  ...validFinding,
  evidenceIds: [referenceCase.evidence[0].evidenceId],
  missingEvidenceIds: ['MR-PARENT-BLOG-CONTEXT'],
};
assert.equal(referenceBoundary(referenceFinding, referenceCase), true);
for (const mutate of [
  (candidate) => {
    candidate.ruleId = 'CSR.HAR.999';
  },
  (candidate) => {
    candidate.ruleVersion = 'wrong';
  },
  (candidate) => {
    candidate.evidenceIds = ['EV-UNKNOWN'];
  },
  (candidate) => {
    candidate.counterEvidenceIds = ['EV-UNKNOWN'];
  },
  (candidate) => {
    candidate.missingEvidenceIds = ['MR-UNKNOWN'];
  },
  (candidate) => {
    candidate.evidenceIds = [42];
  },
]) {
  const candidate = structuredClone(referenceFinding);
  mutate(candidate);
  assert.equal(referenceBoundary(candidate, referenceCase), false);
}

assert.deepEqual(
  hardSafetyEvaluation(referenceFinding, projections[2].record),
  {applicable: true, passed: true},
);
assert.deepEqual(
  hardSafetyEvaluation(
    {...referenceFinding, outcome: 'SUBSTANTIATED'},
    projections[2].record,
  ),
  {applicable: true, passed: false},
);
assert.deepEqual(hardSafetyEvaluation(validFinding, projections[3].record), {
  applicable: false,
  passed: null,
});
assert.deepEqual(safeProviderError(401, {error: {code: 'invalid key'}}), {
  category: 'AUTH_OR_ACCESS',
  code: 'invalid_key',
});
assert.equal(safeProviderError(403, {}).category, 'AUTH_OR_ACCESS');
assert.equal(safeProviderError(429, {}).category, 'RATE_LIMIT');
assert.equal(safeProviderError(500, {}).category, 'PROVIDER_TRANSIENT');
assert.equal(safeProviderError(400, {}).category, 'MODEL_OR_REQUEST');
assert.equal(safeProviderError(404, {}).category, 'MODEL_OR_REQUEST');
assert.equal(safeProviderError(418, {}).category, 'PROVIDER_ERROR');
assert.equal(
  providerStopDecision(400, {
    category: 'MODEL_OR_REQUEST',
    code: 'invalid_json_schema',
  }),
  'GLOBAL',
);
assert.equal(
  providerStopDecision(401, {category: 'AUTH_OR_ACCESS', code: 'invalid_key'}),
  'GLOBAL',
);
assert.equal(
  providerStopDecision(0, {
    category: 'BUDGET_GUARD',
    code: 'MAX_PROVIDER_CALLS_REACHED',
  }),
  'GLOBAL',
);
assert.equal(
  providerStopDecision(403, {category: 'AUTH_OR_ACCESS', code: 'access'}),
  'MODEL',
);
assert.equal(
  providerStopDecision(404, {
    category: 'MODEL_OR_REQUEST',
    code: 'model_not_found',
  }),
  'MODEL',
);
assert.equal(
  providerStopDecision(400, {
    category: 'MODEL_OR_REQUEST',
    code: 'invalid_model',
  }),
  'MODEL',
);
assert.equal(
  providerStopDecision(400, {
    category: 'MODEL_OR_REQUEST',
    code: 'invalid_request_error',
  }),
  'NONE',
);

const successfulRun = (overrides = {}) => ({
  model: 'gpt-5.6-sol',
  caseId: 'S2EVAL-018',
  repeat: 1,
  status: 'SUCCESS',
  providerCallAttempted: true,
  latencyMs: 100,
  usage,
  estimatedCostUsd: 0.005725,
  contractValid: true,
  referenceValid: true,
  hardSafetyApplicable: true,
  hardSafetyPass: true,
  result: referenceFinding,
  ...overrides,
});
const summary = summarizeRuns(config, [
  successfulRun(),
  successfulRun({repeat: 2, latencyMs: 200}),
  successfulRun({
    caseId: 'S2EVAL-022',
    repeat: 1,
    hardSafetyApplicable: false,
    hardSafetyPass: null,
    result: {...referenceFinding, outcome: 'CONFLICTED'},
  }),
  {
    model: 'gpt-5.6-sol',
    caseId: 'S2EVAL-022',
    repeat: 2,
    status: 'PROVIDER_ERROR',
    providerCallAttempted: true,
    latencyMs: 50,
    usage: normalizeUsage(),
    estimatedCostUsd: 0,
    error: {category: 'RATE_LIMIT', code: 'rate_limit'},
  },
  {
    model: 'gpt-5.6-terra',
    caseId: 'S2EVAL-018',
    repeat: 1,
    status: 'SKIPPED',
    providerCallAttempted: false,
    latencyMs: null,
    usage: normalizeUsage(),
    estimatedCostUsd: 0,
    error: {category: 'AUTH_OR_ACCESS', code: 'access'},
  },
]);
assert.equal(summary.providerCalled, true);
assert.equal(summary.qualityDenominator, 0);
assert.equal(summary.selectedModel, null);
assert.equal(
  summary.modelSelectionStatus,
  'NO_MODEL_SELECTED_INSUFFICIENT_GROUND_TRUTH',
);
assert.deepEqual(summary.eligibleByHardSafety, ['gpt-5.6-sol']);
assert.equal(summary.models[0].attemptedCalls, 4);
assert.equal(summary.models[0].successfulCalls, 3);
assert.equal(summary.models[0].hardSafetyPercent, 100);
assert.equal(summary.models[0].stableCasePairs, 1);
assert.equal(summary.models[0].comparableCasePairs, 1);
assert.equal(summary.models[0].stabilityPercent, 100);
assert.equal(summary.models[0].latencyMs.min, 100);
assert.equal(summary.models[0].latencyMs.p50, 100);
assert.equal(summary.models[0].latencyMs.p95, 200);
assert.equal(summary.models[0].latencyMs.max, 200);
assert.equal(summary.models[0].errors.length, 1);
assert.equal(summary.models[0].semanticObservations.length, 1);
assert.equal(summary.models[1].scheduledRuns, 1);
assert.equal(summary.models[1].attemptedCalls, 0);
assert.equal(summary.models[1].latencyMs.p50, null);
assert.equal(summary.totals.scheduledRuns, 5);
assert.equal(summary.totals.attemptedCalls, 4);
assert.equal(summarizeRuns(config, []).providerCalled, false);

assert.equal(
  config.modelCandidates.length *
    (config.datasetCaseRefs.length + config.directFixtureCaseRefs.length) *
    config.requestPolicy.repeats,
  config.requestPolicy.maxProviderCalls,
);
assert.ok(
  projectedMaximumCostUsd(config) <= config.requestPolicy.maxEstimatedCostUsd,
);
assert.equal(config.scoringPolicy.globalAccuracyAllowed, false);
assert.equal(config.scoringPolicy.modelSelectionAuthority, false);

assert.equal(manifest.benchmarkVersion, config.benchmarkVersion);
assert.equal(manifest.runtimeAuthority, false);
for (const [relativePath, expectedHash] of Object.entries(manifest.artifactHashes)) {
  assert.equal(sha256(relativePath), expectedHash, `Artifact drift: ${relativePath}`);
}
for (const [relativePath, expectedHash] of Object.entries(manifest.dependencyHashes)) {
  assert.equal(sha256(relativePath), expectedHash, `Dependency drift: ${relativePath}`);
}
for (const dependencyManifestPath of [paths.promptManifest, paths.datasetManifest]) {
  const dependencyManifest = json(dependencyManifestPath);
  for (const [relativePath, expectedHash] of Object.entries(
    dependencyManifest.artifactHashes,
  )) {
    assert.equal(sha256(relativePath), expectedHash, `Pinned artifact drift: ${relativePath}`);
  }
  for (const [relativePath, expectedHash] of Object.entries(
    dependencyManifest.dependencyHashes,
  )) {
    assert.equal(
      sha256(relativePath),
      expectedHash,
      `Pinned dependency drift: ${relativePath}`,
    );
  }
}

const promptDigest = sha256Text(prompts.join('\n---\n'));
assert.equal(promptDigest.length, 64);
console.log('S2_05_CONFIG_SCHEMA=PASS (1/1)');
console.log('S2_05_CONFIG_NEGATIVE_GUARDS=PASS (8/8)');
console.log('S2_05_OUTPUT_SCHEMA=PASS (1/1)');
console.log('S2_05_OUTPUT_NEGATIVE_GUARDS=PASS (5/5)');
console.log(`S2_05_DATASET_CASES=PASS (${projections.length}/5)`);
console.log('S2_05_DIRECT_CASES=PASS (1/1)');
console.log(`S2_05_PROMPT_ASSEMBLY=PASS (${prompts.length}/6)`);
console.log('S2_05_PROVIDER_REQUEST_BOUNDARY=PASS');
console.log('S2_05_COST_GUARD=PASS (0.918/1 USD)');
console.log('S2_05_NO_QUALITY_DENOMINATOR=PASS (0)');
console.log(`S2_05_PROMPT_DIGEST=${promptDigest}`);
console.log('S2_05_STATIC_GATE=PASS');
