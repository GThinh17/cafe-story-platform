export type MentionRange = {
  start: number;
  end: number;
  query: string;
};

export type MentionEntry = {
  type: "user" | "page";
  id: string;
};

export type MentionMap = Record<string, MentionEntry>;

const MENTION_ACTIVE_REGEX = /(?:^|\s)@([\w.-]*)$/;
const MENTION_SCAN_REGEX = /(?:^|\s)@([\w.-]+)/g;
const COMBINING_MARKS_REGEX = /[̀-ͯ]/g;

export function detectActiveMention(
  text: string,
  cursorPos: number,
): MentionRange | null {
  const before = text.slice(0, cursorPos);
  const match = before.match(MENTION_ACTIVE_REGEX);
  if (!match) return null;
  const query = match[1];
  const start = cursorPos - query.length - 1;
  return { start, end: cursorPos, query };
}

export function replaceMentionAt(
  text: string,
  range: MentionRange,
  slug: string,
): { text: string; cursor: number } {
  const insertion = `@${slug} `;
  const next = text.slice(0, range.start) + insertion + text.slice(range.end);
  const cursor = range.start + insertion.length;
  return { text: next, cursor };
}

export function resolveMentionedIds(
  text: string,
  mentionMap: MentionMap,
): { userIds: string[]; cafePageIds: string[] } {
  const userIds = new Set<string>();
  const cafePageIds = new Set<string>();

  for (const match of text.matchAll(MENTION_SCAN_REGEX)) {
    const slug = match[1];
    const entry = mentionMap[slug];
    if (!entry) continue;
    if (entry.type === "user") {
      userIds.add(entry.id);
    } else {
      cafePageIds.add(entry.id);
    }
  }

  return { userIds: [...userIds], cafePageIds: [...cafePageIds] };
}

// Khi submit, encode cafe page mentions thành '@[<slug>](cafe:<id>)' để render-time
// có thể route đúng `/cafes/<id>`. User mentions giữ nguyên `@<slug>` vì slug = userName
// đã khớp route `/[username]`.
export function encodeMentionsForSubmit(
  text: string,
  mentionMap: MentionMap,
): string {
  return text.replace(/(^|\s)@([\w.-]+)/g, (_, leading: string, slug: string) => {
    const entry = mentionMap[slug];
    if (entry?.type === "page") {
      return `${leading}@[${slug}](cafe:${entry.id})`;
    }
    return `${leading}@${slug}`;
  });
}

export function slugifyForMention(name: string): string {
  return name
    .normalize("NFKD")
    .replace(COMBINING_MARKS_REGEX, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "");
}

export function ensureUniqueSlug(
  base: string,
  taken: Set<string>,
  fallbackId: string,
): string {
  const safeBase = base.length > 0 ? base : `page-${fallbackId.slice(0, 6)}`;
  if (!taken.has(safeBase)) return safeBase;
  let i = 2;
  while (taken.has(`${safeBase}-${i}`)) i++;
  return `${safeBase}-${i}`;
}
