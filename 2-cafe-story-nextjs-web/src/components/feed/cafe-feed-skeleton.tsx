import type * as React from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

function CafeSkeleton({
  className,
  ...props
}: React.ComponentProps<typeof Skeleton>) {
  return (
    <Skeleton
      className={cn("bg-surface-muted", className)}
      {...props}
    />
  );
}

function StoryRailSkeleton() {
  return (
    <div className="w-full overflow-hidden">
      <div className="grid grid-cols-[repeat(auto-fit,64px)] gap-x-4 gap-y-4">
        {Array.from({ length: 5 }).map((_, index) => (
          <div className="grid w-16 justify-items-center gap-2" key={index}>
            <CafeSkeleton className="size-16 rounded-full" />
            <CafeSkeleton className="h-3 w-12" />
          </div>
        ))}
      </div>
    </div>
  );
}

function PostCardSkeleton() {
  return (
    <Card className="mx-auto w-[85%] max-w-full overflow-hidden">
      <CardHeader className="flex flex-row items-center justify-between gap-4 px-4 py-4">
        <div className="flex min-w-0 items-center gap-3">
          <CafeSkeleton className="size-11 rounded-full" />
          <div className="flex min-w-0 flex-col gap-2">
            <CafeSkeleton className="h-4 w-36" />
            <CafeSkeleton className="h-3 w-44 max-w-full" />
          </div>
        </div>
        <CafeSkeleton className="h-7 w-12" />
      </CardHeader>

      <CafeSkeleton className="aspect-[4/5] w-full rounded-none sm:aspect-[5/4]" />

      <CardContent className="flex flex-col gap-4 px-4 py-4">
        <div className="flex items-center justify-between">
          <div className="flex gap-2">
            <CafeSkeleton className="size-10 rounded-full" />
            <CafeSkeleton className="size-10 rounded-full" />
            <CafeSkeleton className="size-10 rounded-full" />
          </div>
          <CafeSkeleton className="h-8 w-14" />
        </div>
        <CafeSkeleton className="h-4 w-44" />
        <div className="flex flex-col gap-2">
          <CafeSkeleton className="h-4 w-full" />
          <CafeSkeleton className="h-4 w-4/5" />
        </div>
        <div className="flex gap-2">
          <CafeSkeleton className="h-6 w-24" />
          <CafeSkeleton className="h-6 w-28" />
        </div>
      </CardContent>
    </Card>
  );
}

function SidebarSkeleton() {
  return (
    <section className="flex flex-col gap-7">
      <div className="flex items-center gap-3">
        <CafeSkeleton className="size-12 rounded-full" />
        <div className="flex min-w-0 flex-1 flex-col gap-2">
          <CafeSkeleton className="h-4 w-28" />
          <CafeSkeleton className="h-3 w-20" />
        </div>
        <CafeSkeleton className="h-4 w-12" />
      </div>

      <Card className="shadow-none">
        <CardHeader className="px-4 py-4">
          <CafeSkeleton className="h-4 w-36" />
        </CardHeader>
        <CardContent className="flex flex-col gap-4 px-4 pb-4">
          {Array.from({ length: 4 }).map((_, index) => (
            <div className="flex items-center gap-3" key={index}>
              <CafeSkeleton className="size-11 rounded-full" />
              <div className="flex flex-1 flex-col gap-2">
                <CafeSkeleton className="h-3 w-28" />
                <CafeSkeleton className="h-3 w-20" />
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      <div className="flex flex-col gap-2">
        <CafeSkeleton className="h-3 w-56" />
        <CafeSkeleton className="h-3 w-32" />
      </div>
    </section>
  );
}

export function CafeFeedSkeleton() {
  return (
    <div
      aria-busy="true"
      aria-label="Loading cafe feed"
      className="min-h-screen w-full max-w-full overflow-x-clip bg-background text-foreground"
    >
      <main className="grid w-full max-w-[1120px] touch-pan-y grid-cols-1 gap-14 overflow-x-clip px-4 py-8 sm:px-8 xl:ml-12 xl:grid-cols-[630px_320px] xl:px-0 2xl:ml-20">
        <section className="flex w-full max-w-[630px] flex-col gap-8">
          <StoryRailSkeleton />
          <div className="flex flex-col gap-6">
            <PostCardSkeleton />
            <PostCardSkeleton />
          </div>
        </section>

        <aside className="sticky top-8 hidden h-fit w-full xl:block">
          <SidebarSkeleton />
        </aside>
      </main>

      <div className="fixed bottom-8 right-6 z-40 hidden xl:block 2xl:right-10">
        <div className="flex h-14 w-48 items-center gap-3 rounded-full border border-border bg-surface px-4 shadow-xl">
          <CafeSkeleton className="size-8 rounded-full" />
          <CafeSkeleton className="h-4 flex-1" />
          <Separator className="h-7" orientation="vertical" />
          <CafeSkeleton className="size-7 rounded-full" />
        </div>
      </div>
    </div>
  );
}
