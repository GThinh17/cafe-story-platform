import { CafePageContent } from "@/components/cafe/cafe-page-content";

type CafeDetailPageProps = {
  params: Promise<{
    id: string;
  }>;
};

export default async function CafeDetailPage({
  params,
}: CafeDetailPageProps) {
  const { id } = await params;

  return (
    <main className="min-h-screen w-screen max-w-none touch-pan-y overflow-x-clip bg-background sm:-ml-28 xl:-ml-80">
      <div className="mx-auto w-full max-w-[1140px] px-4 py-12 sm:px-8 xl:px-0">
        <CafePageContent cafePageId={id} />
      </div>
    </main>
  );
}
