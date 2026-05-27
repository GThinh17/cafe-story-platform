"use client";

import { ProfileHeader } from "@/components/profile/profile-header";
import { ProfileReviewGrid } from "@/components/profile/profile-review-grid";
import { useCurrentUser } from "@/hooks/use-current-user";
import {
  getUserAvatarImage,
  getUserDisplayName,
  getUserEmail,
  getUserHandle,
  getUserInitials,
} from "@/lib/avatar";
import { mockProfileReviews } from "@/mocks/reviews";
import { mockProfileHighlights, mockUserProfile } from "@/mocks/users";
import type { AuthUser } from "@/types/auth";
import type { UserProfile } from "@/types/user";

function mapAuthUserToProfile(user: AuthUser | null): UserProfile {
  if (!user) {
    return mockUserProfile;
  }

  const handle = getUserHandle(user);
  const displayName = getUserDisplayName(user);
  const email = getUserEmail(user);

  return {
    ...mockUserProfile,
    avatarImage: getUserAvatarImage(user),
    avatarInitials: getUserInitials(user),
    bio: email ? `Email: ${email}` : mockUserProfile.bio,
    displayName,
    email,
    username: handle,
    website: `cafestory.vn/${handle}`,
  };
}

export function ProfilePageContent() {
  const { user, isLoading } = useCurrentUser();
  const profile = mapAuthUserToProfile(user);

  return (
    <>
      <ProfileHeader
        highlights={mockProfileHighlights}
        isLoading={isLoading}
        profile={profile}
      />
      <ProfileReviewGrid reviews={mockProfileReviews} />
    </>
  );
}
