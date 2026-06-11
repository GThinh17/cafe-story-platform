import { UserRound } from "lucide-react-native";
import { useEffect, useState } from "react";
import { Image, StyleSheet, View } from "react-native";
import { colors } from "../../theme";

type AvatarProps = {
  initials?: string;
  size?: number;
  uri?: string | null;
};

export function Avatar({ size = 48, uri }: AvatarProps) {
  const [imageFailed, setImageFailed] = useState(false);
  const sourceUri = uri?.trim() ? uri : null;
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
      <UserRound
        color={colors.primaryStrong}
        size={Math.max(18, size * 0.52)}
        strokeWidth={2.4}
      />
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
});
