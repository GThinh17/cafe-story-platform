import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";
import { fileURLToPath } from "node:url";

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url));
const repositoryRoot = path.resolve(scriptDirectory, "../../../../../..");
const require = createRequire(import.meta.url);
const Ajv2020 = require(
  path.join(repositoryRoot, "2-cafe-story-nextjs-web/node_modules/ajv/dist/2020")
).default;
const addFormats = require(
  path.join(repositoryRoot, "2-cafe-story-nextjs-web/node_modules/ajv-formats")
).default;

const readJson = (filePath) =>
  JSON.parse(fs.readFileSync(filePath, { encoding: "utf8" }));
const clone = (value) => JSON.parse(JSON.stringify(value));
const unique = (values) => [...new Set(values)];
const sorted = (values) => [...values].sort();

function decodePointerSegment(segment) {
  return segment.replaceAll("~1", "/").replaceAll("~0", "~");
}

function resolvePointer(document, pointer, allowAppend = false) {
  if (!pointer.startsWith("/")) {
    throw new Error(`JSON Pointer không hợp lệ: ${pointer}`);
  }

  const segments = pointer
    .slice(1)
    .split("/")
    .map(decodePointerSegment);
  const last = segments.pop();
  let parent = document;

  for (const segment of segments) {
    const key = Array.isArray(parent) ? Number(segment) : segment;
    if (parent[key] === undefined) {
      throw new Error(`Không tìm thấy JSON Pointer segment: ${pointer}`);
    }
    parent = parent[key];
  }

  if (last === "-" && allowAppend && Array.isArray(parent)) {
    return { parent, key: parent.length };
  }
  return { parent, key: Array.isArray(parent) ? Number(last) : last };
}

function applyOperations(base, operations) {
  const document = clone(base);
  for (const operation of operations) {
    const { parent, key } = resolvePointer(
      document,
      operation.path,
      operation.op === "add"
    );
    if (operation.op === "add") {
      if (Array.isArray(parent)) {
        parent.splice(key, 0, clone(operation.value));
      } else {
        parent[key] = clone(operation.value);
      }
    } else if (operation.op === "replace") {
      if (parent[key] === undefined) {
        throw new Error(`replace target không tồn tại: ${operation.path}`);
      }
      parent[key] = clone(operation.value);
    } else if (operation.op === "remove") {
      if (Array.isArray(parent)) {
        parent.splice(key, 1);
      } else {
        delete parent[key];
      }
    } else {
      throw new Error(`Operation không hỗ trợ: ${operation.op}`);
    }
  }
  return document;
}

const requiredKindsByTarget = {
  BLOG: [
    "TARGET_IDENTITY",
    "REPORT_TARGET_ASSOCIATION",
    "TARGET_STATE",
    "TARGET_TEXT_CONTENT",
    "TARGET_ACTOR_ASSOCIATION",
    "TARGET_PAGE_ASSOCIATION",
    "TARGET_REGION_ASSOCIATION"
  ],
  COMMENT: [
    "TARGET_IDENTITY",
    "REPORT_TARGET_ASSOCIATION",
    "TARGET_STATE",
    "TARGET_TEXT_CONTENT",
    "TARGET_ACTOR_ASSOCIATION",
    "TARGET_PAGE_ASSOCIATION",
    "PARENT_BLOG_CONTEXT",
    "PARENT_COMMENT_CONTEXT"
  ],
  USER: [
    "TARGET_IDENTITY",
    "REPORT_TARGET_ASSOCIATION",
    "TARGET_STATE",
    "TARGET_PUBLIC_PROFILE_CONTEXT",
    "TARGET_DEEP_CONTEXT"
  ],
  CAFE_PAGE: [
    "TARGET_IDENTITY",
    "REPORT_TARGET_ASSOCIATION",
    "TARGET_STATE",
    "TARGET_PUBLIC_PROFILE_CONTEXT",
    "PAGE_OWNER_ASSOCIATION",
    "TARGET_REGION_ASSOCIATION",
    "TARGET_DEEP_CONTEXT"
  ]
};

const kindTargets = {
  TARGET_IDENTITY: ["BLOG", "COMMENT", "USER", "CAFE_PAGE"],
  REPORT_TARGET_ASSOCIATION: ["BLOG", "COMMENT", "USER", "CAFE_PAGE"],
  TARGET_STATE: ["BLOG", "COMMENT", "USER", "CAFE_PAGE"],
  TARGET_TEXT_CONTENT: ["BLOG", "COMMENT"],
  TARGET_MEDIA_REFERENCE: ["BLOG", "COMMENT", "USER", "CAFE_PAGE"],
  VERIFIED_MEDIA_OBSERVATION: ["BLOG", "COMMENT"],
  TARGET_ACTOR_ASSOCIATION: ["BLOG", "COMMENT"],
  TARGET_PAGE_ASSOCIATION: ["BLOG", "COMMENT"],
  TARGET_REGION_ASSOCIATION: ["BLOG", "CAFE_PAGE"],
  PARENT_BLOG_CONTEXT: ["COMMENT"],
  PARENT_COMMENT_CONTEXT: ["COMMENT"],
  TARGET_PUBLIC_PROFILE_CONTEXT: ["USER", "CAFE_PAGE"],
  PAGE_OWNER_ASSOCIATION: ["CAFE_PAGE"],
  TARGET_DEEP_CONTEXT: ["USER", "CAFE_PAGE"]
};

const kindSources = {
  TARGET_IDENTITY: ["PLATFORM_RECORD", "TARGET_SNAPSHOT"],
  REPORT_TARGET_ASSOCIATION: ["PLATFORM_RECORD"],
  TARGET_STATE: ["PLATFORM_RECORD", "TARGET_SNAPSHOT"],
  TARGET_TEXT_CONTENT: ["PLATFORM_RECORD", "TARGET_SNAPSHOT"],
  TARGET_MEDIA_REFERENCE: ["PLATFORM_RECORD", "TARGET_SNAPSHOT"],
  VERIFIED_MEDIA_OBSERVATION: ["VERIFIED_MEDIA_OBSERVATION"],
  TARGET_ACTOR_ASSOCIATION: ["PLATFORM_RECORD", "TARGET_SNAPSHOT"],
  TARGET_PAGE_ASSOCIATION: ["PLATFORM_RECORD", "TARGET_SNAPSHOT"],
  TARGET_REGION_ASSOCIATION: ["PLATFORM_RECORD", "TARGET_SNAPSHOT"],
  PARENT_BLOG_CONTEXT: ["PLATFORM_RECORD", "TARGET_SNAPSHOT"],
  PARENT_COMMENT_CONTEXT: ["PLATFORM_RECORD", "TARGET_SNAPSHOT"],
  TARGET_PUBLIC_PROFILE_CONTEXT: ["PLATFORM_RECORD", "TARGET_SNAPSHOT"],
  PAGE_OWNER_ASSOCIATION: ["PLATFORM_RECORD", "TARGET_SNAPSHOT"],
  TARGET_DEEP_CONTEXT: ["PLATFORM_RECORD"]
};

const candidateKinds = new Set([
  "TARGET_TEXT_CONTENT",
  "VERIFIED_MEDIA_OBSERVATION"
]);
const reservedKinds = new Set([
  "VERIFIED_MEDIA_OBSERVATION",
  "TARGET_DEEP_CONTEXT"
]);
const missingStatuses = new Set([
  "MISSING",
  "UNREADABLE",
  "INACCESSIBLE",
  "NOT_COLLECTED"
]);
const forbiddenFieldNames = new Set([
  "authorization",
  "cookie",
  "set-cookie",
  "password",
  "userpassword",
  "email",
  "useremail",
  "phone",
  "userphone",
  "roles",
  "accesstoken",
  "refreshtoken",
  "token",
  "session",
  "sessions",
  "apikey",
  "secret",
  "opensai_api_key",
  "openai_api_key",
  "riskscore",
  "violationprobability"
]);

function containsForbiddenField(value) {
  if (Array.isArray(value)) {
    return value.some(containsForbiddenField);
  }
  if (value === null || typeof value !== "object") {
    return false;
  }
  return Object.entries(value).some(
    ([key, nestedValue]) =>
      forbiddenFieldNames.has(key.toLowerCase()) ||
      containsForbiddenField(nestedValue)
  );
}

function semanticValidate(instance) {
  const errors = [];
  const add = (code) => errors.push(code);
  const target = instance.targetSnapshot;
  const targetType = target.targetType;
  const evidenceIds = instance.evidence.map((item) => item.evidenceId);
  const evidenceIdSet = new Set(evidenceIds);
  const missingIds = instance.missingEvidence.map(
    (item) => item.missingEvidenceId
  );
  const missingIdSet = new Set(missingIds);
  const candidateRuleIds = new Set(instance.policyContext.candidateRuleIds);
  const ruleEvaluationIds = instance.ruleEvaluations.map((item) => item.ruleId);

  if (containsForbiddenField(instance)) {
    add("FORBIDDEN_FIELD_PRESENT");
  }

  if (unique(evidenceIds).length !== evidenceIds.length) {
    add("DUPLICATE_EVIDENCE_ID");
  }
  if (unique(missingIds).length !== missingIds.length) {
    add("DUPLICATE_MISSING_EVIDENCE_ID");
  }
  if (unique(ruleEvaluationIds).length !== ruleEvaluationIds.length) {
    add("DUPLICATE_RULE_EVALUATION");
  }
  if (
    sorted(unique(ruleEvaluationIds)).join("|") !==
    sorted([...candidateRuleIds]).join("|")
  ) {
    add("RULE_EVALUATION_COVERAGE_MISMATCH");
  }

  if (!target.reportTargetAssociation.associationValidated) {
    add("REPORT_TARGET_ASSOCIATION_INVALID");
  }
  if (
    target.reportTargetAssociation.targetAlias !== target.targetAlias
  ) {
    add("REPORT_TARGET_ALIAS_MISMATCH");
  }

  for (const requiredKind of requiredKindsByTarget[targetType] ?? []) {
    const count = instance.evidence.filter(
      (item) => item.evidenceKind === requiredKind
    ).length;
    if (count !== 1) {
      add(`STRUCTURAL_CARDINALITY_${requiredKind}`);
    }
  }

  for (const item of instance.evidence) {
    if (
      item.subject.targetType !== targetType ||
      item.subject.targetAlias !== target.targetAlias ||
      item.subject.snapshotVersion !== target.snapshotVersion ||
      item.subject.snapshotHash !== target.snapshotHash
    ) {
      add("EVIDENCE_SUBJECT_MISMATCH");
    }
    if (!(kindTargets[item.evidenceKind] ?? []).includes(targetType)) {
      add("EVIDENCE_KIND_TARGET_INCOMPATIBLE");
    }
    if (!(kindSources[item.evidenceKind] ?? []).includes(item.source.sourceType)) {
      add("EVIDENCE_KIND_SOURCE_INCOMPATIBLE");
    }
    const expectedUse = candidateKinds.has(item.evidenceKind)
      ? "RULE_EVALUATION_CANDIDATE"
      : "CONTEXT_ONLY";
    if (item.intendedUse !== expectedUse) {
      add("EVIDENCE_KIND_INTENDED_USE_INVALID");
    }
    if (
      item.collectedForRuleIds.some((ruleId) => !candidateRuleIds.has(ruleId))
    ) {
      add("COLLECTED_FOR_RULE_NOT_CANDIDATE");
    }

    const status = item.availability.status;
    const representation = item.payload.representation;
    if (status === "AVAILABLE") {
      if (item.availability.reasonCode !== null) {
        add("AVAILABLE_HAS_REASON_CODE");
      }
      if (representation === "NONE" || item.integrity.payloadDigest === null) {
        add("AVAILABLE_PAYLOAD_INVALID");
      }
    } else {
      if (!item.availability.reasonCode) {
        add("UNAVAILABLE_REASON_REQUIRED");
      }
      if (missingStatuses.has(status) && representation !== "NONE") {
        add("UNAVAILABLE_PAYLOAD_MUST_BE_NONE");
      }
    }

    if (
      reservedKinds.has(item.evidenceKind) &&
      ["AVAILABLE", "PARTIAL"].includes(status)
    ) {
      add("RESERVED_KIND_AVAILABLE");
    }
  }

  for (const evaluation of instance.ruleEvaluations) {
    for (const evidenceId of [
      ...evaluation.supportingEvidenceIds,
      ...evaluation.counterEvidenceIds
    ]) {
      if (!evidenceIdSet.has(evidenceId)) {
        add("RULE_EVALUATION_UNKNOWN_EVIDENCE_REF");
      }
    }
    for (const missingId of evaluation.missingEvidenceIds) {
      if (!missingIdSet.has(missingId)) {
        add("RULE_EVALUATION_UNKNOWN_MISSING_REF");
      }
    }
    if (
      evaluation.supportingEvidenceIds.some((evidenceId) => {
        const item = instance.evidence.find(
          (candidate) => candidate.evidenceId === evidenceId
        );
        return item?.intendedUse !== "RULE_EVALUATION_CANDIDATE";
      })
    ) {
      add("CONTEXT_ONLY_USED_AS_SUPPORT");
    }
  }

  for (const missing of instance.missingEvidence) {
    const isKindRequirement = missing.requirementType === "EVIDENCE_KIND";
    const isSemanticRequirement =
      missing.requirementType === "SEMANTIC_REQUIREMENT";
    if (
      isKindRequirement &&
      (missing.requiredEvidenceKind === null ||
        missing.semanticRequirementCode !== null)
    ) {
      add("MISSING_REQUIREMENT_KIND_SHAPE_INVALID");
    }
    if (
      isSemanticRequirement &&
      (missing.requiredEvidenceKind !== null ||
        missing.semanticRequirementCode === null)
    ) {
      add("MISSING_REQUIREMENT_SEMANTIC_SHAPE_INVALID");
    }
  }

  for (const conflict of instance.conflicts) {
    if (conflict.evidenceIds.some((id) => !evidenceIdSet.has(id))) {
      add("CONFLICT_UNKNOWN_EVIDENCE_REF");
    }
  }

  for (const finding of instance.recommendation.findings) {
    if (
      finding.evidenceIds.some((evidenceId) => !evidenceIdSet.has(evidenceId))
    ) {
      add("FINDING_UNKNOWN_EVIDENCE_REF");
    }
    if (
      finding.evidenceIds.some((evidenceId) => {
        const item = instance.evidence.find(
          (candidate) => candidate.evidenceId === evidenceId
        );
        return item?.intendedUse !== "RULE_EVALUATION_CANDIDATE";
      })
    ) {
      add("FINDING_CONTEXT_ONLY_AS_PROOF");
    }
  }

  const policyActive =
    instance.policyContext.policyStatus === "ACTIVE" &&
    instance.policyContext.ruleCatalogStatus === "ACTIVE";
  const recommendation = instance.recommendation;

  if (!policyActive) {
    const policyStopValid =
      instance.providerExecution.eligible === false &&
      instance.providerExecution.called === false &&
      instance.providerExecution.route === "POLICY_VALIDATION_STOP" &&
      recommendation.operationalStatus === "POLICY_INVALID" &&
      recommendation.reportDecision === null &&
      recommendation.candidateTargetAction === "NO_ACTION" &&
      recommendation.findings.length === 0 &&
      instance.ruleEvaluations.every(
        (evaluation) => evaluation.outcome === "POLICY_INVALID"
      );
    if (!policyStopValid) {
      add("INACTIVE_POLICY_CREATED_CONTENT_DECISION");
    }
    return unique(errors);
  }

  if (recommendation.operationalStatus !== "VALID") {
    add("ACTIVE_POLICY_OPERATIONAL_STATUS_INVALID");
  }

  const manualTarget = ["USER", "CAFE_PAGE"].includes(targetType);
  if (manualTarget) {
    const clampValid =
      instance.providerExecution.eligible === false &&
      instance.providerExecution.called === false &&
      instance.providerExecution.route === "LOCAL_MANUAL_ONLY" &&
      recommendation.reportDecision === "NEEDS_MANUAL_REVIEW" &&
      recommendation.candidateTargetAction === "NO_ACTION" &&
      recommendation.findings.length === 0;
    if (!clampValid) {
      add("MANUAL_TARGET_CLAMP_VIOLATION");
    }
  }

  const criticalMissing = instance.missingEvidence.some(
    (missing) => missing.critical
  );
  const materialConflict = instance.conflicts.some(
    (conflict) => conflict.material
  );
  const materialUnassessable = instance.ruleEvaluations.some(
    (evaluation) =>
      evaluation.material &&
      ["UNASSESSABLE", "CONFLICTED"].includes(evaluation.outcome)
  );
  const manualBlocker =
    manualTarget || criticalMissing || materialConflict || materialUnassessable;

  if (
    manualBlocker &&
    (recommendation.reportDecision !== "NEEDS_MANUAL_REVIEW" ||
      recommendation.candidateTargetAction !== "NO_ACTION")
  ) {
    add("MANUAL_BLOCKER_DECISION_VIOLATION");
  }

  if (recommendation.reportDecision === "RESOLVE") {
    const substantiated = instance.ruleEvaluations.some(
      (evaluation) =>
        evaluation.material && evaluation.outcome === "SUBSTANTIATED"
    );
    if (!["BLOG", "COMMENT"].includes(targetType)) {
      add("RESOLVE_TARGET_NOT_ELIGIBLE");
    }
    if (!substantiated) {
      add("RESOLVE_WITHOUT_SUBSTANTIATED_RULE");
    }
    if (manualBlocker) {
      add("RESOLVE_WITH_MANUAL_BLOCKER");
    }
    if (recommendation.findings.length === 0) {
      add("RESOLVE_WITHOUT_FINDING");
    }
  }

  if (recommendation.reportDecision === "REJECT") {
    if (!["BLOG", "COMMENT"].includes(targetType)) {
      add("REJECT_TARGET_NOT_ELIGIBLE");
    }
    if (!instance.scopeEvaluation.complete) {
      add("REJECT_SCOPE_INCOMPLETE");
    }
    if (
      !instance.scopeEvaluation.evidenceControlRuleIds.includes("CSR.EVD.004")
    ) {
      add("REJECT_EVD004_MISSING");
    }
    if (
      instance.ruleEvaluations.some(
        (evaluation) =>
          evaluation.material &&
          !["NOT_SUBSTANTIATED", "NOT_APPLICABLE"].includes(
            evaluation.outcome
          )
      )
    ) {
      add("REJECT_MATERIAL_RULE_NOT_CLEARED");
    }
    if (criticalMissing || materialConflict) {
      add("REJECT_WITH_MISSING_OR_CONFLICT");
    }
    if (recommendation.candidateTargetAction !== "KEEP_VISIBLE") {
      add("REJECT_ACTION_NOT_KEEP_VISIBLE");
    }
    if (recommendation.findings.length !== 0) {
      add("REJECT_HAS_VIOLATION_FINDING");
    }
  }

  if (
    recommendation.reportDecision === "NEEDS_MANUAL_REVIEW" &&
    recommendation.candidateTargetAction !== "NO_ACTION"
  ) {
    add("MANUAL_REVIEW_ACTION_NOT_NO_ACTION");
  }

  return unique(errors);
}

const manifest = readJson(path.join(scriptDirectory, "contract-manifest.json"));
const schema = readJson(path.join(scriptDirectory, manifest.schema));
const ajv = new Ajv2020({
  allErrors: true,
  strict: true
});
addFormats(ajv);
const validateSchema = ajv.compile(schema);

const actualCaseFiles = fs
  .readdirSync(path.join(scriptDirectory, manifest.fixtureDirectory))
  .filter((name) => name.endsWith(".json"))
  .sort();
const declaredCaseFiles = [...manifest.cases].sort();
const manifestMatchesFiles =
  JSON.stringify(actualCaseFiles) === JSON.stringify(declaredCaseFiles);

const results = [];
let allPassed = manifestMatchesFiles;

for (const caseFile of manifest.cases) {
  const caseDefinition = readJson(
    path.join(scriptDirectory, manifest.fixtureDirectory, caseFile)
  );
  const base = readJson(
    path.join(scriptDirectory, "fixtures/bases", caseDefinition.base)
  );
  const instance = applyOperations(base, caseDefinition.operations);
  const schemaValid = validateSchema(instance);
  const schemaErrors = schemaValid
    ? []
    : (validateSchema.errors ?? []).map((error) => ({
        instancePath: error.instancePath,
        keyword: error.keyword,
        message: error.message
      }));
  const semanticErrors = schemaValid ? semanticValidate(instance) : [];
  const semanticValid = schemaValid ? semanticErrors.length === 0 : null;
  const actualErrorCodes = schemaValid
    ? sorted(semanticErrors)
    : ["SCHEMA_INVALID"];
  const expectedErrorCodes = sorted(caseDefinition.expected.errorCodes);
  const pass =
    schemaValid === caseDefinition.expected.schemaValid &&
    semanticValid === caseDefinition.expected.semanticValid &&
    JSON.stringify(actualErrorCodes) === JSON.stringify(expectedErrorCodes);

  allPassed = allPassed && pass;
  results.push({
    id: caseDefinition.id,
    file: caseFile,
    schemaValid,
    semanticValid,
    expectedErrorCodes,
    actualErrorCodes,
    schemaErrors,
    pass
  });
}

const summary = {
  contractVersion: manifest.contractVersion,
  schemaVersion: manifest.schemaVersion,
  lifecycle: manifest.lifecycle,
  runtimeAuthority: manifest.runtimeAuthority,
  validator: {
    engine: "Ajv",
    version: require(
      path.join(repositoryRoot, "2-cafe-story-nextjs-web/node_modules/ajv/package.json")
    ).version,
    draft: "2020-12",
    strict: true
  },
  manifestMatchesFiles,
  requiredScenarioCount: manifest.requiredScenarioCount,
  actualScenarioCount: results.length,
  passed: results.filter((result) => result.pass).length,
  failed: results.filter((result) => !result.pass).length,
  allPassed,
  results
};

process.stdout.write(`${JSON.stringify(summary, null, 2)}\n`);
process.exitCode = allPassed ? 0 : 1;
