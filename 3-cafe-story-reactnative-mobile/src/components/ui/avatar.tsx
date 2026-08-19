import { UserRound } from "lucide-react-native";
import { Text } from "react-native";
import { useEffect, useState } from "react";
import { Image, StyleSheet, View } from "react-native";
import { colors, typography } from "../../theme";
import { resolveAvatarImageUri } from "../../utils/avatar-image";
import { t } from "../../features/i18n";

type AvatarProps = {
  initials?: string;
  size?: number;
  uri?: string | null;
};

export function Avatar({ initials, size = 48, uri }: AvatarProps) {
  const [imageFailed, setImageFailed] = useState(false);
  const sourceUri = resolveAvatarImageUri(uri, size);
  const avatarStyle = {
    borderRadius: size / 2,
    height: size,
    width: size,
  };

  useEffect(() => {
    setImageFailed(false);
  }, [sourceUri]);

  if (sourceUri && !imageFailed) {
    return (
      <Image
        onError={() => setImageFailed(true)}
        source={{ uri: sourceUri }}
        style={[styles.avatar, avatarStyle]}
      />
    );
  }

  return (
    <View style={[styles.avatar, styles.fallback, avatarStyle]}>
      {initials ? (
        <Text
          numberOfLines={1}
          style={[
            styles.initials,
            { fontSize: Math.max(typography.caption, size * 0.32) },
          ]}
        >
          {initials}
        </Text>
      ) : (
        <UserRound
          color={colors.primaryStrong}
          size={Math.max(18, size * 0.52)}
          strokeWidth={2.4}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  avatar: {
    overflow: "hidden",
  },
  fallback: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderColor: colors.border,
    borderWidth: 1,
    justifyContent: "center",
  },
  initials: {
    color: colors.primaryStrong,
    fontWeight: "900",
  },
});
