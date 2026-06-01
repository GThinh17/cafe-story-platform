import { EditorialCollectionCard } from "@/components/cafe/editorial-collection-card";
import { ExploreSectionHeader } from "@/components/cafe/explore-section-header";
import type { CafeEditorialCollection } from "@/types/cafe";

type EditorialCollectionsSectionProps = {
  collections: CafeEditorialCollection[];
};

export function EditorialCollectionsSection({
  collections,
}: EditorialCollectionsSectionProps) {
  return (
    <section className="space-y-7">
      <ExploreSectionHeader title="Editorial Collections" />
      <div className="grid gap-8 lg:grid-cols-2">
        {collections.map((collection) => (
          <EditorialCollectionCard
            collection={collection}
            key={collection.id}
          />
        ))}
      </div>
    </section>
  );
}
