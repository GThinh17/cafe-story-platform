import type { CafeSummary } from "@/types/cafe";

export const mockCafeSummaries: CafeSummary[] = [
  {
    id: "grounds",
    name: "The Grounds",
    location: "Shoreditch, London",
    type: "Roastery",
    rating: "4.9",
    distance: "0.8 km",
    image:
      "https://images.unsplash.com/photo-1554118811-1e0d58224f24?auto=format&fit=crop&w=900&q=85",
    tags: ["Roastery", "Quiet", "Outdoor"],
    description:
      "A calm roastery with wide tables, soft light, and a reliable espresso bar.",
  },
  {
    id: "batch-baby",
    name: "Batch Baby",
    location: "Soho, London",
    type: "Filter Only",
    rating: "4.7",
    distance: "1.4 km",
    image:
      "https://images.unsplash.com/photo-1521017432531-fbd92d768814?auto=format&fit=crop&w=900&q=85",
    tags: ["Filter", "Minimal", "Beans"],
    description:
      "Small, bright, and serious about pour over without making the room feel cold.",
  },
  {
    id: "origin",
    name: "Origin Coffee",
    location: "King's Cross, London",
    type: "Workspace",
    rating: "4.6",
    distance: "2.1 km",
    image:
      "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=900&q=85",
    tags: ["Workspace", "Brunch", "Seats"],
    description:
      "A clean work-friendly cafe with steady service and enough room for laptops.",
  },
  {
    id: "prufrock",
    name: "Prufrock",
    location: "Farringdon, London",
    type: "Training",
    rating: "4.8",
    distance: "2.8 km",
    image:
      "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=900&q=85",
    tags: ["Training", "Espresso", "Community"],
    description:
      "A coffee school energy with polished drinks and people who like talking beans.",
  },
];
