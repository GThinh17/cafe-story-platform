import { Image, StyleSheet, Text, View } from "react-native";
import { colors } from "../../theme";

type AvatarProps = {
  initials?: string;
  size?: number;
  uri?: string | null;
};

export function Avatar({ initials = "CS", size = 48, uri }: AvatarProps) {
  const avatarStyle = {
    borderRadius: size / 2,
    height: size,
    width: size,
  };

  if (uri) {
    return (
      <Image
        source={{ uri }}
        style={[styles.avatar, avatarStyle]}
      />
    );
  }

  return (
    <View style={[styles.avatar, styles.fallback, avatarStyle]}>
      <Text style={styles.initials}>{initials}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  avatar: {
    overflow: "hidden",
  },
  fallback: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    justifyContent: "center",
  },
  initials: {
    color: colors.primaryStrong,
    fontWeight: "900",
  },
});
