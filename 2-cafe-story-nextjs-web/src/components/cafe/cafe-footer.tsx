import { Separator } from "@/components/ui/separator";

export function CafeFooter() {
  return (
    <footer className="py-14 text-center">
      <Separator className="mb-14" />
      <img
        alt=""
        className="mx-auto h-10 w-10 opacity-50"
        decoding="async"
        src="/icons/cafestory-brand-icon.svg"
      />
      <p className="mt-5 font-serif text-2xl italic text-espresso">
        Cafe Story
      </p>
      <p className="mt-3 text-xs font-black uppercase tracking-[0.16em] text-muted">
        Curating the fine art of coffee
      </p>
    </footer>
  );
}
