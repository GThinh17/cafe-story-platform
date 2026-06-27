import type { ReactNode } from "react";
import { BrandIcon } from "@/components/ui/brand-icon";
import Link from "next/link";

type AuthLayoutShellProps = {
  children: ReactNode;
};

const authHeroImage =
  "https://images.unsplash.com/photo-1758204067856-c910003bc5ba?auto=format&fit=crop&fm=jpg&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&ixlib=rb-4.1.0&q=80&w=1800";

export function AuthLayoutShell({ children }: AuthLayoutShellProps) {
  return (
    <main className="fixed inset-0 overflow-hidden bg-background text-foreground">
      <div className="grid h-full min-h-0 w-full grid-cols-1 grid-rows-[260px_minmax(0,1fr)] overflow-hidden lg:grid-cols-2 lg:grid-rows-1">
        <section className="relative h-full min-h-0 overflow-hidden bg-espresso">
          <img
            alt="Small cafe table with a coffee cup in warm light"
            className="absolute inset-0 h-full w-full scale-[1.04] object-cover object-center"
            decoding="async"
            src={authHeroImage}
          />
          <div className="absolute inset-0 bg-espresso/25" />
          <div className="absolute inset-0 bg-gradient-to-t from-espresso/72 via-espresso/12 to-transparent" />

          <div className="relative flex h-full min-h-0 flex-col justify-between p-6 sm:p-10 lg:p-16">
            <Link
              aria-label="Cafe Story home"
              className="grid size-12 place-items-center rounded-full bg-surface/85 p-1 shadow-sm no-underline backdrop-blur"
              href="/"
            >
              <BrandIcon className="size-10" />
            </Link>

            <div className="flex max-w-[390px] flex-col gap-3">
              <p className="text-xs font-bold uppercase tracking-[0.16em] text-white/75">
                Est. 2026
              </p>
              <img
                alt="CaféStory"
                className="h-12 w-auto object-contain brightness-0 invert sm:h-14"
                decoding="async"
                src="/icons/cafestory-wordmark.png"
              />
              <p className="text-base leading-7 text-white/88 sm:text-lg">
                Where every cafe tells a story, and every cup keeps a small
                memory warm.
              </p>
            </div>
          </div>
        </section>

        <section className="flex h-full min-h-0 justify-center overflow-y-auto bg-surface px-5 py-10 sm:px-10 lg:px-16">
          <div className="my-auto w-full max-w-[400px]">
            <div className="mb-12 lg:hidden">
              <img
                alt="CaféStory"
                className="h-8 w-auto object-contain [mix-blend-mode:multiply]"
                decoding="async"
                src="/icons/cafestory-wordmark.png"
              />
              <p className="mt-2 text-sm leading-6 text-coffee-muted">
                Coffee moments, collected.
              </p>
            </div>
            {children}
          </div>
        </section>
      </div>
    </main>
  );
}
