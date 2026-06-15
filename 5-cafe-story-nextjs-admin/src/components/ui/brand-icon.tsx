import type { ComponentPropsWithoutRef } from "react";

type BrandIconProps = Omit<ComponentPropsWithoutRef<"img">, "alt" | "src"> & {
  decorative?: boolean;
};

const brandIconSrc = "/icons/cafestory-brand-icon.svg";

export function BrandIcon({
  className,
  decorative = true,
  ...props
}: BrandIconProps) {
  return (
    <img
      alt={decorative ? "" : "Cafe Story"}
      aria-hidden={decorative ? "true" : undefined}
      className={`block shrink-0 rounded-full ${className ?? ""}`}
      decoding="async"
      src={brandIconSrc}
      {...props}
    />
  );
}
