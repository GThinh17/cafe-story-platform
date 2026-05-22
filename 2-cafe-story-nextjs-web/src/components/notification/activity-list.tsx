import type { ActivityNotification } from "@/types/user";

type ActivityListProps = {
  items: ActivityNotification[];
};

export function ActivityList({ items }: ActivityListProps) {
  return (
    <div className="space-y-3">
      {items.map((item) => (
        <article
          className="flex items-center gap-4 rounded-md border border-border bg-surface p-4 shadow-sm"
          key={item.id}
        >
          <span className="grid h-12 w-12 shrink-0 place-items-center rounded-full bg-surface-muted text-sm font-black text-primary-strong">
            {item.avatarInitials}
          </span>
          <div className="min-w-0 flex-1">
            <p className="text-sm leading-6">
              <span className="font-black">{item.actor}</span> {item.action}{" "}
              <span className="font-black">{item.target}</span>
            </p>
            <p className="text-xs text-muted">{item.time} ago</p>
          </div>
          {item.unread ? (
            <span className="h-2.5 w-2.5 rounded-full bg-accent" />
          ) : null}
        </article>
      ))}
    </div>
  );
}
