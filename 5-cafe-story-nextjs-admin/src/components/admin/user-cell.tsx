"use client";

import { useState } from "react";

const DEFAULT_AVATAR = "/images/default-avatar.svg";

type UserCellProps = {
  name: string | null | undefined;
  avatar?: string | null;
  subtitle?: string | null;
};

export function UserCell({ name, avatar, subtitle }: UserCellProps) {
  const [src, setSrc] = useState(avatar || DEFAULT_AVATAR);

  return (
    <div className="flex items-center gap-2.5 min-w-0">
      <img
        alt={name ?? "user"}
        className="size-7 shrink-0 rounded-full object-cover object-center bg-surface-muted"
        src={src}
        onError={() => setSrc(DEFAULT_AVATAR)}
      />
      <div className="min-w-0">
        <p className="truncate text-sm font-semibold text-espresso leading-snug">
          {name ?? "—"}
        </p>
        {subtitle ? (
          <p className="truncate text-xs text-muted leading-snug">{subtitle}</p>
        ) : null}
      </div>
    </div>
  );
}
