"use client";

import type * as React from "react";
import { useI18n } from "@/components/providers/locale-provider";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

function CafeSkeleton({
  className,
  ...props
}: React.ComponentProps<typeof Skeleton>) {
  return (
    <Skeleton
      className={cn(
        "border border-border/40 from-surface-muted via-rating/10 to-surface-muted",
        className,
      )}
      {...props}
    />
  );
}

/** Bám theo markup thật của StoryRail: 2 nút mũi tên size-8 kẹp một dải flex gap-8. */
function StoryRailSkeleton() {
  return (
    <div className="flex w-full items-center gap-2">
      <CafeSkeleton className="size-8 shrink-0 rounded-full" />
      <div className="flex min-w-0 flex-1 gap-8 overflow-hidden pb-1">
        {Array.from({ length: 6 }).map((_, index) => (
          <div
            className="grid w-[5.2rem] shrink-0 justify-items-center"
            key={index}
          >
            <CafeSkeleton className="size-[5.2rem] rounded-full" />
            <CafeSkeleton className="mt-2 h-4 w-[4.2rem]" />
          </div>
        ))}
      </div>
      <CafeSkeleton className="size-8 shrink-0 rounded-full" />
    </div>
  );
}

/**
 * Khớp từng khối của {@code PostCard}: cùng bề rộng 85%, cùng padding, cùng
 * hình dạng phần tử. Lệch một chỗ nào là lúc dữ liệu về sẽ thấy nội dung nhảy.
 */
function PostCardSkeleton() {
  return (
    // shadow-none: PostCard thật cũng tắt shadow, thiếu nó là card bị "phồng"
    // lên lúc skeleton rồi xẹp xuống khi có dữ liệu.
    <Card className="mx-auto w-[85%] max-w-full overflow-hidden shadow-none">
      <CardHeader className="flex flex-row items-center justify-between gap-4 px-4 py-4">
        <div className="flex min-w-0 items-center gap-3">
          <CafeSkeleton className="size-11 shrink-0 rounded-full" />
          <div className="flex min-w-0 flex-col gap-2">
            <CafeSkeleton className="h-4 w-36" />
            <CafeSkeleton className="h-3 w-44 max-w-full" />
          </div>
        </div>
        {/* Nút ba chấm là icon tròn size-8, không phải khối chữ nhật. */}
        <CafeSkeleton className="size-8 shrink-0 rounded-full" />
      </CardHeader>

      {/* PostMediaCarousel frame="adaptive" mặc định tỉ lệ 1:1. */}
      <CafeSkeleton className="aspect-square w-full rounded-none" />

      <CardContent className="flex flex-col gap-4 px-4 py-4">
        <div className="flex items-center justify-between">
          {/* Like/comment/share là nút ghost icon + số, nằm ngang gap-4 — không
              phải ba nút tròn size-10. */}
          <div className="flex items-center gap-4">
            {Array.from({ length: 3 }).map((_, index) => (
              <CafeSkeleton className="h-6 w-12" key={index} />
            ))}
          </div>
          <CafeSkeleton className="size-6 shrink-0" />
        </div>
        <CafeSkeleton className="h-3 w-40" />
        <div className="flex flex-col gap-2">
          <CafeSkeleton className="h-4 w-full" />
          <CafeSkeleton className="h-4 w-4/5" />
        </div>
        <div className="flex flex-wrap gap-2">
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

/**
 * Chỉ phần nội dung của CỘT FEED — không kèm khung trang.
 *
 * <p>Dùng cho {@code <Suspense>} bên trong section feed của trang home. Trước
 * đây chỗ đó nhận cả {@link CafeFeedSkeleton}, tức là một trang hoàn chỉnh
 * (min-h-screen + grid riêng + sidebar riêng + dock nổi) bị nhét vào trong cột
 * rộng 630px của trang thật: grid lồng grid, sidebar giả bị bóp vào cột feed.
 */
export function FeedColumnSkeleton() {
  const { t } = useI18n();

  return (
    <div
      aria-busy="true"
      aria-label={t("feed.skeleton.label")}
      className="space-y-8"
    >
      <StoryRailSkeleton />
      <div className="flex flex-col gap-6">
        <PostCardSkeleton />
        <PostCardSkeleton />
      </div>
    </div>
  );
}

/**
 * Skeleton cấp trang, dùng cho {@code app/(home)/loading.tsx} — lúc đó trang
 * thật chưa render nên phải tự dựng khung.
 *
 * <p>Khung dưới đây sao đúng theo `app/(home)/page.tsx`: cùng grid
 * `xl:grid-cols-[680px_1fr_320px]`, cùng `max-w-[630px] space-y-8` cho cột
 * feed, cùng `xl:col-start-3` cho sidebar. Không dựng lại MessageDock vì trang
 * thật để `fallback={null}` cho nó.
 */
export function CafeFeedSkeleton() {
  return (
    <div className="min-h-screen w-full max-w-full overflow-x-clip bg-background text-foreground">
      <main className="grid w-full max-w-[1120px] touch-pan-y grid-cols-1 gap-14 overflow-x-clip px-4 py-8 sm:px-8 xl:ml-12 xl:max-w-none xl:grid-cols-[680px_1fr_320px] xl:gap-0 xl:px-0 xl:pr-16 2xl:ml-20 2xl:pr-24">
        <section className="w-full max-w-[630px] space-y-8">
          <FeedColumnSkeleton />
        </section>

        <aside className="sticky top-8 hidden h-fit w-full xl:col-start-3 xl:block">
          <SidebarSkeleton />
        </aside>
      </main>
    </div>
  );
}
