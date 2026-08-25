import type { ReactNode } from "react";

type PageShellProps = {
  title?: string;
  description?: string;
  children: ReactNode;
  aside?: ReactNode;
};

export function PageShell({ title, description, children, aside }: PageShellProps) {
  return (
    <main className="grid w-full max-w-[1120px] touch-pan-y grid-cols-1 gap-14 overflow-x-clip px-4 py-8 sm:px-8 xl:ml-12 xl:grid-cols-[630px_320px] xl:px-0 2xl:ml-20">
      <section className="w-full max-w-[630px] space-y-8">
        {title || description ? (
          <header className="space-y-2">
            {title ? (
              <h1 className="text-3xl font-black text-foreground">{title}</h1>
            ) : null}
            {description ? (
              <p className="max-w-[520px] text-sm leading-6 text-muted">
                {description}
              </p>
            ) : null}
          </header>
        ) : null}
        {children}
      </section>

      {aside ? (
        <aside className="sticky top-8 hidden h-fit w-full xl:block">{aside}</aside>
      ) : null}
    </main>
  );
}
