import assert from "node:assert/strict";
import crypto from "node:crypto";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const endpoint =
  process.env.ADMIN_TRANSLATION_WEBHOOK_URL ||
  "http://127.0.0.1:5678/webhook/cafestory-admin-content-translation";
const envText = readFileSync(path.join(root, "docker/.env"), "utf8");

function envValue(name) {
  const escaped = name.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  const matches = [...envText.matchAll(new RegExp(`^\\s*${escaped}\\s*=\\s*(.*)$`, "gm"))];
  assert.ok(matches.length <= 1, `Duplicate ${name} entries`);
  return matches[0]?.[1]?.trim().replace(/^(['"])(.*)\1$/, "$2") ?? "";
}

const secret = envValue("ADMIN_REPORT_AI_HMAC_SECRET");
assert.ok(secret, "ADMIN_REPORT_AI_HMAC_SECRET is required");

function canonicalize(value) {
  if (Array.isArray(value)) return `[${value.map(canonicalize).join(",")}]`;
  if (value && typeof value === "object") {
    return `{${Object.keys(value)
      .sort()
      .map((key) => `${JSON.stringify(key)}:${canonicalize(value[key])}`)
      .join(",")}}`;
  }
  if (
    value === null ||
    typeof value === "string" ||
    typeof value === "boolean" ||
    (typeof value === "number" && Number.isFinite(value))
  ) {
    return JSON.stringify(value);
  }
  throw new Error("Payload is not valid JCS JSON");
}

function sha256(value) {
  return crypto.createHash("sha256").update(value, "utf8").digest("hex");
}

function requestHeaders(body) {
  const timestamp = String(Math.floor(Date.now() / 1000));
  const nonce = crypto.randomUUID();
  const bodyHash = sha256(canonicalize(body));
  const signature = crypto
    .createHmac("sha256", secret)
    .update(`${timestamp}\n${nonce}\n${bodyHash}`, "utf8")
    .digest("hex");
  return {
    "content-type": "application/json",
    "x-cafestory-contract-version": body.contractVersion,
    "x-cafestory-correlation-id": body.requestId,
    "x-cafestory-timestamp": timestamp,
    "x-cafestory-nonce": nonce,
    "x-cafestory-body-sha256": bodyHash,
    "x-cafestory-signature": signature,
  };
}

function protectedTokens(value) {
  const patterns = [
    /https?:\/\/[^\s<>()]+/gi,
    /\b[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\b/gi,
    /\b[A-Z][A-Z0-9]*(?:[._:-][A-Z0-9]+)+\b/g,
    /\b[A-Z][A-Z0-9_]{2,}\b/g,
    /\b\d{6,}\b/g,
  ];
  return patterns.flatMap((pattern) => value.match(pattern) || []).sort();
}

function verifySignedResponse(response, body) {
  const expectedKeys = [
    "detectedLocale",
    "modelName",
    "requestId",
    "targetLocale",
    "translatedText",
    "translationState",
  ].sort();
  assert.deepEqual(Object.keys(body).sort(), expectedKeys);
  const contractVersion = response.headers.get("x-cafestory-contract-version");
  const correlationId = response.headers.get("x-cafestory-correlation-id");
  const timestamp = response.headers.get("x-cafestory-timestamp");
  const nonce = response.headers.get("x-cafestory-nonce");
  const bodyHash = response.headers.get("x-cafestory-body-sha256");
  const signature = response.headers.get("x-cafestory-signature");
  assert.equal(contractVersion, "ADMIN-CONTENT-TRANSLATION-V1");
  assert.equal(correlationId, body.requestId);
  assert.ok(timestamp && nonce && bodyHash && signature, "Signed response headers are required");
  assert.equal(bodyHash, sha256(canonicalize(body)));
  const expectedSignature = crypto
    .createHmac("sha256", secret)
    .update(`${timestamp}\n${nonce}\n${bodyHash}`, "utf8")
    .digest("hex");
  assert.equal(signature, expectedSignature);
}

const cases = [
  {
    name: "English to Vietnamese",
    detectedLocale: "en",
    targetLocale: "vi",
    contentKind: "COMMENT_CONTENT",
    text:
      "This comment promises a guaranteed profit and asks users to open https://safe.example, share OTP, and keep ACTIVE CSR.INT.003 0f4d1f50-8db1-4ad9-8e58-0f66ae7fdf1a unchanged. 🙂",
    validateLanguage: (value) => /[ăâđêôơưáàảãạéèẻẽẹíìỉĩịóòỏõọúùủũụýỳỷỹỵ]/iu.test(value),
  },
  {
    name: "Vietnamese to English",
    detectedLocale: "vi",
    targetLocale: "en",
    contentKind: "BLOG_CONTENT",
    text:
      "Bình luận này hứa hẹn lợi nhuận bảo đảm và yêu cầu người dùng mở https://safe.example, chia sẻ OTP, đồng thời giữ nguyên ACTIVE CSR.INT.003 0f4d1f50-8db1-4ad9-8e58-0f66ae7fdf1a. 🙂",
    validateLanguage: (value) =>
      /\b(comment|guaranteed|profit|asks?|users?|open|share)\b/i.test(value) &&
      !/[ăâđêôơưáàảãạéèẻẽẹíìỉĩịóòỏõọúùủũụýỳỷỹỵ]/iu.test(value),
  },
  {
    name: "Identifier-heavy report description",
    detectedLocale: "en",
    targetLocale: "vi",
    contentKind: "REPORT_DESCRIPTION",
    text:
      "E2E Report Admin AI ACTIONS COMMENT 1786883671028 1d37d0c8-bcf0-40d6-a5b3-1bd2caf3927b needs review for suspicious behavior.",
    validateLanguage: (value) =>
      /[ăâđêôơưáàảãạéèẻẽẹíìỉĩịóòỏõọúùủũụýỳỷỹỵ]/iu.test(value) &&
      !/UNTRUSTED_TEXT_BEGIN|UNTRUSTED_TEXT_END|CONTENT_KIND=/i.test(value),
  },
];

const results = [];
for (const testCase of cases) {
  const body = {
    contractVersion: "ADMIN-CONTENT-TRANSLATION-V1",
    requestId: crypto.randomUUID(),
    text: testCase.text,
    targetLocale: testCase.targetLocale,
    contentKind: testCase.contentKind,
  };
  const response = await fetch(endpoint, {
    method: "POST",
    headers: requestHeaders(body),
    body: JSON.stringify(body),
    signal: AbortSignal.timeout(90_000),
  });
  const raw = await response.text();
  assert.equal(response.status, 200, `${testCase.name} returned ${response.status}: ${raw}`);
  const translated = JSON.parse(raw);
  verifySignedResponse(response, translated);
  assert.equal(translated.requestId, body.requestId);
  assert.equal(translated.detectedLocale, testCase.detectedLocale);
  assert.equal(translated.targetLocale, testCase.targetLocale);
  assert.equal(translated.translationState, "TRANSLATED");
  assert.ok(translated.modelName);
  assert.notEqual(translated.translatedText, body.text);
  assert.ok(testCase.validateLanguage(translated.translatedText), `${testCase.name} language check failed`);
  assert.deepEqual(protectedTokens(translated.translatedText), protectedTokens(body.text));
  results.push({
    case: testCase.name,
    detectedLocale: translated.detectedLocale,
    targetLocale: translated.targetLocale,
    identifierPreservation: "PASS",
    signedResponse: "PASS",
    strictResponseShape: "PASS",
  });
}

console.log(JSON.stringify({ endpoint, providerRuntime: "PASS", cases: results }, null, 2));
