import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center rounded-md px-3 py-1 text-xs font-medium transition",
  {
    variants: {
      variant: {
        default: "bg-primary text-white",
        secondary: "bg-surface-muted text-primary-strong",
        outline: "border border-border bg-surface text-foreground",
        rating: "bg-rating/15 text-rating",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  },
);

function Badge({
  className,
  variant,
  ...props
}: React.ComponentProps<"span"> & VariantProps<typeof badgeVariants>) {
  return (
    <span className={cn(badgeVariants({ variant, className }))} {...props} />
  );
}

export { Badge, badgeVariants };
