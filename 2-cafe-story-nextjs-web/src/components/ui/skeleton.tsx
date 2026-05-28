import { cn } from "@/lib/utils"

function Skeleton({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      data-slot="skeleton"
      className={cn(
        "animate-pulse rounded-md bg-gradient-to-r from-surface-muted via-primary/10 to-surface-muted",
        className,
      )}
      {...props}
    />
  )
}

export { Skeleton }
