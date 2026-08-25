import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const workflowPath = path.join(
  root,
  "docker",
  "cafestory-admin-content-translation-n8n-workflow.json",
);
const validateCode = fs.readFileSync(
  path.join(root, "docker/n8n-code/admin-content-translation/validate-and-build-request.js"),
  "utf8",
);
const normalizeCode = fs.readFileSync(
  path.join(root, "docker/n8n-code/admin-content-translation/normalize-and-sign-response.js"),
  "utf8",
);

const workflow = {
  id: "cafestory-admin-content-translation-v1",
  name: "CafeStory Admin Content Translation",
  active: false,
  nodes: [
    {
      parameters: {
        httpMethod: "POST",
        path: "cafestory-admin-content-translation",
        responseMode: "responseNode",
        options: {},
      },
      id: "b09b0f39-57c1-45bd-981a-26c6a63e8466",
      name: "Admin Content Translation Webhook",
      type: "n8n-nodes-base.webhook",
      typeVersion: 2,
      position: [-760, 0],
      webhookId: "cafestory-admin-content-translation",
    },
    {
      parameters: { jsCode: validateCode },
      id: "49aeef5a-08ab-4c2a-a7cb-eb6a09e2ce85",
      name: "Validate Translation Contract",
      type: "n8n-nodes-base.code",
      typeVersion: 2,
      position: [-490, 0],
    },
    {
      parameters: {
        method: "POST",
        url: "https://api.openai.com/v1/responses",
        sendHeaders: true,
        headerParameters: {
          parameters: [
            { name: "Authorization", value: "=Bearer {{$env.OPENAI_API_KEY}}" },
            { name: "Content-Type", value: "application/json" },
          ],
        },
        sendBody: true,
        specifyBody: "json",
        jsonBody: "={{ $json.openaiRequest }}",
        options: { timeout: 30000 },
      },
      id: "e3adf190-8005-42bf-a2e7-47b5d866f78a",
      name: "OpenAI Translation",
      type: "n8n-nodes-base.httpRequest",
      typeVersion: 4.2,
      retryOnFail: true,
      maxTries: 3,
      waitBetweenTries: 2000,
      position: [-220, 0],
    },
    {
      parameters: { jsCode: normalizeCode },
      id: "52b5474a-c8ce-4a31-9992-96056520ef9d",
      name: "Normalize And Sign Translation",
      type: "n8n-nodes-base.code",
      typeVersion: 2,
      position: [50, 0],
    },
    {
      parameters: {
        respondWith: "json",
        responseBody: "={{ $json.response }}",
        options: {
          responseHeaders: {
            entries: [
              {
                name: "X-CafeStory-Contract-Version",
                value: "={{ $json.responseHeaders.contractVersion }}",
              },
              {
                name: "X-CafeStory-Correlation-Id",
                value: "={{ $json.responseHeaders.correlationId }}",
              },
              {
                name: "X-CafeStory-Timestamp",
                value: "={{ $json.responseHeaders.timestamp }}",
              },
              {
                name: "X-CafeStory-Nonce",
                value: "={{ $json.responseHeaders.nonce }}",
              },
              {
                name: "X-CafeStory-Body-SHA256",
                value: "={{ $json.responseHeaders.bodyHash }}",
              },
              {
                name: "X-CafeStory-Signature",
                value: "={{ $json.responseHeaders.signature }}",
              },
            ],
          },
        },
      },
      id: "91843acd-e9fd-42b2-9af0-a6b254d5b66f",
      name: "Respond To Backend",
      type: "n8n-nodes-base.respondToWebhook",
      typeVersion: 1.1,
      position: [320, 0],
    },
  ],
  connections: {
    "Admin Content Translation Webhook": {
      main: [[{ node: "Validate Translation Contract", type: "main", index: 0 }]],
    },
    "Validate Translation Contract": {
      main: [[{ node: "OpenAI Translation", type: "main", index: 0 }]],
    },
    "OpenAI Translation": {
      main: [[{ node: "Normalize And Sign Translation", type: "main", index: 0 }]],
    },
    "Normalize And Sign Translation": {
      main: [[{ node: "Respond To Backend", type: "main", index: 0 }]],
    },
  },
  settings: { executionOrder: "v1" },
  versionId: "f524f76c-a55c-4be3-b4dd-3ad83d0ee94e",
  meta: { templateCredsSetupCompleted: true },
  tags: [],
};

fs.writeFileSync(workflowPath, `${JSON.stringify(workflow, null, 2)}\n`, "utf8");
console.log(`Synced ${path.relative(root, workflowPath)}`);
