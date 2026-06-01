import { cn } from "@/lib/utils";

type MenuSectionTitleProps = {
  eyebrow?: string;
  title: string;
  ornament?: boolean;
  className?: string;
};

export function MenuSectionTitle({
  eyebrow,
  title,
  ornament = false,
  className,
}: MenuSectionTitleProps) {
  if (eyebrow) {
    return (
      <div className={cn("text-center", className)}>
        <p className="text-[10px] font-black uppercase tracking-[0.24em] text-muted">
          {eyebrow}
        </p>
        <h3 className="mt-2 font-serif text-3xl font-medium text-espresso">
          {title}
        </h3>
      </div>
    );
  }

  return (
    <div className={cn("flex items-center gap-4", className)}>
      <span className="h-px flex-1 bg-line-soft" />
      <h3 className="shrink-0 font-serif text-lg font-medium italic text-espresso">
        {ornament ? `& ${title} &` : title}
      </h3>
      <span className="h-px flex-1 bg-line-soft" />
    </div>
  );
}
