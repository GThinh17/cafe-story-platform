import { CafePageContent } from "@/components/cafe/cafe-page-content";

export default async function CafeDetailPage({
  params,
}: PageProps<"/cafes/[id]">) {
  const { id } = await params;

  return (
    <main className="-ml-8 min-h-screen w-[calc(100vw-64px)] max-w-none touch-pan-y overflow-x-clip bg-background sm:-ml-14 sm:w-[calc(100vw-72px)] xl:-ml-[248px]">
      <div className="mx-auto w-full max-w-[1140px] px-4 py-12 sm:px-8 xl:px-0">
        <CafePageContent cafePageId={id} />
      </div>
    </main>
  );
}
