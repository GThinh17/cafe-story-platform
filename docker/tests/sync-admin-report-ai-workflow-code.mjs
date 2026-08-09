import { readFileSync, writeFileSync } from 'node:fs';

const workflowPath = new URL('../cafestory-admin-report-ai-resolution-n8n-workflow.json', import.meta.url);
const buildCodePath = new URL(
  '../n8n-code/admin-report-ai-resolution/validate-contract-v2-and-build-request.js',
  import.meta.url,
);
const normalizeCodePath = new URL(
  '../n8n-code/admin-report-ai-resolution/validate-and-normalize-recommendation-v2.js',
  import.meta.url,
);

const workflow = JSON.parse(readFileSync(workflowPath, 'utf8'));
const replacements = new Map([
  ['Validate Contract V2 And Build Request', readFileSync(buildCodePath, 'utf8').trim()],
  ['Validate And Normalize Recommendation V2', readFileSync(normalizeCodePath, 'utf8').trim()],
]);

for (const [nodeName, jsCode] of replacements) {
  const node = workflow.nodes.find((candidate) => candidate.name === nodeName);
  if (!node) throw new Error(`Missing workflow node: ${nodeName}`);
  node.parameters.jsCode = jsCode;
}

writeFileSync(workflowPath, `${JSON.stringify(workflow, null, 2)}\n`, 'utf8');
console.log('ADMIN_REPORT_AI_WORKFLOW_CODE_SYNC=PASS');
