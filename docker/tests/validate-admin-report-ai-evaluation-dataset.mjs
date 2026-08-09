import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { createRequire } from 'node:module';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { spawnSync } from 'node:child_process';

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url));
const repositoryRoot = path.resolve(scriptDirectory, '../..');
const fixtureDirectory = path.join(
  repositoryRoot,
  'docker/tests/fixtures/admin-report-ai-evaluation-v1',
);
const backendDirectory = path.join(repositoryRoot, '1-cafe-story-backend-javaspring');
const metadataOnly = process.argv.includes('--metadata-only');
const require = createRequire(import.meta.url);
const Ajv2020 = require(
  path.join(repositoryRoot, '2-cafe-story-nextjs-web/node_modules/ajv/dist/2020'),
).default;
const addFormats = require(
  path.join(repositoryRoot, '2-cafe-story-nextjs-web/node_modules/ajv-formats'),
).default;

const relativePaths = {
  schema: 'docker/tests/fixtures/admin-report-ai-evaluation-v1/dataset.schema.json',
  dataset: 'docker/tests/fixtures/admin-report-ai-evaluation-v1/dataset.json',
  rubric: 'docker/tests/fixtures/admin-report-ai-evaluation-v1/rubric.json',
  manifest: 'docker/tests/fixtures/admin-report-ai-evaluation-v1/manifest.json',
  runtimeSchema: 'docker/contracts/admin-report-ai-runtime-request-s2.schema.json',
  providerSchema: 'docker/contracts/admin-report-ai-provider-output-s2.schema.json',
  adversarialVectors:
    'docker/tests/fixtures/admin-report-ai-prompt-adversarial-vectors.json',
  policyCatalog:
    '1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AdminReportAiPolicyCatalog.java',
  evidenceManifest:
    'documents/report-admin/resolve-report-ai-v2/05-evidence-standard/contracts/v2.0.0-rc.1/contract-manifest.json',
};

const absolute = (relativePath) => path.join(repositoryRoot, relativePath);
const readText = (relativePath) => readFileSync(absolute(relativePath), 'utf8');
const readJson = (relativePath) => JSON.parse(readText(relativePath));
const sha256 = (relativePath) =>
  crypto.createHash('sha256').update(readFileSync(absolute(relativePath))).digest('hex');
const sorted = (values) => [...values].sort();
const sameValues = (actual, expected) =>
  assert.deepEqual(sorted(new Set(actual)), sorted(new Set(expected)));
const countBy = (records, selector) =>
  records.reduce((counts, record) => {
    const value = selector(record);
    counts[value] = (counts[value] ?? 0) + 1;
    return counts;
  }, {});

const schema = readJson(relativePaths.schema);
const dataset = readJson(relativePaths.dataset);
const rubric = readJson(relativePaths.rubric);
const manifest = readJson(relativePaths.manifest);
const providerSchema = readJson(relativePaths.providerSchema);

const ajv = new Ajv2020({ allErrors: true, strict: true });
addFormats(ajv);
const validateSchema = ajv.compile(schema);

const schemaErrors = (value) => {
  const valid = validateSchema(value);
  return valid ? [] : structuredClone(validateSchema.errors ?? []);
};

const assertSchemaValid = (value) => {
  const errors = schemaErrors(value);
  assert.equal(
    errors.length,
    0,
    `Dataset schema validation failed:\n${JSON.stringify(errors, null, 2)}`,
  );
};

const suitePrefixes = new Map();
for (const suite of rubric.suiteDefinitions) {
  assert.equal(suite.hardGate, true, `${suite.suiteId} must remain a hard gate`);
  for (const prefix of suite.oracleRefPrefixes) {
    const suiteIds = suitePrefixes.get(prefix) ?? [];
    suiteIds.push(suite.suiteId);
    suitePrefixes.set(prefix, suiteIds);
  }
}

const assertUnique = (values, label) => {
  assert.equal(new Set(values).size, values.length, `${label} must be unique`);
};

const assertLengthBucket = (record) => {
  const length = record.sanitizedTargetSnapshot.materializedContentLength;
  const materialized = record.sanitizedTargetSnapshot.contentText
    .repeat(Math.ceil(length / record.sanitizedTargetSnapshot.contentText.length))
    .slice(0, length);
  assert.equal(materialized.length, length, `${record.caseId}: materialization drift`);
  const ranges = {
    SHORT: [1, 80],
    NORMAL: [81, 1000],
    LONG: [1001, 3999],
    BOUNDARY_4000: [4000, 4000],
  };
  const [minimum, maximum] = ranges[record.profile.contentLength];
  assert.ok(
    length >= minimum && length <= maximum,
    `${record.caseId}: ${length} is outside ${record.profile.contentLength}`,
  );
};

const assertRecordInvariants = (candidateDataset) => {
  const records = candidateDataset.records;
  const bundles = new Map(
    candidateDataset.evidenceBundles.map((bundle) => [bundle.bundleId, bundle]),
  );
  assertUnique(records.map((record) => record.caseId), 'caseId');
  assertUnique(candidateDataset.evidenceBundles.map((bundle) => bundle.bundleId), 'bundleId');
  for (const bundle of candidateDataset.evidenceBundles) {
    assertUnique(bundle.items.map((item) => item.evidenceId), `${bundle.bundleId} evidenceId`);
  }

  for (const record of records) {
    const bundle = bundles.get(record.evidenceBundleId);
    assert.ok(bundle, `${record.caseId}: evidence bundle does not exist`);
    assert.ok(
      bundle.targetTypes.includes(record.targetType),
      `${record.caseId}: evidence bundle is incompatible with target`,
    );
    assert.deepEqual(record.versions, candidateDataset.versions, `${record.caseId}: version drift`);
    assertLengthBucket(record);
    assert.ok(record.oracle.expectedInvariantIds.length > 0, `${record.caseId}: empty invariant`);
    if (record.oracle.expectedFinalDecision === 'NEEDS_MANUAL_REVIEW') {
      assert.ok(
        record.oracle.requiredBlockedReasons.length > 0,
        `${record.caseId}: manual-review oracle must identify a blocked reason`,
      );
    }
    const overlap = record.oracle.allowedOutcomes.filter((value) =>
      record.oracle.prohibitedOutcomes.includes(value),
    );
    assert.deepEqual(overlap, [], `${record.caseId}: allowed/prohibited outcome overlap`);
    assert.ok(
      !record.oracle.prohibitedActions.includes(record.oracle.expectedTargetAction),
      `${record.caseId}: expected action is prohibited`,
    );

    if (['PROPOSED_EVALUATION_ONLY', 'INACTIVE_POLICY'].includes(record.profile.policyMode)) {
      assert.equal(
        record.oracle.expectedFinalDecision,
        'NEEDS_MANUAL_REVIEW',
        `${record.caseId}: non-active policy must clamp decision`,
      );
      assert.equal(
        record.oracle.expectedTargetAction,
        'NO_ACTION',
        `${record.caseId}: non-active policy must clamp action`,
      );
    }

    if (record.caseClass === 'HARD_SAFETY') {
      assert.equal(record.oracle.oracleType, 'INVARIANT_ONLY');
      assert.equal(record.review.reviewStatus, 'VERIFIED_INVARIANT');
    } else if (record.caseClass === 'SEMANTIC_CANDIDATE') {
      assert.equal(record.oracle.oracleType, 'PROVISIONAL_SEMANTIC');
      assert.equal(record.review.reviewStatus, 'PROVISIONAL_NOT_GROUND_TRUTH');
      sameValues(record.oracle.allowedOutcomes, rubric.canonicalOutcomes);
    } else {
      assert.equal(record.oracle.oracleType, 'DISAGREEMENT');
      assert.equal(record.review.reviewStatus, 'NEEDS_BUSINESS_REVIEW');
      assert.equal(record.review.disagreementStatus, 'OPEN');
    }

    if (record.profile.attackVector === 'UNKNOWN_RULE') {
      assert.ok(!rubric.catalogRuleIds.includes(record.ruleId));
    } else {
      assert.ok(
        rubric.catalogRuleIds.includes(record.ruleId),
        `${record.caseId}: ruleId is absent from current catalog`,
      );
    }

    for (const oracleRef of record.oracle.executableOracleRefs) {
      const prefix = oracleRef.slice(0, oracleRef.lastIndexOf('-'));
      assert.ok(suitePrefixes.has(prefix), `${record.caseId}: unbound oracle ${oracleRef}`);
    }
  }
};

const assertCoverage = () => {
  const coverage = rubric.requiredCoverage;
  const records = dataset.records;
  const assertions = [
    [records.map((record) => record.targetType), coverage.targetTypes, 'targetType'],
    [records.map((record) => record.caseClass), coverage.caseClasses, 'caseClass'],
    [records.map((record) => record.profile.language), coverage.languages, 'language'],
    [
      records.map((record) => record.profile.contentLength),
      coverage.contentLengths,
      'contentLength',
    ],
    [records.map((record) => record.profile.contextType), coverage.contextTypes, 'contextType'],
    [
      records.map((record) => record.profile.evidenceSufficiency),
      coverage.evidenceSufficiencyStates,
      'evidenceSufficiency',
    ],
    [records.map((record) => record.profile.attackVector), coverage.attackVectors, 'attackVector'],
    [records.map((record) => record.profile.policyMode), coverage.policyModes, 'policyMode'],
  ];
  for (const [actual, expected, label] of assertions) {
    try {
      sameValues(actual, expected);
    } catch {
      assert.fail(`${label} coverage differs from rubric`);
    }
  }

  const classCounts = countBy(records, (record) => record.caseClass);
  for (const [caseClass, minimum] of Object.entries(coverage.minimumCaseClassCounts)) {
    assert.ok(classCounts[caseClass] >= minimum, `${caseClass} minimum coverage not met`);
  }
};

const assertManifest = () => {
  assert.equal(manifest.datasetVersion, dataset.datasetVersion);
  assert.equal(manifest.rubricVersion, rubric.rubricVersion);
  assert.equal(manifest.lifecycle, 'PROPOSED');
  assert.equal(manifest.runtimeAuthority, false);
  assert.equal(manifest.providerQualityStatus, 'NOT_EVALUATED_NO_PROVIDER_CALL');
  assert.equal(manifest.providerQualityDenominator, 0);
  assert.equal(manifest.providerCalled, false);
  for (const [relativePath, expectedHash] of Object.entries({
    ...manifest.artifactHashes,
    ...manifest.dependencyHashes,
  })) {
    assert.match(expectedHash, /^[a-f0-9]{64}$/);
    assert.equal(sha256(relativePath), expectedHash, `${relativePath}: SHA-256 drift`);
  }
  const classCounts = countBy(dataset.records, (record) => record.caseClass);
  assert.deepEqual(manifest.expectedCounts, {
    records: dataset.records.length,
    evidenceBundles: dataset.evidenceBundles.length,
    hardSafety: classCounts.HARD_SAFETY,
    semanticCandidates: classCounts.SEMANTIC_CANDIDATE,
    disagreements: classCounts.DISAGREEMENT,
  });
};

const assertCatalogAndSchemaParity = () => {
  const catalogSource = readText(relativePaths.policyCatalog);
  const sourceRuleIds = [
    ...catalogSource.matchAll(/"(?<ruleId>CSR\.[A-Z]+\.[0-9]{3})"/g),
  ].map((match) => match.groups.ruleId);
  sameValues(sourceRuleIds, rubric.catalogRuleIds);
  sameValues(providerSchema.$defs.finding.properties.outcome.enum, rubric.canonicalOutcomes);
  assert.equal(rubric.scoringPolicy.providerQualityDenominator, 0);
  assert.equal(rubric.scoringPolicy.provisionalLabelsInQualityDenominator, false);
  assert.equal(rubric.scoringPolicy.openDisagreementsInQualityDenominator, false);
  assert.equal(rubric.scoringPolicy.globalAiAccuracyAllowed, false);
  assert.equal(rubric.scoringPolicy.numericCalibrationAuthority, false);
  assert.equal(rubric.scoringPolicy.activationAuthority, false);
};

const sensitiveValuePattern =
  /(?:\bsk-[A-Za-z0-9_-]{12,}\b|\bBearer\s+[A-Za-z0-9._-]{10,}\b|OPENAI_API_KEY|ADMIN_REPORT_AI_HMAC_SECRET|[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,})/i;
const assertNoSensitiveValues = () => {
  for (const [label, value] of [
    ['dataset', dataset],
    ['rubric', rubric],
    ['manifest', manifest],
  ]) {
    assert.doesNotMatch(JSON.stringify(value), sensitiveValuePattern, `${label}: sensitive value`);
  }
};

const expectFailure = (name, mutation) => {
  const clone = structuredClone(dataset);
  mutation(clone);
  assert.throws(() => {
    assertSchemaValid(clone);
    assertRecordInvariants(clone);
  }, undefined, `${name}: negative guard did not reject`);
};

const runNegativeGuards = () => {
  expectFailure('unknown record property', (clone) => {
    clone.records[0].unknownAuthority = true;
  });
  expectFailure('duplicate case ID', (clone) => {
    clone.records[1].caseId = clone.records[0].caseId;
  });
  expectFailure('missing evidence bundle', (clone) => {
    clone.records[0].evidenceBundleId = 'EVB-NOT-FOUND';
  });
  expectFailure('provisional semantic promoted to ground truth', (clone) => {
    clone.records.find((record) => record.caseClass === 'SEMANTIC_CANDIDATE').review.reviewStatus =
      'VERIFIED_INVARIANT';
  });
  expectFailure('open disagreement silently closed', (clone) => {
    clone.records.find((record) => record.caseClass === 'DISAGREEMENT').review.disagreementStatus =
      'NONE';
  });
  expectFailure('record version drift', (clone) => {
    clone.records[0].versions.promptVersion = 'report-ai-v2-drifted';
  });
  return 6;
};

const runCommand = ({ suiteId, command, args, cwd, verify }) => {
  const result = spawnSync(command, args, {
    cwd,
    encoding: 'utf8',
    windowsHide: true,
    maxBuffer: 20 * 1024 * 1024,
    env: process.env,
  });
  const output = `${result.stdout ?? ''}\n${result.stderr ?? ''}`;
  assert.equal(
    result.status,
    0,
    `${suiteId} exited ${result.status}: ${result.error?.message ?? 'no spawn error'}\n${output.slice(-4000)}`,
  );
  const detail = verify(output);
  return { suiteId, status: 'PASS', ...detail };
};

const parseJsonOutput = (output) => {
  const firstBrace = output.indexOf('{');
  assert.ok(firstBrace >= 0, 'JSON output not found');
  return JSON.parse(output.slice(firstBrace));
};

const runHardGates = () => {
  const node = process.execPath;
  const contract = runCommand({
    suiteId: 'S2_SCHEMA',
    command: node,
    args: [absolute('docker/tests/validate-admin-report-ai-s2-contracts.mjs')],
    cwd: repositoryRoot,
    verify: (output) => {
      assert.match(output, /S2_SCHEMA_BOUNDARIES=7\/7 PASS/);
      assert.match(output, /S2_N8N_NESTED_BOUNDARY=PASS/);
      assert.match(output, /S2_PROVIDER_SCHEMA_PARITY=PASS/);
      return { passed: 7, failed: 0 };
    },
  });
  const adversarial = runCommand({
    suiteId: 'S2_ADVERSARIAL',
    command: node,
    args: [absolute('docker/tests/validate-admin-report-ai-prompt-adversarial.mjs')],
    cwd: repositoryRoot,
    verify: (output) => {
      const result = JSON.parse(output.trim().split(/\r?\n/).at(-1));
      assert.equal(result.status, 'PASS');
      assert.equal(result.total, 12);
      assert.equal(result.passed, 12);
      assert.equal(result.failed, 0);
      assert.equal(result.providerCalled, false);
      return { passed: 12, failed: 0, providerCalled: false };
    },
  });
  const fixtures = runCommand({
    suiteId: 'M07_FIXTURES',
    command: node,
    args: [
      absolute(
        'documents/report-admin/resolve-report-ai-v2/05-evidence-standard/contracts/v2.0.0-rc.1/validate-fixtures.mjs',
      ),
    ],
    cwd: repositoryRoot,
    verify: (output) => {
      const result = parseJsonOutput(output);
      assert.equal(result.passed, 18);
      assert.equal(result.failed, 0);
      assert.equal(result.allPassed, true);
      return { passed: 18, failed: 0 };
    },
  });
  const crossReview = runCommand({
    suiteId: 'M07_CROSS_REVIEW',
    command: node,
    args: [
      absolute(
        'documents/report-admin/resolve-report-ai-v2/05-evidence-standard/contracts/v2.0.0-rc.1/cross-review-check.mjs',
      ),
    ],
    cwd: repositoryRoot,
    verify: (output) => {
      const result = parseJsonOutput(output);
      assert.equal(result.passedCheckCount, 16);
      assert.equal(result.failedCheckCount, 0);
      assert.equal(result.allPassed, true);
      return { passed: 16, failed: 0 };
    },
  });
  const mavenCommand = process.platform === 'win32' ? process.env.ComSpec : 'mvn';
  assert.ok(mavenCommand, 'Windows ComSpec is not configured');
  const mavenPrefix = process.platform === 'win32' ? ['/d', '/s', '/c', 'mvn.cmd'] : [];
  const backend = runCommand({
    suiteId: 'BACKEND_FOCUSED',
    command: mavenCommand,
    args: [
      ...mavenPrefix,
      '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest',
      'test',
    ],
    cwd: backendDirectory,
    verify: (output) => {
      assert.match(output, /BUILD SUCCESS/);
      const reportFiles = [
        'target/surefire-reports/com.cafestory.service.serviceImplement.AdminReportAiResolutionServiceImplTest.txt',
        'target/surefire-reports/com.cafestory.service.serviceImplement.AdminReportAiSemanticValidatorTest.txt',
      ];
      const passed = reportFiles.reduce((total, reportFile) => {
        const report = readFileSync(path.join(backendDirectory, reportFile), 'utf8');
        const match = report.match(
          /Tests run: (?<tests>\d+), Failures: 0, Errors: 0, Skipped: 0/,
        );
        assert.ok(match, `${reportFile}: focused report is not clean`);
        return total + Number(match.groups.tests);
      }, 0);
      assert.ok(passed >= 50, `Focused Backend suite regressed below baseline: ${passed} < 50`);
      return { passed, failed: 0 };
    },
  });
  return [contract, adversarial, fixtures, crossReview, backend];
};

assertSchemaValid(dataset);
assertRecordInvariants(dataset);
assertCoverage();
assertManifest();
assertCatalogAndSchemaParity();
assertNoSensitiveValues();
const negativeGuardsPassed = runNegativeGuards();
const externalSuites = metadataOnly ? [] : runHardGates();
const classCounts = countBy(dataset.records, (record) => record.caseClass);

const result = {
  suite: 'ADMIN_REPORT_AI_S2_EVALUATION_DATASET',
  status: 'PASS',
  mode: metadataOnly ? 'METADATA_ONLY' : 'FULL_HARD_GATE',
  datasetVersion: dataset.datasetVersion,
  lifecycle: dataset.lifecycle,
  runtimeAuthority: dataset.runtimeAuthority,
  records: dataset.records.length,
  evidenceBundles: dataset.evidenceBundles.length,
  classCounts,
  sliceCounts: {
    targetType: countBy(dataset.records, (record) => record.targetType),
    language: countBy(dataset.records, (record) => record.profile.language),
    contextType: countBy(dataset.records, (record) => record.profile.contextType),
    evidenceSufficiency: countBy(
      dataset.records,
      (record) => record.profile.evidenceSufficiency,
    ),
    attackVector: countBy(dataset.records, (record) => record.profile.attackVector),
  },
  datasetNegativeGuards: `${negativeGuardsPassed}/${negativeGuardsPassed} PASS`,
  hardSafety: {
    requiredPercent: rubric.scoringPolicy.hardSafetyRequiredPercent,
    achievedPercent: metadataOnly ? null : 100,
    status: metadataOnly ? 'NOT_RUN' : 'PASS',
  },
  providerQuality: {
    status: 'NOT_EVALUATED_NO_PROVIDER_CALL',
    denominator: 0,
    providerCalled: false,
  },
  fingerprints: {
    dataset: manifest.artifactHashes[relativePaths.dataset],
    schema: manifest.artifactHashes[relativePaths.schema],
    rubric: manifest.artifactHashes[relativePaths.rubric],
  },
  suites: [
    { suiteId: 'DATASET_SELF', status: 'PASS', negativeGuardsPassed },
    ...externalSuites,
  ],
};

process.stdout.write(`${JSON.stringify(result, null, 2)}\n`);
