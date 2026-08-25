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
        if (values.has(key)) duplicates.add(key);
        values.set(key, property.initializer.text);
      }
    }
    ts.forEachChild(node, visit);
  }

  visit(sourceFile);
  return { duplicates, values };
}

const dictionaryDirectory = path.join(sourceRoot, "features", "i18n", "dictionaries");
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
  if (catalog.duplicates.size) {
    errors.push(`${label} has duplicate keys: ${[...catalog.duplicates].join(", ")}`);
  }
  for (const [key, value] of catalog.values) {
    if (!value.trim()) errors.push(`${label} has an empty value for ${JSON.stringify(key)}.`);
  }
}

function placeholders(value) {
  return [...value.matchAll(/\{(\w+)\}/g)].map((match) => match[1]).sort();
}

for (const key of enKeys) {
  if (
    JSON.stringify(placeholders(en.values.get(key) ?? "")) !==
    JSON.stringify(placeholders(vi.values.get(key) ?? ""))
  ) {
    errors.push(`Placeholder mismatch for dictionary key ${key}.`);
  }
}
for (const [phrase, translation] of phraseCatalog.values) {
  if (JSON.stringify(placeholders(phrase)) !== JSON.stringify(placeholders(translation))) {
    errors.push(`Placeholder mismatch for UI phrase ${JSON.stringify(phrase)}.`);
  }
}

const uiPropertyNames = new Set([
  "aria-label",
  "buttonLabel",
  "confirmLabel",
  "description",
  "emptyDescription",
  "emptyTitle",
  "falseLabel",
  "header",
  "label",
  "message",
  "placeholder",
  "title",
  "trueLabel",
]);
const allowedPhrases = new Set([
  "—",
  "×",
  "CafeStory",
  "CafeStory Admin",
  "ADMIN",
  "ID",
  "AI",
  "VND",
  "CONTRACT V2",
  "LEGACY V1",
  "no_tool_metadata",
]);
const violations = [];

function normalizePhrase(value) {
  return value.replace(/\s+/g, " ").trim();
}

function isMeaningfulUiPhrase(value) {
  const phrase = normalizePhrase(value);
  return phrase && /[A-Za-zÀ-ỹ]/.test(phrase) && !allowedPhrases.has(phrase);
}

function report(value, file, sourceFile, position) {
  const phrase = normalizePhrase(value);
  if (!isMeaningfulUiPhrase(phrase)) return;
  if (phraseCatalog.values.has(phrase) || [...en.values.values()].includes(phrase)) return;
  const relative = path.relative(projectRoot, file).replaceAll(path.sep, "/");
  const line = sourceFile.getLineAndCharacterOfPosition(position).line + 1;
  violations.push(`${relative}:${line} ${JSON.stringify(phrase)}`);
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

  function visit(node) {
    if (ts.isJsxText(node)) report(node.text, file, sourceFile, node.pos);

    if (
      ts.isJsxAttribute(node) &&
      uiPropertyNames.has(node.name.text) &&
      node.initializer &&
      ts.isStringLiteral(node.initializer)
    ) {
      report(node.initializer.text, file, sourceFile, node.pos);
    }

    if (
      ts.isPropertyAssignment(node) &&
      uiPropertyNames.has(node.name.getText(sourceFile).replace(/["']/g, "")) &&
      ts.isStringLiteralLike(node.initializer)
    ) {
      report(node.initializer.text, file, sourceFile, node.pos);
    }

    if (ts.isCallExpression(node)) {
      const callName = node.expression.getText(sourceFile);
      if (
        callName === "ui" &&
        node.arguments[0] &&
        ts.isStringLiteralLike(node.arguments[0])
      ) {
        report(node.arguments[0].text, file, sourceFile, node.arguments[0].pos);
      }
      if (/^(setError|setMessage|setSuccess|set.*Error)$/.test(callName)) {
        for (const argument of node.arguments) {
          if (ts.isStringLiteralLike(argument)) {
            report(argument.text, file, sourceFile, argument.pos);
          }
        }
      }
    }

    if (
      ts.isStringLiteralLike(node) &&
      ts.isConditionalExpression(node.parent) &&
      (node.parent.whenTrue === node || node.parent.whenFalse === node) &&
      ts.isJsxExpression(node.parent.parent) &&
      (ts.isJsxElement(node.parent.parent.parent) ||
        ts.isJsxFragment(node.parent.parent.parent))
    ) {
      report(node.text, file, sourceFile, node.pos);
    }

    ts.forEachChild(node, visit);
  }
  visit(sourceFile);
}

function walk(directory) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const fullPath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      if (
        fullPath.includes(`${path.sep}features${path.sep}i18n`) ||
        fullPath.includes(`${path.sep}lib${path.sep}geo`)
      ) {
        continue;
      }
      walk(fullPath);
    } else if (/\.tsx?$/.test(entry.name)) {
      scanSource(fullPath);
    }
  }
}

for (const area of ["app", "components", "hooks", "lib"]) {
  walk(path.join(sourceRoot, area));
}

if (violations.length) {
  errors.push(
    `Hardcoded UI copy (${violations.length}):\n${violations
      .sort()
      .map((entry) => `  - ${entry}`)
      .join("\n")}`,
  );
}

if (errors.length) {
  console.error(errors.join("\n\n"));
  process.exitCode = 1;
} else {
  console.log(
    JSON.stringify(
      {
        dictionaryKeys: enKeys.length,
        uiPhraseCatalogEntries: phraseCatalog.values.size,
        scannedAreas: ["app", "components", "hooks", "lib"],
        validation: "PASS",
      },
      null,
      2,
    ),
  );
}
