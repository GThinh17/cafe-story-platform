import { BlogDetailReview } from "@/components/review/blog-detail-review";
import { MessageDock } from "@/components/message/message-dock";
import { mockMessageDock } from "@/mocks/messages";
import { mockBlogDetailReviews } from "@/mocks/reviews";

export default async function BlogDetailPage({
  params,
}: PageProps<"/blogs/[id]">) {
  const { id } = await params;
  const review =
    mockBlogDetailReviews.find((item) => item.id === id) ??
    mockBlogDetailReviews[0];

  return (
    <main className="min-h-screen w-full max-w-full touch-pan-y overflow-x-clip bg-background px-4 py-10 sm:px-8 xl:-ml-24 xl:w-[calc(100%+96px)]">
      <div className="mx-auto w-full max-w-[1100px]">
        <BlogDetailReview review={review} />
      </div>

      <div className="fixed bottom-8 right-6 z-40 hidden xl:block 2xl:right-10">
        <MessageDock data={mockMessageDock} />
      </div>
    </main>
  );
}
