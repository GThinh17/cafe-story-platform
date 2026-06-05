import { Skeleton } from "@/components/ui/skeleton";

export function PostCommentSkeleton() {
  return (
    <div className="flex min-w-0 gap-3">
      <Skeleton className="size-9 shrink-0 rounded-full" />
      <div className="min-w-0 flex-1">
        <div className="space-y-2">
          <Skeleton className="h-4 w-3/5" />
          <Skeleton className="h-4 w-full" />
        </div>
        <div className="mt-3 flex items-center gap-4">
          <Skeleton className="h-3 w-8" />
          <Skeleton className="h-3 w-14" />
          <Skeleton className="h-3 w-10" />
          <Skeleton className="h-3 w-5" />
        </div>
      </div>
      <Skeleton className="mt-1 size-5 shrink-0 rounded-full" />
    </div>
  );
}
