import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { createRequire } from 'node:module';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url));
const repositoryRoot = path.resolve(scriptDirectory, '../..');
const require = createRequire(import.meta.url);
const Ajv2020 = require(
  path.join(repositoryRoot, '2-cafe-story-nextjs-web/node_modules/ajv/dist/2020'),
).default;
const addFormats = require(
  path.join(repositoryRoot, '2-cafe-story-nextjs-web/node_modules/ajv-formats'),
).default;

const relativePaths = {
  schema: 'docker/contracts/admin-report-ai-prompt-pilot-s2.schema.json',
  spec: 'docker/contracts/admin-report-ai-prompt-pilot-s2.json',
  assembler:
    'docker/n8n-code/admin-report-ai-resolution/assemble-prompt-pilot-s2-04.js',
  fixtures: 'docker/tests/fixtures/admin-report-ai-prompt-pilot-v1/cases.json',
  manifest: 'docker/tests/fixtures/admin-report-ai-prompt-pilot-v1/manifest.json',
  dataset: 'docker/tests/fixtures/admin-report-ai-evaluation-v1/dataset.json',
  datasetManifest: 'docker/tests/fixtures/admin-report-ai-evaluation-v1/manifest.json',
  providerSchema: 'docker/contracts/admin-report-ai-provider-output-s2.schema.json',
  backendCatalog:
    '1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AdminReportAiPolicyCatalog.java',
};
const absolute = (relativePath) => path.join(repositoryRoot, relativePath);
const readText = (relativePath) => readFileSync(absolute(relativePath), 'utf8');
const readJson = (relativePath) => JSON.parse(readText(relativePath));
const sha256 = (relativePath) =>
  crypto.createHash('sha256').update(readFileSync(absolute(relativePath))).digest('hex');

const schema = readJson(relativePaths.schema);
const spec = readJson(relativePaths.spec);
const fixtures = readJson(relativePaths.fixtures);
const manifest = readJson(relativePaths.manifest);
const dataset = readJson(relativePaths.dataset);
const datasetManifest = readJson(relativePaths.datasetManifest);
const providerSchema = readJson(relativePaths.providerSchema);
const assemblerSource = readText(relativePaths.assembler);
const backendCatalogSource = readText(relativePaths.backendCatalog);

const ajv = new Ajv2020({allErrors: true, strict: true});
addFormats(ajv);
const validateSpec = ajv.compile(schema);
const specErrors = (candidate) => {
  const valid = validateSpec(candidate);
  return valid ? [] : structuredClone(validateSpec.errors ?? []);
};
assert.equal(
  specErrors(spec).length,
  0,
  `Prompt pilot spec schema failed:\n${JSON.stringify(specErrors(spec), null, 2)}`,
);

assert.equal(manifest.candidateVersion, spec.candidateVersion);
assert.equal(manifest.lifecycle, 'PROPOSED');
assert.equal(manifest.runtimeAuthority, false);
assert.equal(manifest.providerCalled, false);
for (const [relativePath, expectedHash] of Object.entries(manifest.artifactHashes)) {
  assert.equal(sha256(relativePath), expectedHash, `Artifact fingerprint drift: ${relativePath}`);
}
for (const [relativePath, expectedHash] of Object.entries(manifest.dependencyHashes)) {
  assert.equal(sha256(relativePath), expectedHash, `Dependency fingerprint drift: ${relativePath}`);
}
assert.equal(datasetManifest.datasetVersion, spec.sourceContextVersions.datasetVersion);
assert.equal(datasetManifest.rubricVersion, spec.sourceContextVersions.rubricVersion);

const profileIds = spec.selectedProfiles.map((profile) => profile.profileId);
const profileRuleIds = spec.selectedProfiles.map((profile) => profile.ruleId);
assert.equal(new Set(profileIds).size, profileIds.length, 'profileId must be unique');
assert.equal(new Set(profileRuleIds).size, profileRuleIds.length, 'pilot ruleId must be unique');
const exclusionRuleIds = spec.exclusions.map((item) => item.ruleId);
assert.equal(new Set(exclusionRuleIds).size, exclusionRuleIds.length, 'exclusion ruleId must be unique');
assert.deepEqual(
  profileRuleIds.filter((ruleId) => exclusionRuleIds.includes(ruleId)),
  [],
  'selected and excluded rule IDs overlap',
);
const canonicalFinding = providerSchema.$defs?.finding;
assert.ok(canonicalFinding, 'canonical provider finding schema is missing');
assert.ok(canonicalFinding.properties?.evidenceIds, 'canonical evidenceIds field is missing');
assert.ok(
  canonicalFinding.required?.includes('evidenceIds'),
  'canonical evidenceIds field must remain required',
);
assert.equal(
  canonicalFinding.properties?.supportingEvidenceIds,
  undefined,
  'candidate must not target a non-canonical supportingEvidenceIds field',
);

const datasetRecords = new Map(dataset.records.map((record) => [record.caseId, record]));
for (const profile of spec.selectedProfiles) {
  assert.ok(
    backendCatalogSource.includes(`"${profile.ruleId}"`),
    `${profile.ruleId} is not present in Backend catalog source`,
  );
  for (const caseId of profile.datasetCaseRefs) {
    const record = datasetRecords.get(caseId);
    assert.ok(record, `${profile.profileId}: missing dataset case ${caseId}`);
    assert.equal(record.ruleId, profile.ruleId, `${caseId}: dataset rule drift`);
    assert.equal(record.ruleFamily, profile.ruleFamily, `${caseId}: dataset family drift`);
  }
  assert.deepEqual(
    profile.branches.map((branch) => branch.branchType).sort(),
    ['CONTEXT_DEPENDENT_TEXT', 'DIRECT_TEXT'],
    `${profile.profileId}: direct/context burden coverage is required`,
  );
}
for (const exclusion of spec.exclusions) {
  assert.ok(
    backendCatalogSource.includes(`"${exclusion.ruleId}"`),
    `${exclusion.ruleId} exclusion is not traceable to Backend catalog source`,
  );
}

const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
const executeAssembler = async (promptPilotSpec, requestContext) => {
  const execute = new AsyncFunction(
    '$json',
    `${assemblerSource}\n//# sourceURL=assemble-prompt-pilot-s2-04.js`,
  );
  return execute({promptPilotSpec, requestContext});
};
const clone = (value) => structuredClone(value);
const outputs = [];
for (const fixture of fixtures.cases) {
  const result = await executeAssembler(spec, fixture.requestContext);
  assert.ok(Array.isArray(result) && result.length === 1, `${fixture.caseId}: invalid node output`);
  const output = result[0].json;
  assert.equal(output.candidateVersion, spec.candidateVersion);
  assert.equal(output.lifecycle, 'PROPOSED');
  assert.equal(output.runtimeAuthority, false);
  assert.equal(output.automationMode, 'A0_RECOMMEND_ONLY');
  assert.equal(output.providerCalled, false);
  assert.deepEqual(output.selectedRuleIds, ['CSR.HAR.001']);
  assert.equal(output.selectedProfiles.length, 1);
  assert.equal(output.selectedProfiles[0].branchType, fixture.expectedBranch);
  assert.equal(output.selectedProfiles[0].missingBehavior, 'NEEDS_MANUAL_REVIEW');
  assert.equal(output.selectedProfiles[0].targetActionCeiling, 'NO_ACTION');
  assert.equal(output.selectedProfiles[0].actionAuthority, false);
  assert.equal(output.userProjectionBoundary.separateFromSystemPrompt, true);
  assert.ok(output.systemPrompt.length <= spec.maximumSystemPromptCharacters);
  assert.ok(output.systemPrompt.includes('AI explanation is not evidence'));
  assert.ok(output.systemPrompt.includes('not a probability, confidence score, or risk score'));
  assert.ok(output.systemPrompt.includes('Missing, stale, unusable, or materially conflicting'));
  assert.ok(output.systemPrompt.includes('evidenceIds and counterEvidenceIds'));
  assert.ok(!output.systemPrompt.includes('supportingEvidenceIds'));
  assert.ok(!output.systemPrompt.includes(fixture.requestContext.reportClaim.description));
  assert.ok(!output.systemPrompt.includes(fixture.requestContext.targetSnapshot.contentText));
  assert.ok(!output.systemPrompt.includes(fixture.requestContext.priorAiOutput));
  assert.ok(!output.systemPrompt.includes('CSR.HAR.999'));
  outputs.push(output);
}
assert.equal(fixtures.cases.length, 2, 'pilot requires exactly two bounded executable fixtures');

const directFixture = fixtures.cases.find((item) => item.expectedBranch === 'DIRECT_TEXT');
const baselineOutput = (await executeAssembler(spec, directFixture.requestContext))[0].json;
for (const [field, attack, marker] of [
  ['reportClaim', {description: 'ATTACK-REPORT-CLAIM-7091'}, 'ATTACK-REPORT-CLAIM-7091'],
  ['targetSnapshot', {contentText: 'ATTACK-TARGET-TEXT-7092'}, 'ATTACK-TARGET-TEXT-7092'],
  [
    'evidence',
    [{payload: {sanitizedText: 'ATTACK-EVIDENCE-7093'}}],
    'ATTACK-EVIDENCE-7093',
  ],
  ['priorAiOutput', 'ATTACK-PRIOR-AI-7094', 'ATTACK-PRIOR-AI-7094'],
]) {
  const mutated = clone(directFixture.requestContext);
  mutated[field] = attack;
  const mutationOutput = (await executeAssembler(spec, mutated))[0].json;
  assert.equal(
    mutationOutput.systemPrompt,
    baselineOutput.systemPrompt,
    `${field} changed the trusted system prompt`,
  );
  assert.ok(!mutationOutput.systemPrompt.includes(marker));
}
const repeatedOutput = (await executeAssembler(spec, clone(directFixture.requestContext)))[0].json;
assert.equal(repeatedOutput.systemPrompt, baselineOutput.systemPrompt, 'assembler is not deterministic');

const unknownRuleRequest = clone(directFixture.requestContext);
unknownRuleRequest.policyContext.candidateRules[0].ruleId = 'CSR.SPAM.001';
unknownRuleRequest.policyContext.candidateRules[0].ruleFamily = 'SPAM';
const unknownRuleOutput = (await executeAssembler(spec, unknownRuleRequest))[0].json;
assert.deepEqual(unknownRuleOutput.selectedRuleIds, []);
assert.ok(unknownRuleOutput.systemPrompt.includes('PILOT-NOT-APPLICABLE'));
assert.ok(!unknownRuleOutput.systemPrompt.includes('CSR.SPAM.001'));

const expectAssemblerReject = async (mutator, label) => {
  const candidateSpec = clone(spec);
  const candidateRequest = clone(directFixture.requestContext);
  mutator(candidateSpec, candidateRequest);
  await assert.rejects(
    () => executeAssembler(candidateSpec, candidateRequest),
    /S2-04 prompt pilot rejected:/,
    label,
  );
};
await expectAssemblerReject(
  (candidateSpec) => {
    candidateSpec.runtimeAuthority = true;
  },
  'runtime authority mutation must fail',
);
await expectAssemblerReject(
  (_, request) => {
    request.automationMode = 'A1_AUTO_RESOLVE';
  },
  'automation expansion must fail',
);
await expectAssemblerReject(
  (_, request) => {
    request.policyContext.candidateRules[0].ruleType = 'ROUTING';
  },
  'signed rule semantic mismatch must fail',
);
await expectAssemblerReject(
  (candidateSpec) => {
    candidateSpec.selectedProfiles[0].requiredSemanticCodes.push('SEM-INVENTED');
  },
  'semantic expansion must fail',
);
await expectAssemblerReject(
  (candidateSpec) => {
    candidateSpec.selectedProfiles[0].branches[0].requiredContextKinds.push('PARENT_COMMENT_CONTEXT');
  },
  'context expansion must fail',
);

const invalidSpecMutations = [
  (candidate) => {
    candidate.runtimeAuthority = true;
  },
  (candidate) => {
    candidate.lifecycle = 'ACTIVE';
  },
  (candidate) => {
    candidate.unknown = true;
  },
  (candidate) => {
    candidate.selectedProfiles[0].actionAuthority = true;
  },
  (candidate) => {
    candidate.baseSafetyClauseCodes.pop();
  },
  (candidate) => {
    candidate.selectedProfiles[0].ruleId = 'CSR.HAR.999';
  },
];
for (const mutate of invalidSpecMutations) {
  const candidate = clone(spec);
  mutate(candidate);
  assert.ok(specErrors(candidate).length > 0, 'invalid spec mutation passed schema');
}

const promptDigest = crypto
  .createHash('sha256')
  .update(outputs.map((output) => output.systemPrompt).join('\n---\n'))
  .digest('hex');
console.log('S2_04_PROMPT_SPEC_SCHEMA=PASS (1/1)');
console.log(`S2_04_SELECTED_PROFILES=PASS (${spec.selectedProfiles.length}/1)`);
console.log(`S2_04_EXECUTABLE_BRANCHES=PASS (${fixtures.cases.length}/2)`);
console.log('S2_04_UNTRUSTED_SYSTEM_PROMPT_MUTATIONS=PASS (4/4)');
console.log('S2_04_ASSEMBLER_NEGATIVE_GUARDS=PASS (5/5)');
console.log(`S2_04_SPEC_NEGATIVE_GUARDS=PASS (${invalidSpecMutations.length}/6)`);
console.log(`S2_04_DATASET_REFS=PASS (${spec.selectedProfiles[0].datasetCaseRefs.length}/5)`);
console.log(`S2_04_PROMPT_DIGEST=${promptDigest}`);
console.log('S2_04_PROVIDER_CALLED=false');
console.log('S2_04_RUNTIME_AUTHORITY=false');
console.log('S2_04_PROMPT_PILOT=PASS');
