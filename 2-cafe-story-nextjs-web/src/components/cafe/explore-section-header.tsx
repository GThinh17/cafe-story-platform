type ExploreSectionHeaderProps = {
  title: string;
  actionLabel?: string;
  actionHref?: string;
};

export function ExploreSectionHeader({
  title,
  actionLabel,
  actionHref,
}: ExploreSectionHeaderProps) {
  return (
    <div className="flex items-end justify-between gap-4">
      <h2 className="font-serif text-3xl font-medium italic text-espresso sm:text-4xl">
        {title}
      </h2>
      {actionLabel && actionHref ? (
        <a
          className="shrink-0 border-b border-line-soft pb-1 text-xs font-black uppercase tracking-[0.12em] text-coffee-muted no-underline transition hover:text-primary"
          href={actionHref}
        >
          {actionLabel}
        </a>
      ) : null}
    </div>
  );
}
