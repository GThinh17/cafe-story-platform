import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import type { CafeEditorialCollection } from "@/types/cafe";

type EditorialCollectionCardProps = {
  collection: CafeEditorialCollection;
};

export function EditorialCollectionCard({
  collection,
}: EditorialCollectionCardProps) {
  return (
    <Card className="group relative min-h-[270px] overflow-hidden border-0 bg-espresso text-white shadow-sm">
      <img
        alt={collection.alt}
        className="absolute inset-0 h-full w-full object-cover transition duration-1000 group-hover:scale-105"
        decoding="async"
        loading="lazy"
        src={collection.image}
      />
      <div className="absolute inset-0 bg-gradient-to-t from-espresso/90 via-espresso/50 to-espresso/10" />
      <CardContent className="relative flex h-full min-h-[270px] flex-col justify-end p-7 sm:p-8">
        <p className="text-xs font-black uppercase tracking-[0.16em] text-white/85">
          {collection.eyebrow}
        </p>
        <h3 className="mt-2 max-w-sm font-serif text-3xl font-semibold leading-tight sm:text-4xl">
          {collection.title}
        </h3>
        <Button
          asChild
          className="mt-8 inline-flex h-11 w-fit items-center justify-center border border-white px-6 text-xs font-black uppercase tracking-[0.14em] text-white no-underline transition hover:bg-white hover:text-espresso"
          variant="outline"
        >
          <a href="/explore">{collection.ctaLabel}</a>
        </Button>
      </CardContent>
    </Card>
  );
}
