const crypto = require('crypto');
const validation = $('Validate Translation Contract').first().json;
const request = validation.requestContext;
const protectedTokenMap = Array.isArray(validation.protectedTokenMap)
  ? validation.protectedTokenMap
  : [];

const outputText = (response) => {
  if (typeof response.output_text === 'string') return response.output_text;
  for (const item of Array.isArray(response.output) ? response.output : []) {
    for (const part of Array.isArray(item.content) ? item.content : []) {
      if (typeof part.text === 'string') return part.text;
    }
  }
  return '';
};
const text = outputText($json).trim();
if (!text) throw new Error('OpenAI translation response did not include output_text');
const result = JSON.parse(text);
const expectedKeys = ['detectedLocale', 'targetLocale', 'translatedText', 'translationState'].sort();
const actualKeys = Object.keys(result).sort();
if (actualKeys.length !== expectedKeys.length || actualKeys.some((key, index) => key !== expectedKeys[index])) {
  throw new Error('OpenAI translation response contains unexpected fields');
}
if (
  !['en', 'vi', 'und'].includes(result.detectedLocale) ||
  result.targetLocale !== request.targetLocale ||
  result.translationState !== 'TRANSLATED' ||
  typeof result.translatedText !== 'string' ||
  !result.translatedText.trim() ||
  result.translatedText.length > 40000
) {
  throw new Error('OpenAI translation response violates the strict contract');
}

let restoredText = result.translatedText;
for (const entry of protectedTokenMap) {
  if (
    !entry ||
    typeof entry.placeholder !== 'string' ||
    typeof entry.value !== 'string' ||
    !Number.isSafeInteger(entry.occurrences) ||
    entry.occurrences < 1
  ) {
    throw new Error('Protected translation token map is invalid');
  }
  const actualOccurrences = restoredText.split(entry.placeholder).length - 1;
  if (actualOccurrences !== entry.occurrences) {
    throw new Error('Translated text changed a protected placeholder');
  }
  restoredText = restoredText.split(entry.placeholder).join(entry.value);
}
if (/⟪CST\d+⟫/u.test(restoredText)) {
  throw new Error('Translated text contains an unknown protected placeholder');
}
result.translatedText = restoredText;

const protectedTokens = (value) => {
  const patterns = [
    /https?:\/\/[^\s<>()]+/gi,
    /\b[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\b/gi,
    /\b[A-Z][A-Z0-9]*(?:[._:-][A-Z0-9]+)+\b/g,
    /\b[A-Z][A-Z0-9_]{2,}\b/g,
    /\b\d{6,}\b/g,
  ];
  return patterns.flatMap((pattern) => value.match(pattern) || []).sort();
};
if (JSON.stringify(protectedTokens(request.text)) !== JSON.stringify(protectedTokens(result.translatedText))) {
  throw new Error('Translated text changed a protected identifier');
}

const response = {
  requestId: request.requestId,
  detectedLocale: result.detectedLocale,
  targetLocale: result.targetLocale,
  translatedText: result.translatedText,
  translationState: 'TRANSLATED',
  modelName: $json.model || $env.OPENAI_DECISION_MODEL || 'gpt-4o-mini',
};
const secret = String($env.ADMIN_REPORT_AI_HMAC_SECRET ?? '');
if (!secret) throw new Error('Admin translation HMAC secret is not configured');
const canonicalize = (value) => {
  if (Array.isArray(value)) return `[${value.map(canonicalize).join(',')}]`;
  if (value && typeof value === 'object') {
    return `{${Object.keys(value).sort().map((key) => `${JSON.stringify(key)}:${canonicalize(value[key])}`).join(',')}}`;
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
const timestamp = String(Math.floor(Date.now() / 1000));
const nonce = crypto.randomUUID();
const bodyHash = crypto.createHash('sha256').update(canonicalize(response), 'utf8').digest('hex');
const signature = crypto
  .createHmac('sha256', secret)
  .update(`${timestamp}\n${nonce}\n${bodyHash}`, 'utf8')
  .digest('hex');

return [{
  json: {
    response,
    responseHeaders: {
      contractVersion: 'ADMIN-CONTENT-TRANSLATION-V1',
      correlationId: String(request.requestId),
      timestamp,
      nonce,
      bodyHash,
      signature,
    },
  },
}];
