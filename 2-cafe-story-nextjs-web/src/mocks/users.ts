import type {
  ActivityNotification,
  ProfileHighlight,
  ReviewerProfile,
  SuggestedUser,
  UserProfile,
} from "@/types/user";

export const mockUserProfile: UserProfile = {
  displayName: "Gia Thinh",
  username: "gthinh_1704",
  avatarInitials: "GT",
  avatarImage:
    "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=320&q=85",
  bio: "Collecting calm cafes, good filters, and corners worth returning to.",
  location: "Ho Chi Minh City",
  website: "cafestory.vn/gthinh",
  stats: {
    posts: "42",
    cafes: "118",
    followers: "3.2k",
  },
};

export const mockProfileHighlights: ProfileHighlight[] = [
  {
    id: "quiet",
    label: "Quiet",
    image:
      "https://images.unsplash.com/photo-1514066558159-fc8c737ef259?auto=format&fit=crop&w=160&q=80",
  },
  {
    id: "roasters",
    label: "Roasters",
    image:
      "https://images.unsplash.com/photo-1442512595331-e89e73853f31?auto=format&fit=crop&w=160&q=80",
  },
  {
    id: "work",
    label: "Work",
    image:
      "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=160&q=80",
  },
  {
    id: "saved",
    label: "Saved",
    image:
      "https://images.unsplash.com/photo-1554118811-1e0d58224f24?auto=format&fit=crop&w=160&q=80",
  },
];

export const mockReviewerProfiles: ReviewerProfile[] = [
  {
    displayName: "Julianne Moore",
    username: "julianne.moore",
    title: "Coffee Connoisseur",
    badge: "Gold Reviewer",
    avatarImage: "/images/reviewers/julianne/portrait.jpg",
    bio: "Exploring the hidden alcoves of NYC one cortado at a time. Obsessed with natural light, vintage espresso machines, and perfectly laminated pastries.",
    location: "Brooklyn, New York",
    stats: {
      reviews: "142",
      followers: "8.4k",
      following: "612",
    },
    visualDiary: [
      {
        image: "/images/reviewers/julianne/diary-white-cup.jpg",
        alt: "Minimalist white ceramic cup of coffee on a concrete surface",
      },
      {
        image: "/images/reviewers/julianne/diary-typewriter.jpg",
        alt: "Vintage typewriter and a glass of cold brew in a cafe corner",
      },
      {
        image: "/images/reviewers/julianne/diary-latte.jpg",
        alt: "Latte art in a teal ceramic cup on a wooden table",
      },
      {
        image: "/images/reviewers/julianne/diary-facade.jpg",
        alt: "Boutique cafe exterior with black metal framing and plants",
      },
      {
        image: "/images/reviewers/julianne/diary-steam.jpg",
        alt: "Warm light filtering through coffee steam",
      },
      {
        image: "/images/reviewers/julianne/diary-espresso-bar.jpg",
        alt: "Modern coffee bar with espresso equipment",
      },
    ],
  },
];

export const mockActivityNotifications: ActivityNotification[] = [
  {
    id: "activity-1",
    section: "thisMonth",
    actors: ["ndk21.01", "_honeybee.kittypie_"],
    avatarImages: [
      "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&q=85",
      "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=120&q=85",
    ],
    message: "đã thích tin của bạn.",
    date: "May 06",
    thumbnailImage:
      "https://images.unsplash.com/photo-1521017432531-fbd92d768814?auto=format&fit=crop&w=160&q=85",
    unread: true,
  },
  {
    id: "activity-2",
    section: "earlier",
    actors: ["ndk21.01", "_honeybee.kittypie_"],
    avatarImages: [
      "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&q=85",
      "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=120&q=85",
    ],
    message: "và 2 người khác đã thích tin của bạn.",
    date: "Apr 19",
    thumbnailImage:
      "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=160&q=85",
  },
  {
    id: "activity-3",
    section: "earlier",
    actors: [],
    avatarImages: [],
    message: "Bạn có 1 người theo dõi mới trên Threads.",
    date: "Mar 25",
    iconLabel: "threads",
  },
];

export const mockSuggestedUsers: SuggestedUser[] = [
  { name: "Phan Ngoc Hien", username: "hien.coffee", initials: "PH" },
  { name: "Clara Bui", username: "clarabrews", initials: "CB" },
  { name: "Ng Khanh Ngoc", username: "ngoc.cafes", initials: "NN" },
];
