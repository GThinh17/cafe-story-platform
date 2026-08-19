import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

const projectRoot = resolve(import.meta.dirname, "..");

function read(relativePath) {
  return readFileSync(resolve(projectRoot, relativePath), "utf8");
}

const config = read("src/features/i18n/config.ts");
const localeProvider = read("src/features/i18n/locale-provider.tsx");
const commentModal = read("src/components/feed/comment-modal.tsx");
const reportModal = read("src/components/feed/report-post-modal.tsx");
const currentLocale = read("src/features/i18n/current-locale.ts");

assert.match(config, /DEFAULT_LOCALE:\s*Locale\s*=\s*"vi"/);
assert.match(localeProvider, /storedPreference\s*===\s*"system"/);
assert.match(
  localeProvider,
  /AsyncStorage\.setItem\(LOCALE_STORAGE_KEY,\s*nextPreference\)/,
);

assert.doesNotMatch(commentModal, /(^|[^\w])t\(\s*comment\.content\s*\)/m);
assert.doesNotMatch(commentModal, /(^|[^\w])t\(\s*authorName\s*\)/m);
assert.match(commentModal, /renderCommentContent\(comment\.content\)/);
assert.match(
  commentModal,
  /const canReport\s*=\s*!isPending\s*&&\s*Boolean\(viewerUserId\)\s*&&\s*comment\.userId\s*!==\s*viewerUserId/,
);
assert.match(commentModal, /targetId=\{reportComment\.id\}/);
assert.match(commentModal, /targetType="COMMENT"/);

assert.match(
  reportModal,
  /targetType:\s*Extract<ReportTargetType,\s*"BLOG"\s*\|\s*"COMMENT">/,
);
assert.match(reportModal, /getReportReasons\(targetType\)/);
assert.match(
  reportModal,
  /createContentReport\(\{[\s\S]*?reasonId:\s*selectedReason\.id,[\s\S]*?targetId,[\s\S]*?targetType,[\s\S]*?\}\)/,
);
assert.doesNotMatch(reportModal, /hide|remove|resolve/i);

assert.doesNotMatch(currentLocale, /localizeNode/);
assert.doesNotMatch(commentModal, /localized-native/);
assert.doesNotMatch(reportModal, /localized-native/);

process.stdout.write(
  "Mobile locale and COMMENT report regression checks: PASS\n" +
    "- Fresh preference defaults to Vietnamese and explicit System persists\n" +
    "- Comment/user content is rendered verbatim, outside t(...)\n" +
    "- COMMENT report uses the existing POST /api/reports contract\n" +
    "- No target/report mutation action exists in the report modal\n",
);
