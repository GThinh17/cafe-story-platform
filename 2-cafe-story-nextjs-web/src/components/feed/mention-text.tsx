"use client";

import Link from "next/link";
import { Fragment, type ReactNode } from "react";
import { cn } from "@/lib/utils";

// Bắt cả 2 dạng:
//   1. Cafe page encoded: '@[<slug>](cafe:<id>)' → group 2, 3
//   2. Plain mention:     '@<slug>'              → group 4
const MENTION_REGEX =
  /(^|\s)(?:@\[([\w.-]+)\]\(cafe:([^)\s]+)\)|@([\w.-]+))/g;

type MentionTextProps = {
  text: string | null | undefined;
  className?: string;
  mentionClassName?: string;
};

type Segment =
  | { kind: "text"; value: string }
  | { kind: "user"; slug: string; leading: string }
  | { kind: "page"; slug: string; cafePageId: string; leading: string };

function parseSegments(text: string): Segment[] {
  const segments: Segment[] = [];
  let lastIndex = 0;

  for (const match of text.matchAll(MENTION_REGEX)) {
    const fullStart = match.index ?? 0;
    if (fullStart > lastIndex) {
      segments.push({ kind: "text", value: text.slice(lastIndex, fullStart) });
    }
    const leading = match[1];
    const pageSlug = match[2];
    const pageId = match[3];
    const userSlug = match[4];

    if (pageSlug && pageId) {
      segments.push({ kind: "page", slug: pageSlug, cafePageId: pageId, leading });
    } else if (userSlug) {
      segments.push({ kind: "user", slug: userSlug, leading });
    }
    lastIndex = fullStart + match[0].length;
  }

  if (lastIndex < text.length) {
    segments.push({ kind: "text", value: text.slice(lastIndex) });
  }

  return segments;
}

export function MentionText({
  text,
  className,
  mentionClassName,
}: MentionTextProps) {
  if (!text) return null;

  const segments = parseSegments(text);
  const mentionStyle = cn(
    "font-semibold text-primary hover:underline",
    mentionClassName,
  );

  const nodes: ReactNode[] = segments.map((segment, idx) => {
    if (segment.kind === "text") {
      return <Fragment key={idx}>{segment.value}</Fragment>;
    }

    const href =
      segment.kind === "page"
        ? `/cafes/${segment.cafePageId}`
        : `/${segment.slug}`;

    return (
      <Fragment key={idx}>
        {segment.leading}
        <Link className={mentionStyle} href={href}>
          @{segment.slug}
        </Link>
      </Fragment>
    );
  });

  return <span className={className}>{nodes}</span>;
}
