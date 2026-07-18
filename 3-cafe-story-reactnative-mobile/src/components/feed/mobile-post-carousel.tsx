import { ImageOff } from "lucide-react-native";
import { useMemo, useState } from "react";
import {
  FlatList,
  Image,
  type ImageStyle,
  NativeScrollEvent,
  NativeSyntheticEvent,
  StyleSheet,
  View,
} from "react-native";

import { useMobileImageSource } from "../../hooks/use-mobile-image-source";
import { colors, spacing } from "../../theme";

type MobilePostCarouselProps = {
  aspectRatio?: number;
  imageAccessibilityLabel?: string;
  imageUrls: string[];
  insetHorizontal?: number;
};

type PostImageProps = {
  accessibilityLabel: string;
  aspectRatio: number;
  uri: string;
  width: number;
};

function PostImage({
  accessibilityLabel,
  aspectRatio,
  uri,
  width,
}: PostImageProps) {
  const { failed, markFailed, source } = useMobileImageSource(uri);

  const imageStyle: ImageStyle = {
    aspectRatio,
    width,
  };

  if (failed) {
    return (
      <View
        accessibilityLabel={`${accessibilityLabel} unavailable`}
        accessibilityRole="image"
        style={[styles.image, styles.imageFallback, imageStyle]}
      >
        <ImageOff color={colors.muted} size={34} strokeWidth={1.8} />
      </View>
    );
  }

  if (!source) {
    return <View style={[styles.image, imageStyle]} />;
  }

  return (
    <Image
      accessibilityLabel={accessibilityLabel}
      onError={markFailed}
      resizeMethod="resize"
      resizeMode="cover"
      source={source}
      style={[styles.image, imageStyle]}
    />
  );
}

export function MobilePostCarousel({
  aspectRatio = 1,
  imageAccessibilityLabel = "Post image",
  imageUrls,
  insetHorizontal = spacing.lg,
}: MobilePostCarouselProps) {
  const [activeIndex, setActiveIndex] = useState(0);
  const [containerWidth, setContainerWidth] = useState(0);

  const itemWidth = Math.max(0, containerWidth);
  const imageWidth = Math.max(0, containerWidth - insetHorizontal * 2);
  const safeImages = useMemo(
    () => imageUrls.filter((imageUrl) => Boolean(imageUrl)),
    [imageUrls],
  );

  function handleMomentumEnd(event: NativeSyntheticEvent<NativeScrollEvent>) {
    if (!itemWidth) {
      return;
    }

    const nextIndex = Math.round(event.nativeEvent.contentOffset.x / itemWidth);
    setActiveIndex(Math.min(Math.max(nextIndex, 0), safeImages.length - 1));
  }

  if (!safeImages.length) {
    return null;
  }

  return (
    <View
      onLayout={(event) => setContainerWidth(event.nativeEvent.layout.width)}
      style={styles.wrapper}
    >
      {itemWidth ? (
        <FlatList
          bounces={false}
          data={safeImages}
          decelerationRate="fast"
          horizontal
          keyExtractor={(item, index) => `${item}-${index}`}
          onMomentumScrollEnd={handleMomentumEnd}
          pagingEnabled
          renderItem={({ index, item }) => (
            <View style={[styles.slide, { paddingHorizontal: insetHorizontal, width: itemWidth }]}>
              <PostImage
                accessibilityLabel={`${imageAccessibilityLabel} ${index + 1}`}
                aspectRatio={aspectRatio}
                uri={item}
                width={imageWidth}
              />
            </View>
          )}
          showsHorizontalScrollIndicator={false}
          snapToAlignment="start"
          snapToInterval={itemWidth}
          style={styles.list}
        />
      ) : null}

      {safeImages.length > 1 ? (
        <View style={styles.dots} pointerEvents="none">
          {safeImages.map((imageUrl, index) => (
            <View
              key={`${imageUrl}-dot-${index}`}
              style={[styles.dot, index === activeIndex && styles.dotActive]}
            />
          ))}
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  dot: {
    backgroundColor: colors.border,
    borderRadius: 3,
    height: 6,
    width: 6,
  },
  dotActive: {
    backgroundColor: colors.primary,
    width: 16,
  },
  dots: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xs,
    justifyContent: "center",
    paddingTop: 10,
  },
  image: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 0,
  },
  imageFallback: {
    alignItems: "center",
    justifyContent: "center",
  },
  list: {
    width: "100%",
  },
  slide: {
    alignItems: "center",
  },
  wrapper: {
    width: "100%",
  },
});
