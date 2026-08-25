import fs from "node:fs";
import path from "node:path";
import { execFileSync } from "node:child_process";
import { fileURLToPath } from "node:url";

const contractDirectory = path.dirname(fileURLToPath(import.meta.url));
const dossierRoot = path.resolve(contractDirectory, "../../..");
const readText = (relativePath) =>
  fs.readFileSync(path.join(dossierRoot, relativePath), "utf8");
const readJson = (filePath) =>
  JSON.parse(fs.readFileSync(filePath, { encoding: "utf8" }));
const sameSet = (left, right) =>
  JSON.stringify([...new Set(left)].sort()) ===
  JSON.stringify([...new Set(right)].sort());

const documents = {
  m01: "05-evidence-standard/common-evidence-envelope.md",
  m02: "08-target-evidence/blog-metadata-schema.md",
  m03: "08-target-evidence/comment-metadata-schema.md",
  m04: "08-target-evidence/user-cafe-page-manual-only-metadata-schema.md",
  m05: "05-evidence-standard/evidence-kind-catalog.md",
  m06: "05-evidence-standard/rule-to-evidence-requirement-matrix.md",
  m07: "05-evidence-standard/evidence-metadata-contract-v2.md",
  ruleCatalog: "03-rule-catalog/rule-catalog-overview.md",
  policyVersioning: "02-policy-framework/policy-versioning.md"
};
const texts = Object.fromEntries(
  Object.entries(documents).map(([key, relativePath]) => [
    key,
    readText(relativePath)
  ])
);
const schema = readJson(
  path.join(contractDirectory, "evidence-metadata-contract.schema.json")
);
const manifest = readJson(path.join(contractDirectory, "contract-manifest.json"));
const fixtureValidation = JSON.parse(
  execFileSync(process.execPath, [path.join(contractDirectory, "validate-fixtures.mjs")], {
    encoding: "utf8"
  })
);

const catalogRuleIds = [
  ...texts.ruleCatalog.matchAll(/CSR\.[A-Z]+\.\d{3}/g)
].map((match) => match[0]);
const catalogKinds = [
  ...texts.m05.matchAll(/^\| `EK-\d{2}` \| `([A-Z_]+)` \|/gm)
].map((match) => match[1]);
const semanticCodes = [
  ...texts.m06.matchAll(/^\| `(SEM-[A-Z-]+)` \|/gm)
].map((match) => match[1]);
const legacyExamplePattern =
  /"evidenceKind"\s*:\s*"(CONTENT_SNAPSHOT|MEDIA_OBSERVATION|PARENT_CONTEXT_SNAPSHOT)"/;
const lifecycle = [
  "DRAFT",
  "PROPOSED",
  "APPROVED",
  "ACTIVE",
  "DEPRECATED",
  "RETIRED"
];
const caseDirectory = path.join(contractDirectory, manifest.fixtureDirectory);
const caseFiles = fs
  .readdirSync(caseDirectory)
  .filter((name) => name.endsWith(".json"))
  .sort();
const sourceProperties = schema.$defs.evidenceItem.properties.source.properties;
const userContext =
  schema.$defs.userSnapshot.allOf[1].properties.data.properties
    .publicProfileContext;
const pageData = schema.$defs.cafePageSnapshot.allOf[1].properties.data;

const checks = {
  m01ToM07Approved: ["m01", "m02", "m03", "m04", "m05", "m06", "m07"].every(
    (key) => /\| Trạng thái \| `APPROVED` \|/.test(texts[key])
  ),
  noLegacyEvidenceKindInExamples: ["m01", "m02", "m03", "m04"].every(
    (key) => !legacyExamplePattern.test(texts[key])
  ),
  ruleIdsExact24:
    new Set(catalogRuleIds).size === 24 &&
    sameSet(catalogRuleIds, schema.$defs.ruleId.enum),
  evidenceKindsExact14:
    new Set(catalogKinds).size === 14 &&
    sameSet(catalogKinds, schema.$defs.evidenceKind.enum),
  semanticRequirementsExact15:
    new Set(semanticCodes).size === 15 &&
    sameSet(semanticCodes, schema.$defs.semanticRequirementCode.enum),
  lifecycleExact:
    sameSet(schema.$defs.policyContext.properties.policyStatus.enum, lifecycle) &&
    sameSet(
      schema.$defs.policyContext.properties.ruleCatalogStatus.enum,
      lifecycle
    ),
  userContextClosed: userContext.additionalProperties === false,
  pageContextClosed:
    pageData.properties.ownershipContext.additionalProperties === false &&
    pageData.properties.publicPageContext.additionalProperties === false,
  provenanceFieldsPresent: [
    "sourceEntityType",
    "sourceEntityAlias",
    "sourceFieldPath"
  ].every((field) => Object.hasOwn(sourceProperties, field)),
  confidenceRequiredNullable:
    schema.$defs.recommendation.required.includes("assessmentConfidence") &&
    schema.$defs.recommendation.properties.assessmentConfidence.type.includes(
      "null"
    ) &&
    /bắt buộc nhưng nullable/.test(texts.m07),
  manifestMatchesCaseFiles: sameSet(manifest.cases, caseFiles),
  minimumFixtureCoverage:
    manifest.requiredScenarioCount === 15 && caseFiles.length >= 15,
  privacyNegativeFixtureRegistered: manifest.cases.includes(
    "17-forbidden-nested-field-invalid.json"
  ),
  retiredLifecycleFixtureRegistered: manifest.cases.includes(
    "18-retired-policy-stops-evaluation.json"
  ),
  fixtureRegressionPassed:
    fixtureValidation.allPassed === true &&
    fixtureValidation.failed === 0 &&
    fixtureValidation.passed === caseFiles.length,
  contractNotActivated:
    ["PROPOSED", "APPROVED"].includes(manifest.lifecycle) &&
    manifest.runtimeAuthority === false
};

const failedChecks = Object.entries(checks)
  .filter(([, passed]) => !passed)
  .map(([name]) => name);
const result = {
  gate: "G0-12M-08",
  contractVersion: manifest.contractVersion,
  checks,
  checkCount: Object.keys(checks).length,
  passedCheckCount: Object.values(checks).filter(Boolean).length,
  failedCheckCount: failedChecks.length,
  failedChecks,
  counts: {
    ruleIds: new Set(catalogRuleIds).size,
    evidenceKinds: new Set(catalogKinds).size,
    semanticRequirements: new Set(semanticCodes).size,
    targets: 4,
    fixtures: caseFiles.length,
    fixturePasses: fixtureValidation.passed
  },
  lifecycle: manifest.lifecycle,
  runtimeAuthority: manifest.runtimeAuthority,
  allPassed: failedChecks.length === 0
};

process.stdout.write(`${JSON.stringify(result, null, 2)}\n`);
process.exitCode = result.allPassed ? 0 : 1;
