import { CafePage } from "@/components/cafe/cafe-page";
import {
  mockCafeMenu,
  mockCafeMenusByCafeId,
  mockCafeSummaries,
} from "@/mocks/cafes";
import { mockCafeReviewPosts } from "@/mocks/reviews";

export default async function CafeDetailPage({
  params,
}: PageProps<"/cafes/[id]">) {
  const { id } = await params;
  const cafe =
    mockCafeSummaries.find((item) => item.id === id) ?? mockCafeSummaries[0];
  const menu = mockCafeMenusByCafeId[cafe.id] ?? mockCafeMenu;

  return (
    <main className="-ml-8 min-h-screen w-[calc(100vw-64px)] max-w-none touch-pan-y overflow-x-clip bg-background sm:-ml-14 sm:w-[calc(100vw-72px)] xl:-ml-[248px]">
      <div className="mx-auto w-full max-w-[1140px] px-4 py-12 sm:px-8 xl:px-0">
        <CafePage
          cafe={cafe}
          menu={menu}
          recentReviews={mockCafeReviewPosts}
        />
      </div>
    </main>
  );
}
