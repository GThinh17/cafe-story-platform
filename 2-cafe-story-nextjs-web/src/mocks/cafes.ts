import type {
  CafeCategory,
  CafeEditorialCollection,
  CafeMenu,
  CafeSummary,
} from "@/types/cafe";

export const mockCafeSummaries: CafeSummary[] = [
  {
    id: "velvet-roast",
    name: "The Velvet Roast",
    location: "East Village, New York",
    address: "1242 Artisan Way, East Village, New York, NY 10003",
    type: "Specialty Coffee & Roastery",
    rating: "4.9",
    reviewCount: "1,248",
    distance: "0.8 km",
    priceLevel: "$$",
    hours: "Opens 7:00 AM",
    status: "Closed",
    photoCount: "1,248",
    image: "/images/cafes/velvet-roast/latte-art.jpg",
    coverImage: "/images/cafes/velvet-roast/minimal-interior.jpg",
    coverImageAlt:
      "Minimal bright cafe interior with tall windows and olive trees",
    gallery: [
      "/images/cafes/velvet-roast/minimal-interior.jpg",
      "/images/cafes/velvet-roast/croissant-flatlay.jpg",
      "/images/cafes/velvet-roast/espresso-machine.jpg",
      "/images/cafes/velvet-roast/reading-table.jpg",
      "/images/cafes/velvet-roast/white-facade.jpg",
      "/images/cafes/velvet-roast/pour-over.jpg",
      "/images/cafes/velvet-roast/bean-jars.jpg",
      "/images/cafes/velvet-roast/cozy-corner.jpg",
    ],
    tags: ["Good for work", "Outdoor seating", "High-speed WiFi", "Pet friendly", "Quiet atmosphere"],
    amenities: ["Good for work", "Outdoor seating", "High-speed WiFi", "Pet friendly", "Quiet atmosphere"],
    popularDrinks: ["View menu", "Espresso", "Pour-over"],
    description:
      "A minimalist haven for specialty coffee lovers. The Velvet Roast features floor-to-ceiling windows, communal oak tables, and a dedicated slow-bar for pour-overs.",
    featureSummary:
      "A minimalist haven for specialty coffee lovers. The Velvet Roast features floor-to-ceiling windows, communal oak tables, and a dedicated slow-bar for pour-overs. It's the perfect spot for deep work or a contemplative morning espresso.",
    peakHours: "Peak hours usually 10 AM - 1 PM",
    openingHours: [
      { day: "Mon - Fri", time: "7:00 AM - 6:00 PM" },
      { day: "Saturday", time: "8:00 AM - 7:00 PM", highlight: true },
      { day: "Sunday", time: "8:00 AM - 5:00 PM" },
    ],
    communityPhotos: [
      {
        image: "/images/cafes/velvet-roast/latte-art.jpg",
        alt: "Latte art in a dark ceramic cup on a sunlit wooden table",
      },
      {
        image: "/images/cafes/velvet-roast/minimal-interior.jpg",
        alt: "Minimal cafe interior with tall windows and light wooden tables",
      },
      {
        image: "/images/cafes/velvet-roast/croissant-flatlay.jpg",
        alt: "Croissant and black coffee on a white plate",
      },
      {
        image: "/images/cafes/velvet-roast/espresso-machine.jpg",
        alt: "Espresso pouring from a professional machine",
      },
      {
        image: "/images/cafes/velvet-roast/reading-table.jpg",
        alt: "Open book and espresso on an outdoor cafe table",
      },
      {
        image: "/images/cafes/velvet-roast/white-facade.jpg",
        alt: "Minimal white cafe facade with a glass door",
      },
      {
        image: "/images/cafes/velvet-roast/pour-over.jpg",
        alt: "Pour-over coffee being brewed with steam rising",
      },
      {
        image: "/images/cafes/velvet-roast/bean-jars.jpg",
        alt: "Specialty coffee beans displayed in labeled glass jars",
      },
      {
        image: "/images/cafes/velvet-roast/cozy-corner.jpg",
        alt: "Cozy evening cafe corner with a leather chair and warm lamp",
      },
    ],
  },
  {
    id: "batch-baby",
    name: "Batch Baby",
    location: "Soho, London",
    address: "12 Brewer Street, Soho",
    type: "Filter Only",
    rating: "4.7",
    reviewCount: "890",
    distance: "1.4 km",
    priceLevel: "$$",
    hours: "Open until 8:30 PM",
    image:
      "https://images.unsplash.com/photo-1521017432531-fbd92d768814?auto=format&fit=crop&w=900&q=85",
    gallery: [
      "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=85",
      "https://images.unsplash.com/photo-1498804103079-a6351b050096?auto=format&fit=crop&w=900&q=85",
      "https://images.unsplash.com/photo-1514066558159-fc8c737ef259?auto=format&fit=crop&w=900&q=85",
    ],
    tags: ["Filter", "Minimal", "Beans"],
    amenities: ["Pour over bar", "Beans retail", "Quiet counter"],
    popularDrinks: ["Kenya filter", "Iced long black", "Espresso tonic"],
    description:
      "Small, bright, and serious about pour over without making the room feel cold.",
  },
  {
    id: "origin",
    name: "Origin Coffee",
    location: "King's Cross, London",
    address: "96 Euston Road, King's Cross",
    type: "Workspace",
    rating: "4.6",
    reviewCount: "1.8k",
    distance: "2.1 km",
    priceLevel: "$$",
    hours: "Open until 9:00 PM",
    image:
      "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=900&q=85",
    gallery: [
      "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=900&q=85",
      "https://images.unsplash.com/photo-1554118811-1e0d58224f24?auto=format&fit=crop&w=900&q=85",
      "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=900&q=85",
    ],
    tags: ["Workspace", "Brunch", "Seats"],
    amenities: ["Wifi", "Sockets", "Brunch", "Large tables"],
    popularDrinks: ["Flat white", "Cold brew", "Mocha"],
    description:
      "A clean work-friendly cafe with steady service and enough room for laptops.",
  },
  {
    id: "prufrock",
    name: "Prufrock",
    location: "Farringdon, London",
    address: "23 Leather Lane, Farringdon",
    type: "Training",
    rating: "4.8",
    reviewCount: "1.2k",
    distance: "2.8 km",
    priceLevel: "$$",
    hours: "Open until 7:00 PM",
    image:
      "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=900&q=85",
    gallery: [
      "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=900&q=85",
      "https://images.unsplash.com/photo-1521017432531-fbd92d768814?auto=format&fit=crop&w=900&q=85",
      "https://images.unsplash.com/photo-1442512595331-e89e73853f31?auto=format&fit=crop&w=900&q=85",
    ],
    tags: ["Training", "Espresso", "Community"],
    amenities: ["Coffee classes", "Beans retail", "Espresso bar"],
    popularDrinks: ["House espresso", "Cappuccino", "Filter flight"],
    description:
      "A coffee school energy with polished drinks and people who like talking beans.",
  },
];

export const mockFeaturedCafe = mockCafeSummaries[0];

export const mockCafeCategories: CafeCategory[] = [
  {
    id: "vintage-vibes",
    label: "Vintage vibes",
    icon: "sparkle",
  },
  {
    id: "workspace-ready",
    label: "Workspace ready",
    icon: "laptop",
  },
  {
    id: "specialty-brews",
    label: "Specialty brews",
    icon: "coffee",
  },
  {
    id: "hidden-gems",
    label: "Hidden gems",
    icon: "gem",
  },
  {
    id: "outdoor-patios",
    label: "Outdoor patios",
    icon: "leaf",
  },
];

export const mockCafeEditorialCollections: CafeEditorialCollection[] = [
  {
    id: "rainy-day-spots",
    eyebrow: "Featured list",
    title: "Top 5 Rainy Day Spots",
    ctaLabel: "Explore list",
    image: "/images/cafes/velvet-roast/reading-table.jpg",
    alt: "A quiet cafe table by a window on a rainy day",
  },
  {
    id: "espresso-excellence",
    eyebrow: "Awards 2026",
    title: "Espresso Excellence",
    ctaLabel: "Read more",
    image: "/images/cafes/velvet-roast/pour-over.jpg",
    alt: "Coffee being brewed with warm light and steam",
  },
];

const mockVelvetMenuImages = {
  latte: {
    src: "/images/cafes/velvet-roast/latte-art.jpg",
    alt: "Velvet latte with leaf latte art",
    label: "Velvet Latte",
  },
  coldBrew: {
    src: "/images/cafes/velvet-roast/pour-over.jpg",
    alt: "Tall cold brew on a pale wood table",
    label: "Nitro Cold Brew",
  },
  croissant: {
    src: "/images/cafes/velvet-roast/croissant-flatlay.jpg",
    alt: "Almond croissant dusted with sugar",
  },
  brownie: {
    src: "/images/cafes/velvet-roast/cozy-corner.jpg",
    alt: "Dark chocolate brownie on a cafe table",
  },
};

export const mockCafeMenu: CafeMenu = {
  signature: [
    {
      id: "velvet-latte",
      name: "Velvet Latte",
      price: 6.5,
      description:
        "Signature blend with steamed milk & house-made vanilla bean syrup.",
      image: mockVelvetMenuImages.latte.src,
      images: [
        mockVelvetMenuImages.latte,
        mockVelvetMenuImages.coldBrew,
        mockVelvetMenuImages.croissant,
        mockVelvetMenuImages.brownie,
      ],
      badge: "Barista's Choice",
    },
    {
      id: "nitro-cold-brew",
      name: "Nitro Cold Brew",
      price: 7,
      description:
        "12-hour steep, infused with nitrogen for a creamy, cascading finish.",
      image: mockVelvetMenuImages.coldBrew.src,
      images: [
        mockVelvetMenuImages.latte,
        mockVelvetMenuImages.coldBrew,
        mockVelvetMenuImages.croissant,
        mockVelvetMenuImages.brownie,
      ],
    },
  ],
  seasonal: [
    {
      id: "autumn-spiced-flat-white",
      name: "Autumn Spiced Flat White",
      price: 6,
      description: "Cinnamon, nutmeg, and a hint of maple.",
    },
    {
      id: "cardamom-rose-cortado",
      name: "Cardamom Rose Cortado",
      price: 5.5,
      description: "Floral notes perfectly balanced with rich espresso.",
    },
  ],
  classics: [
    {
      id: "americano",
      name: "Americano",
      price: 4.5,
      description: "Double shot of espresso with hot water",
    },
    {
      id: "cappuccino",
      name: "Cappuccino",
      price: 5.5,
      description: "Equal parts espresso, steamed milk, and silky foam",
    },
    {
      id: "macchiato",
      name: "Macchiato",
      price: 4,
      description: "Double espresso marked with a dollop of foam",
    },
    {
      id: "pour-over",
      name: "Pour Over",
      price: 6,
      description: "Single origin, slow-brewed to perfection",
    },
  ],
  pastries: [
    {
      id: "almond-croissant",
      name: "Almond Croissant",
      price: 5,
      description: "Twice-baked with almond frangipane & toasted flakes",
      image: mockVelvetMenuImages.croissant.src,
      badge: "Chef's Pick",
    },
    {
      id: "sea-salt-brownie",
      name: "Sea Salt Brownie",
      price: 4.5,
      description: "Dark chocolate Valrhona with Maldon sea salt",
      image: mockVelvetMenuImages.brownie.src,
    },
  ],
};

export const mockCafeMenusByCafeId: Record<string, CafeMenu> = {
  "velvet-roast": mockCafeMenu,
};
