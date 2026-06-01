import {
  CoffeeIcon,
  GemIcon,
  LaptopIcon,
  LeafIcon,
  MapPinIcon,
  SearchIcon,
  SparklesIcon,
  StarIcon,
} from "lucide-react";
import type { CafeCategory } from "@/types/cafe";

export type ExploreIconName = CafeCategory["icon"] | "pin" | "search" | "star";

const exploreIcons = {
  coffee: CoffeeIcon,
  gem: GemIcon,
  laptop: LaptopIcon,
  leaf: LeafIcon,
  pin: MapPinIcon,
  search: SearchIcon,
  sparkle: SparklesIcon,
  star: StarIcon,
};

type ExploreIconProps = {
  name: ExploreIconName;
  className?: string;
};

export function ExploreIcon({ name, className }: ExploreIconProps) {
  const Icon = exploreIcons[name];

  return <Icon aria-hidden="true" className={className ?? "size-4"} />;
}
