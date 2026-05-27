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
    <main className="min-h-screen w-full overflow-x-clip bg-background text-foreground">
      <div className="grid min-h-screen w-full grid-cols-1 lg:grid-cols-2">
        <section className="relative min-h-[340px] overflow-hidden bg-espresso lg:min-h-screen">
          <img
            alt="Small cafe table with a coffee cup in warm light"
            className="absolute inset-0 h-full w-full scale-[1.04] object-cover object-center"
            decoding="async"
            src={authHeroImage}
          />
          <div className="absolute inset-0 bg-espresso/25" />
          <div className="absolute inset-0 bg-gradient-to-t from-espresso/72 via-espresso/12 to-transparent" />

          <div className="relative flex h-full min-h-[340px] flex-col justify-between p-6 sm:p-10 lg:min-h-screen lg:p-16">
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
              <h2 className="text-4xl font-black leading-tight text-white sm:text-5xl">
                Cafe Story
              </h2>
              <p className="text-base leading-7 text-white/88 sm:text-lg">
                Where every cafe tells a story, and every cup keeps a small
                memory warm.
              </p>
            </div>
          </div>
        </section>

        <section className="flex min-h-screen items-center justify-center bg-surface px-5 py-12 sm:px-10 lg:px-16">
          <div className="w-full max-w-[400px]">
            <div className="mb-12 lg:hidden">
              <p className="text-lg font-black text-espresso">Cafe Story</p>
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
