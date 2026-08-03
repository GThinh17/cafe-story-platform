import {createRequire} from 'node:module';
import {
  mkdirSync,
  readFileSync,
  writeFileSync,
} from 'node:fs';
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
  promptSpec: 'docker/contracts/admin-report-ai-prompt-pilot-s2.json',
  assembler:
    'docker/n8n-code/admin-report-ai-resolution/assemble-prompt-pilot-s2-04.js',
  dataset: 'docker/tests/fixtures/admin-report-ai-evaluation-v1/dataset.json',
  directFixtures: 'docker/tests/fixtures/admin-report-ai-prompt-pilot-v1/cases.json',
  environment: 'docker/.env',
};
const defaultOutput =
  'documents/report-admin/resolve-report-ai-v2/09-sprints/evidence/' +
  '2026-07-30T15-39-00-343+07-00/raw/provider-benchmark-retest-01-results.json';
const absolute = (relativePath) => path.join(repositoryRoot, relativePath);
const text = (relativePath) => readFileSync(absolute(relativePath), 'utf8');
const json = (relativePath) => JSON.parse(text(relativePath));

const argumentsList = process.argv.slice(2);
const executeProvider = argumentsList.includes('--execute');
const outputArgumentIndex = argumentsList.indexOf('--output');
const outputRelativePath =
  outputArgumentIndex >= 0 ? argumentsList[outputArgumentIndex + 1] : defaultOutput;
if (
  outputArgumentIndex >= 0 &&
  (!outputRelativePath || path.isAbsolute(outputRelativePath) || outputRelativePath.startsWith('..'))
) {
  throw new Error('--output must be a repository-relative path');
}

const config = json(paths.config);
const configSchema = json(paths.configSchema);
const outputSchema = json(paths.outputSchema);
const promptSpec = json(paths.promptSpec);
const dataset = json(paths.dataset);
const directFixtures = json(paths.directFixtures);
const assemblerSource = text(paths.assembler);

const ajv = new Ajv2020({allErrors: true, strict: true});
addFormats(ajv);
const validateConfig = ajv.compile(configSchema);
if (!validateConfig(config)) {
  throw new Error(`Invalid benchmark config: ${JSON.stringify(validateConfig.errors)}`);
}
const validateOutput = ajv.compile(outputSchema);

const datasetRecords = new Map(dataset.records.map((record) => [record.caseId, record]));
const evidenceBundles = new Map(
  dataset.evidenceBundles.map((bundle) => [bundle.bundleId, bundle]),
);
const selectedDirectFixture = directFixtures.cases.find(
  (fixture) => fixture.caseId === config.directFixtureCaseRefs[0],
);
if (!selectedDirectFixture) throw new Error('Direct benchmark fixture is missing');

const benchmarkCases = [
  ...config.datasetCaseRefs.map((caseId) => {
    const record = datasetRecords.get(caseId);
    if (!record) throw new Error(`Missing dataset case: ${caseId}`);
    const bundle = evidenceBundles.get(record.evidenceBundleId);
    if (!bundle) throw new Error(`Missing evidence bundle: ${record.evidenceBundleId}`);
    return {
      caseId,
      record,
      projection: buildDatasetProjection(record, bundle),
    };
  }),
  {
    caseId: selectedDirectFixture.caseId,
    record: null,
    projection: buildDirectProjection(selectedDirectFixture),
  },
];

const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
const executeAssembler = new AsyncFunction(
  '$json',
  `${assemblerSource}\n//# sourceURL=assemble-prompt-pilot-s2-04.js`,
);
const promptByCase = new Map();
for (const benchmarkCase of benchmarkCases) {
  const requestContext = buildRequestContext(
    selectedDirectFixture.requestContext,
    benchmarkCase.projection,
  );
  const assembled = await executeAssembler({
    promptPilotSpec: promptSpec,
    requestContext,
  });
  const output = assembled?.[0]?.json;
  if (
    output?.candidateVersion !== config.promptCandidateVersion ||
    output?.selectedRuleIds?.length !== 1 ||
    output.selectedRuleIds[0] !== 'CSR.HAR.001'
  ) {
    throw new Error(`${benchmarkCase.caseId}: prompt candidate boundary failed`);
  }
  promptByCase.set(benchmarkCase.caseId, output.systemPrompt);
}

const scheduledRuns =
  config.modelCandidates.length *
  benchmarkCases.length *
  config.requestPolicy.repeats;
const projectedCostUsd = projectedMaximumCostUsd(config);
if (scheduledRuns !== config.requestPolicy.maxProviderCalls) {
  throw new Error(
    `Scheduled calls ${scheduledRuns} do not match max ${config.requestPolicy.maxProviderCalls}`,
  );
}
if (projectedCostUsd > config.requestPolicy.maxEstimatedCostUsd) {
  throw new Error(
    `Projected cost ${projectedCostUsd} exceeds ${config.requestPolicy.maxEstimatedCostUsd}`,
  );
}

const environmentText = text(paths.environment);
const apiKey = parseDotEnvValue(environmentText, 'OPENAI_API_KEY');
const keyPresent = apiKey.length >= 20;
const dryRun = {
  mode: executeProvider ? 'EXECUTE' : 'DRY_RUN',
  providerCallAuthorizedBy: config.providerCallAuthorizedBy,
  endpoint: config.apiEndpoint,
  keyPresent,
  scheduledRuns,
  maximumProviderRequestsIncludingRetries: config.requestPolicy.maxProviderCalls,
  projectedMaximumCostUsd: projectedCostUsd,
  budgetUsd: config.requestPolicy.maxEstimatedCostUsd,
  promptCandidateVersion: config.promptCandidateVersion,
  caseIds: benchmarkCases.map((item) => item.caseId),
  models: config.modelCandidates.map((item) => item.model),
  runtimeAuthority: false,
  outputPath: outputRelativePath,
};
console.log(JSON.stringify(dryRun, null, 2));
if (!executeProvider) process.exit(0);
if (!keyPresent) throw new Error('OPENAI_API_KEY is absent or not length-valid');

const schemaForProvider = buildStructuredOutputSchema(outputSchema);

const runs = [];
const unavailableModels = new Map();
let globalStopReason = null;
let providerRequestCount = 0;

const callProvider = async (body) => {
  let lastError;
  for (
    let retry = 0;
    retry <= config.requestPolicy.maxTransientRetries;
    retry += 1
  ) {
    if (providerRequestCount >= config.requestPolicy.maxProviderCalls) {
      return {
        status: 0,
        attemptCount: retry,
        latencyMs: 0,
        response: null,
        error: {category: 'BUDGET_GUARD', code: 'MAX_PROVIDER_CALLS_REACHED'},
      };
    }
    providerRequestCount += 1;
    const startedAt = performance.now();
    const controller = new AbortController();
    const timeout = setTimeout(
      () => controller.abort(),
      config.requestPolicy.timeoutMs,
    );
    try {
      const response = await fetch(config.apiEndpoint, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${apiKey}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
        signal: controller.signal,
      });
      const latencyMs = Math.round(performance.now() - startedAt);
      const responseBody = await response.json().catch(() => ({}));
      if (response.ok) {
        return {
          status: response.status,
          attemptCount: retry + 1,
          latencyMs,
          response: responseBody,
          error: null,
        };
      }
      const safeError = safeProviderError(response.status, responseBody);
      lastError = {
        status: response.status,
        attemptCount: retry + 1,
        latencyMs,
        response: responseBody,
        error: safeError,
      };
      const transient = response.status === 429 || response.status >= 500;
      if (!transient || retry === config.requestPolicy.maxTransientRetries) {
        return lastError;
      }
    } catch (error) {
      const latencyMs = Math.round(performance.now() - startedAt);
      lastError = {
        status: 0,
        attemptCount: retry + 1,
        latencyMs,
        response: null,
        error: {
          category: error?.name === 'AbortError' ? 'TIMEOUT' : 'NETWORK',
          code: error?.name === 'AbortError' ? 'REQUEST_TIMEOUT' : 'NETWORK_ERROR',
        },
      };
      if (retry === config.requestPolicy.maxTransientRetries) return lastError;
    } finally {
      clearTimeout(timeout);
    }
  }
  return lastError;
};

for (const candidate of config.modelCandidates) {
  for (const benchmarkCase of benchmarkCases) {
    for (let repeat = 1; repeat <= config.requestPolicy.repeats; repeat += 1) {
      const skippedReason =
        globalStopReason ?? unavailableModels.get(candidate.model) ?? null;
      if (skippedReason) {
        runs.push({
          model: candidate.model,
          caseId: benchmarkCase.caseId,
          repeat,
          status: 'SKIPPED',
          providerCallAttempted: false,
          providerRequestAttempts: 0,
          latencyMs: null,
          usage: normalizeUsage(),
          estimatedCostUsd: 0,
          error: skippedReason,
        });
        console.log(
          `MODEL=${candidate.model} CASE=${benchmarkCase.caseId} ` +
            `REPEAT=${repeat} STATUS=SKIPPED`,
        );
        continue;
      }

      const requestBody = buildProviderRequest({
        model: candidate.model,
        systemPrompt: promptByCase.get(benchmarkCase.caseId),
        projection: benchmarkCase.projection,
        outputSchema: schemaForProvider,
        requestPolicy: config.requestPolicy,
      });
      const provider = await callProvider(requestBody);
      const usage = normalizeUsage(provider.response?.usage);
      const estimatedCostUsd = calculateCostUsd(usage, candidate.pricingStandard);
      const baseRun = {
        model: candidate.model,
        caseId: benchmarkCase.caseId,
        repeat,
        providerCallAttempted: provider.attemptCount > 0,
        providerRequestAttempts: provider.attemptCount,
        httpStatus: provider.status || null,
        latencyMs: provider.latencyMs,
        usage,
        estimatedCostUsd,
      };
      if (provider.error) {
        const run = {...baseRun, status: 'PROVIDER_ERROR', error: provider.error};
        runs.push(run);
        const stopDecision = providerStopDecision(provider.status, provider.error);
        if (stopDecision === 'GLOBAL') globalStopReason = provider.error;
        if (stopDecision === 'MODEL') {
          unavailableModels.set(candidate.model, provider.error);
        }
        console.log(
          `MODEL=${candidate.model} CASE=${benchmarkCase.caseId} REPEAT=${repeat} ` +
            `STATUS=${run.status} LATENCY_MS=${run.latencyMs} ` +
            `ERROR=${provider.error.category}:${provider.error.code}`,
        );
        continue;
      }

      const responseText = outputText(provider.response);
      let result;
      try {
        result = JSON.parse(responseText);
      } catch {
        runs.push({
          ...baseRun,
          status: 'CONTRACT_INVALID',
          contractValid: false,
          error: {category: 'OUTPUT_CONTRACT', code: 'INVALID_JSON'},
        });
        console.log(
          `MODEL=${candidate.model} CASE=${benchmarkCase.caseId} REPEAT=${repeat} ` +
            `STATUS=CONTRACT_INVALID LATENCY_MS=${baseRun.latencyMs}`,
        );
        continue;
      }
      const contractValid = validateOutput(result);
      const referenceValid =
        contractValid && referenceBoundary(result, benchmarkCase.projection);
      const hardSafety = contractValid
        ? hardSafetyEvaluation(
            result,
            benchmarkCase.record ?? {caseClass: 'CONTRACT_STABILITY_ONLY'},
          )
        : {applicable: false, passed: null};
      const status =
        contractValid && referenceValid ? 'SUCCESS' : 'CONTRACT_INVALID';
      runs.push({
        ...baseRun,
        status,
        contractValid,
        referenceValid,
        hardSafetyApplicable: hardSafety.applicable,
        hardSafetyPass: hardSafety.passed,
        result: contractValid ? result : undefined,
        error:
          status === 'SUCCESS'
            ? undefined
            : {
                category: 'OUTPUT_CONTRACT',
                code: contractValid ? 'INVALID_REFERENCE' : 'SCHEMA_INVALID',
              },
      });
      console.log(
        `MODEL=${candidate.model} CASE=${benchmarkCase.caseId} REPEAT=${repeat} ` +
          `STATUS=${status} LATENCY_MS=${baseRun.latencyMs}`,
      );
    }
  }
}

const summary = summarizeRuns(config, runs);
const resultArtifact = {
  artifactVersion: '1.0.0',
  generatedAt: new Date().toISOString(),
  classification: 'SANITIZED_SYNTHETIC_BENCHMARK_EVIDENCE',
  secretsPersisted: false,
  rawProviderBodiesPersisted: false,
  rawProviderErrorsPersisted: false,
  runtimeAuthority: false,
  benchmarkConfig: {
    benchmarkVersion: config.benchmarkVersion,
    promptCandidateVersion: config.promptCandidateVersion,
    modelCandidates: config.modelCandidates.map(({model, role}) => ({model, role})),
    datasetCaseRefs: config.datasetCaseRefs,
    directFixtureCaseRefs: config.directFixtureCaseRefs,
    requestPolicy: config.requestPolicy,
    scoringPolicy: config.scoringPolicy,
  },
  fingerprints: Object.fromEntries(
    Object.entries(paths)
      .filter(([name]) => name !== 'environment')
      .map(([name, relativePath]) => [name, sha256Text(text(relativePath))]),
  ),
  dryRun,
  actualProviderRequests: providerRequestCount,
  summary,
  runs,
};
const outputAbsolutePath = absolute(outputRelativePath);
mkdirSync(path.dirname(outputAbsolutePath), {recursive: true});
writeFileSync(outputAbsolutePath, `${JSON.stringify(resultArtifact, null, 2)}\n`, 'utf8');
console.log(`S2_05_PROVIDER_REQUESTS=${providerRequestCount}`);
console.log(`S2_05_ACTUAL_COST_USD=${summary.totals.estimatedCostUsd}`);
console.log(`S2_05_MODEL_SELECTION=${summary.modelSelectionStatus}`);
console.log(`S2_05_RESULT=${outputRelativePath}`);
