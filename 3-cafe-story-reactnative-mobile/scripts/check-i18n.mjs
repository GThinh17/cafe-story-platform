import fs from "node:fs";
import path from "node:path";
import ts from "typescript";

const projectRoot = path.resolve(import.meta.dirname, "..");
const sourceRoot = path.join(projectRoot, "src");

function unwrapExpression(expression) {
  if (
    ts.isAsExpression(expression) ||
    ts.isSatisfiesExpression(expression) ||
    ts.isParenthesizedExpression(expression)
  ) {
    return unwrapExpression(expression.expression);
  }
  return expression;
}

function readObject(file, variableName) {
  const source = fs.readFileSync(file, "utf8");
  const sourceFile = ts.createSourceFile(
    file,
    source,
    ts.ScriptTarget.Latest,
    true,
    ts.ScriptKind.TS,
  );
  const values = new Map();
  const duplicates = new Set();

  function visit(node) {
    if (
      ts.isVariableDeclaration(node) &&
      node.name.getText(sourceFile) === variableName &&
      node.initializer
    ) {
      const object = unwrapExpression(node.initializer);
      if (!ts.isObjectLiteralExpression(object)) {
        throw new Error(`${variableName} in ${file} is not an object literal`);
      }

      for (const property of object.properties) {
        if (
          !ts.isPropertyAssignment(property) ||
          !ts.isStringLiteralLike(property.initializer)
        ) {
          continue;
        }
        const key = ts.isStringLiteralLike(property.name)
          ? property.name.text
          : property.name.getText(sourceFile);
        if (values.has(key)) {
          duplicates.add(key);
        }
        values.set(key, property.initializer.text);
      }
    }
    ts.forEachChild(node, visit);
  }

  visit(sourceFile);
  return { duplicates, values };
}

const dictionaryDirectory = path.join(
  sourceRoot,
  "features",
  "i18n",
  "dictionaries",
);
const en = readObject(path.join(dictionaryDirectory, "en.ts"), "en");
const vi = readObject(path.join(dictionaryDirectory, "vi.ts"), "vi");
const phraseCatalog = readObject(
  path.join(sourceRoot, "features", "i18n", "ui-phrases.ts"),
  "viUiPhrases",
);

const errors = [];
const enKeys = [...en.values.keys()].sort();
const viKeys = [...vi.values.keys()].sort();

if (JSON.stringify(enKeys) !== JSON.stringify(viKeys)) {
  errors.push("English and Vietnamese dictionaries do not have identical keys.");
}

for (const [label, catalog] of [
  ["English dictionary", en],
  ["Vietnamese dictionary", vi],
  ["Vietnamese UI phrase catalog", phraseCatalog],
]) {
  if (catalog.duplicates.size > 0) {
    errors.push(`${label} has duplicate keys: ${[...catalog.duplicates].join(", ")}`);
  }
  for (const [key, value] of catalog.values) {
    if (!value.trim()) {
      errors.push(`${label} has an empty value for ${JSON.stringify(key)}.`);
    }
  }
}

function placeholders(value) {
  return [...value.matchAll(/\{(\w+)\}/g)].map((match) => match[1]).sort();
}

for (const key of enKeys) {
  const enPlaceholders = placeholders(en.values.get(key) ?? "");
  const viPlaceholders = placeholders(vi.values.get(key) ?? "");
  if (JSON.stringify(enPlaceholders) !== JSON.stringify(viPlaceholders)) {
    errors.push(`Placeholder mismatch for ${key}.`);
  }
}

const uiPropertyNames = new Set([
  "accessibilityHint",
  "accessibilityLabel",
  "buttonLabel",
  "body",
  "description",
  "emptyDescription",
  "emptyLabel",
  "emptyMessage",
  "emptyTitle",
  "label",
  "message",
  "placeholder",
  "title",
]);
const allowedPhrases = new Set([
  "1:1",
  "3M",
  "4:3",
  "10:16",
  "16:9",
  "Cafe Story",
  "CafeStory",
  "CafeStory AI",
  "CafeStory Assistant",
  "CTR",
  "Gia Thinh",
  "L /",
  "Nguyen Hue Street",
  "S",
  "Stripe",
  "VNPAY",
  "cafestory_user",
  "hello@cafestory.com",
  "https://your-cafe.example/menu",
  "button",
  "image",
  "none",
  "outlined",
  "padding",
  "primary",
  "secondary",
  "transparent",
]);
const englishDictionaryValues = new Set(en.values.values());
const knownUiPhrases = new Set(phraseCatalog.values.keys());
const missingPhrases = new Map();
const implicitPhrases = new Map();
const forbiddenImports = [];

function normalizePhrase(value) {
  return value.replace(/\s+/g, " ").trim();
}

function phraseLocation(file, sourceFile, position) {
  const relative = path.relative(projectRoot, file).replaceAll(path.sep, "/");
  const line = sourceFile.getLineAndCharacterOfPosition(position).line + 1;
  return `${relative}:${line}`;
}

function recordPhrase(value, file, sourceFile, position, requireExplicit = false) {
  const phrase = normalizePhrase(value);
  if (!phrase || !/[A-Za-z]/.test(phrase) || allowedPhrases.has(phrase)) {
    return;
  }
  if (englishDictionaryValues.has(phrase) || knownUiPhrases.has(phrase)) {
    if (requireExplicit && !implicitPhrases.has(phrase)) {
      implicitPhrases.set(phrase, phraseLocation(file, sourceFile, position));
    }
    return;
  }

  if (!missingPhrases.has(phrase)) {
    missingPhrases.set(phrase, phraseLocation(file, sourceFile, position));
  }
}

function scanSource(file) {
  const source = fs.readFileSync(file, "utf8");
  const sourceFile = ts.createSourceFile(
    file,
    source,
    ts.ScriptTarget.Latest,
    true,
    ts.ScriptKind.TSX,
  );

  if (source.includes("features/i18n/localized-native")) {
    forbiddenImports.push(path.relative(projectRoot, file).replaceAll(path.sep, "/"));
  }

  function visit(node) {
    if (ts.isJsxText(node)) {
      recordPhrase(node.text, file, sourceFile, node.pos, true);
    }

    if (
      ts.isJsxAttribute(node) &&
      uiPropertyNames.has(node.name.text) &&
      node.initializer &&
      ts.isStringLiteral(node.initializer)
    ) {
      recordPhrase(node.initializer.text, file, sourceFile, node.pos, true);
    }

    if (
      ts.isPropertyAssignment(node) &&
      uiPropertyNames.has(node.name.getText(sourceFile).replace(/["']/g, "")) &&
      ts.isStringLiteralLike(node.initializer)
    ) {
      recordPhrase(node.initializer.text, file, sourceFile, node.pos);
    }

    if (ts.isCallExpression(node)) {
      const callName = node.expression.getText(sourceFile);
      if (/^(Alert\.alert|setError|setMessage|setSuccess)$/.test(callName)) {
        for (const argument of node.arguments) {
          if (ts.isStringLiteralLike(argument)) {
            recordPhrase(argument.text, file, sourceFile, argument.pos, true);
          }
        }
      }
    }

    if (
      ts.isStringLiteralLike(node) &&
      ts.isConditionalExpression(node.parent) &&
      (node.parent.whenTrue === node || node.parent.whenFalse === node) &&
      ts.isJsxExpression(node.parent.parent)
    ) {
      recordPhrase(node.text, file, sourceFile, node.pos, true);
    }

    ts.forEachChild(node, visit);
  }

  visit(sourceFile);
}

function walk(directory) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const fullPath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      if (fullPath.includes(`${path.sep}features${path.sep}i18n`)) {
        continue;
      }
      walk(fullPath);
    } else if (/\.tsx?$/.test(entry.name)) {
      scanSource(fullPath);
    }
  }
}

for (const area of ["components", "features", "navigation", "screens"]) {
  walk(path.join(sourceRoot, area));
}

if (missingPhrases.size > 0) {
  errors.push(
    `Uncatalogued UI phrases (${missingPhrases.size}):\n${[...missingPhrases]
      .sort(([left], [right]) => left.localeCompare(right))
      .map(([phrase, location]) => `  - ${JSON.stringify(phrase)} at ${location}`)
      .join("\n")}`,
  );
}

if (implicitPhrases.size > 0) {
  errors.push(
    `UI phrases must use explicit t(...) calls (${implicitPhrases.size}):\n${[...implicitPhrases]
      .sort(([left], [right]) => left.localeCompare(right))
      .map(([phrase, location]) => `  - ${JSON.stringify(phrase)} at ${location}`)
      .join("\n")}`,
  );
}

if (forbiddenImports.length > 0) {
  errors.push(
    `Forbidden localized-native imports:\n${forbiddenImports
      .sort()
      .map((file) => `  - ${file}`)
      .join("\n")}`,
  );
}

if (errors.length > 0) {
  console.error(errors.join("\n\n"));
  process.exitCode = 1;
} else {
  console.log(
    JSON.stringify(
      {
        dictionaryKeys: enKeys.length,
        scannedAreas: ["components", "features", "navigation", "screens"],
        uiPhraseCatalogEntries: phraseCatalog.values.size,
        validation: "PASS",
      },
      null,
      2,
    ),
  );
}
