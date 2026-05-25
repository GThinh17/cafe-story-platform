import { CafeSuggestionList } from "@/components/cafe/cafe-suggestion-list";
import { PageShell } from "@/components/layout/page-shell";
import { CreatePostForm } from "@/components/review/create-post-form";
import { mockCafeSummaries } from "@/mocks/cafes";
import { mockReviewComposer, mockReviewDraftHints } from "@/mocks/reviews";

export default function CreatePostPage() {
  return (
    <PageShell
      aside={<CafeSuggestionList cafes={mockCafeSummaries} />}
    >
      <CreatePostForm
        composer={mockReviewComposer}
        hints={mockReviewDraftHints}
      />
    </PageShell>
  );
}
