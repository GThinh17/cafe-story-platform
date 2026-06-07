import { XIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DialogClose, DialogTitle } from "@/components/ui/dialog";

type CafeMenuModalHeaderProps = {
  cafeName: string;
};

export function CafeMenuModalHeader({ cafeName }: CafeMenuModalHeaderProps) {
  return (
    <header className="sticky top-0 z-10 flex items-center justify-between gap-6 border-b border-line-soft bg-surface/90 px-6 py-5 backdrop-blur sm:px-10">
      <div className="min-w-0">
        <p className="text-[10px] font-black uppercase tracking-[0.22em] text-muted">
          Curated Selection
        </p>
        <DialogTitle className="mt-1 truncate font-serif text-3xl font-medium italic text-espresso">
          {cafeName}
        </DialogTitle>
      </div>
      <DialogClose asChild>
        <Button
          aria-label="Close menu"
          className="size-10 shrink-0 rounded-full text-espresso hover:bg-surface-muted"
          size="icon-sm"
          type="button"
          variant="ghost"
        >
          <XIcon className="size-5" />
        </Button>
      </DialogClose>
    </header>
  );
}
