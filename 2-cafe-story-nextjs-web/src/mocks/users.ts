import type {
  ActivityNotification,
  ProfileHighlight,
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

export const mockActivityNotifications: ActivityNotification[] = [
  {
    id: "activity-1",
    actor: "Jessica Brew",
    avatarInitials: "JB",
    action: "liked your review of",
    target: "The Monolith",
    time: "4 min",
    unread: true,
  },
  {
    id: "activity-2",
    actor: "Marco Explorer",
    avatarInitials: "ME",
    action: "saved your list",
    target: "Quiet cafes for deep work",
    time: "22 min",
    unread: true,
  },
  {
    id: "activity-3",
    actor: "Batch Baby",
    avatarInitials: "BB",
    action: "replied to your question about",
    target: "filter availability",
    time: "1 hr",
  },
  {
    id: "activity-4",
    actor: "Nora Cups",
    avatarInitials: "NC",
    action: "started following",
    target: "your cafe stories",
    time: "3 hr",
  },
];

export const mockSuggestedUsers: SuggestedUser[] = [
  { name: "Phan Ngoc Hien", username: "hien.coffee", initials: "PH" },
  { name: "Clara Bui", username: "clarabrews", initials: "CB" },
  { name: "Ng Khanh Ngoc", username: "ngoc.cafes", initials: "NN" },
];
