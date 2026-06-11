import type { ImageSourcePropType } from "react-native";

export type UserPostPreview = {
  id: string;
  image: ImageSourcePropType;
  caption: string;
  likeCount: number;
  commentCount: number;
};
