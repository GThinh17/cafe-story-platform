"use client";

import { Fragment, type ReactNode } from "react";

// AI trả markdown nhẹ; ở bubble chỉ render đậm '**text**'.
// Cặp '**' phải nằm trên cùng một đoạn và không rỗng, tránh nuốt text khi model
// để '**' lẻ (ví dụ '2 ** 3' hoặc bullet chưa đóng cặp).
const BOLD_REGEX = /\*\*(?!\s)([^*\n]+?)(?<!\s)\*\*/g;

type MarkdownBoldTextProps = {
  text: string | null | undefined;
  className?: string;
};

type Segment =
  | { kind: "text"; value: string }
  | { kind: "bold"; value: string };

function parseSegments(text: string): Segment[] {
  const segments: Segment[] = [];
  let lastIndex = 0;

  for (const match of text.matchAll(BOLD_REGEX)) {
    const matchStart = match.index ?? 0;
    if (matchStart > lastIndex) {
      segments.push({ kind: "text", value: text.slice(lastIndex, matchStart) });
    }
    segments.push({ kind: "bold", value: match[1] });
    lastIndex = matchStart + match[0].length;
  }

  if (lastIndex < text.length) {
    segments.push({ kind: "text", value: text.slice(lastIndex) });
  }

  return segments;
}

export function MarkdownBoldText({ text, className }: MarkdownBoldTextProps) {
  if (!text) return null;

  const nodes: ReactNode[] = parseSegments(text).map((segment, index) =>
    segment.kind === "bold" ? (
      <strong className="font-bold" key={index}>
        {segment.value}
      </strong>
    ) : (
      <Fragment key={index}>{segment.value}</Fragment>
    ),
  );

  return <span className={className}>{nodes}</span>;
}
