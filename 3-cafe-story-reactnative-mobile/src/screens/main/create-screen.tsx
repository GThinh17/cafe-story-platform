import * as ImagePicker from "expo-image-picker";
import { useCallback, useEffect, useState } from "react";
import { StyleSheet, Text, View } from "react-native";
import { useNavigation } from "@react-navigation/native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";

import {
  CreatePostComposeStep,
  CreatePostHeader,
  CreatePostLocationPickerModal,
  CreatePostPeoplePickerModal,
  CreatePostSettingsStep,
  Screen,
} from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { MainTabParamList } from "../../navigation";
import {
  createBlog,
  getMyProfile,
  isRemoteImageUrl,
  uploadPostImageToCloudinary,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { CreatePostDraft, UserResponse } from "../../types";

type CreatePostStep = "compose" | "settings";

const initialDraft: CreatePostDraft = {
  allowComments: true,
  caption: "",
  mediaUrls: [],
  pinToProfile: false,
  taggedUserIds: [],
  tags: [],
  visibility: "PUBLIC",
};

function locationNameFromUser(user: {
  regionCity?: string | null;
  regionId?: string | null;
  regionProvince?: string | null;
} | null) {
  if (!user?.regionId) {
    return undefined;
  }

  const name = [user.regionCity, user.regionProvince]
    .filter(Boolean)
    .join(", ");

  return {
    name: name || "Profile location",
    regionId: user.regionId,
  };
}

function uploadFileNameFromUri(uri: string, index: number) {
  const pathName = uri.split(/[?#]/)[0] ?? "";
  const fileName = pathName.split("/").pop();

  return fileName?.includes(".") ? fileName : `post-${Date.now()}-${index}.jpg`;
}

export function CreateScreen() {
  const navigation = useNavigation<BottomTabNavigationProp<MainTabParamList>>();
  const { user } = useAuth();
  const [currentStep, setCurrentStep] = useState<CreatePostStep>("compose");
  const [draft, setDraft] = useState<CreatePostDraft>({
    ...initialDraft,
    location: locationNameFromUser(user),
  });
  const [selectedTaggedUsers, setSelectedTaggedUsers] = useState<UserResponse[]>([]);
  const [isPeoplePickerVisible, setIsPeoplePickerVisible] = useState(false);
  const [isLocationPickerVisible, setIsLocationPickerVisible] = useState(false);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const canContinue = Boolean(draft.caption.trim());
  const isSettingsStep = currentStep === "settings";

  useEffect(() => {
    if (draft.location?.regionId) {
      return;
    }

    const fallbackLocation = locationNameFromUser(user);
    if (fallbackLocation) {
      setDraft((currentDraft) => ({
        ...currentDraft,
        location: fallbackLocation,
      }));
      return;
    }

    let isActive = true;
    getMyProfile()
      .then((profile) => {
        const profileLocation = locationNameFromUser(profile);
        if (isActive && profileLocation) {
          setDraft((currentDraft) => ({
            ...currentDraft,
            location: profileLocation,
          }));
        }
      })
      .catch(() => {
        // The create flow can still render; posting will ask for location if missing.
      });

    return () => {
      isActive = false;
    };
  }, [draft.location?.regionId, user]);

  const updateDraft = useCallback((patch: Partial<CreatePostDraft>) => {
    setDraft((currentDraft) => ({
      ...currentDraft,
      ...patch,
    }));
    setError("");
  }, []);

  const resetDraft = useCallback(() => {
    setDraft({
      ...initialDraft,
      location: locationNameFromUser(user),
    });
    setSelectedTaggedUsers([]);
    setCurrentStep("compose");
    setError("");
  }, [user]);

  const handleCancel = useCallback(() => {
    resetDraft();
    navigation.navigate(routes.home);
  }, [navigation, resetDraft]);

  const handleNext = useCallback(() => {
    if (!draft.caption.trim()) {
      setError("Write a caption before continuing.");
      return;
    }

    setError("");
    setCurrentStep("settings");
  }, [draft.caption]);

  const handleBack = useCallback(() => {
    setError("");
    setCurrentStep("compose");
  }, []);

  const handleAddMedia = useCallback(async () => {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();

    if (!permission.granted) {
      setError("Photo access is required to add images.");
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      allowsEditing: false,
      allowsMultipleSelection: true,
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.86,
      selectionLimit: 8,
    });

    if (result.canceled) {
      return;
    }

    const uris = result.assets
      .map((asset) => asset.uri)
      .filter((uri): uri is string => Boolean(uri));

    if (!uris.length) {
      return;
    }

    setDraft((currentDraft) => ({
      ...currentDraft,
      mediaUrls: [...currentDraft.mediaUrls, ...uris].slice(0, 8),
    }));
    setError("");
  }, []);

  const handleToggleTag = useCallback((tag: string) => {
    setDraft((currentDraft) => {
      const tags = currentDraft.tags.includes(tag)
        ? currentDraft.tags.filter((item) => item !== tag)
        : [...currentDraft.tags, tag];

      return {
        ...currentDraft,
        tags,
      };
    });
  }, []);

  const handleApplyTaggedUsers = useCallback((profiles: UserResponse[]) => {
    setSelectedTaggedUsers(profiles);
    updateDraft({
      taggedUserIds: profiles.map((profile) => profile.userId),
    });
  }, [updateDraft]);

  const handleApplyLocation = useCallback(
    (location: NonNullable<CreatePostDraft["location"]>) => {
      updateDraft({ location });
    },
    [updateDraft],
  );

  const handlePost = useCallback(async () => {
    if (!draft.caption.trim()) {
      setError("Write a caption before posting.");
      setCurrentStep("compose");
      return;
    }

    if (!draft.location?.regionId) {
      setError("Add your profile location before posting.");
      return;
    }

    setIsSubmitting(true);
    setError("");

    try {
      const imageUrls = await Promise.all(
        draft.mediaUrls.map((uri, index) =>
          isRemoteImageUrl(uri)
            ? Promise.resolve(uri)
            : uploadPostImageToCloudinary({
              name: uploadFileNameFromUri(uri, index),
              uri,
            }),
        ),
      );

      await createBlog({
        allowComment: draft.allowComments,
        content: draft.caption.trim(),
        imageUrls,
        isPinned: draft.pinToProfile,
        pageId: draft.cafePageId,
        regionId: draft.location?.regionId,
        taggedUserIds: draft.taggedUserIds,
      });
      resetDraft();
      navigation.navigate(routes.home);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to create post.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }, [draft, navigation, resetDraft]);

  return (
    <Screen padded={false}>
      <CreatePostHeader
        actionDisabled={isSettingsStep ? isSubmitting : !canContinue}
        actionLabel={isSettingsStep ? "Post" : "Next"}
        isBack={isSettingsStep}
        isSubmitting={isSubmitting}
        onAction={isSettingsStep ? handlePost : handleNext}
        onLeftPress={isSettingsStep ? handleBack : handleCancel}
        title={isSettingsStep ? "Post Settings" : "New Post"}
      />

      {error ? (
        <View style={styles.errorBanner}>
          <Text style={styles.errorText}>{error}</Text>
        </View>
      ) : null}

      {isSettingsStep ? (
        <CreatePostSettingsStep
          draft={draft}
          onUpdateDraft={updateDraft}
          user={user}
        />
      ) : (
        <CreatePostComposeStep
          draft={draft}
          onAddMedia={handleAddMedia}
          onOpenLocationPicker={() => setIsLocationPickerVisible(true)}
          onOpenPeoplePicker={() => setIsPeoplePickerVisible(true)}
          onRemoveMedia={() => updateDraft({ mediaUrls: [] })}
          onToggleTag={handleToggleTag}
          onUpdateDraft={updateDraft}
          selectedTaggedUsers={selectedTaggedUsers}
          user={user}
        />
      )}

      <CreatePostPeoplePickerModal
        currentUserId={user?.userId}
        onApply={handleApplyTaggedUsers}
        onClose={() => setIsPeoplePickerVisible(false)}
        selectedUserIds={draft.taggedUserIds}
        visible={isPeoplePickerVisible}
      />

      <CreatePostLocationPickerModal
        onApply={handleApplyLocation}
        onClose={() => setIsLocationPickerVisible(false)}
        visible={isLocationPickerVisible}
      />
    </Screen>
  );
}

const styles = StyleSheet.create({
  errorBanner: {
    backgroundColor: colors.secondarySoft,
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
  },
  errorText: {
    color: colors.primaryStrong,
    fontSize: typography.label,
    fontWeight: "800",
    lineHeight: 20,
    textAlign: "center",
  },
});
