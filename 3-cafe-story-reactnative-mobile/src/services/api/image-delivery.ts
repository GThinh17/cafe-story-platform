import type { ImageURISource } from "react-native";

const DICEBEAR_SVG_URL = /^(https:\/\/api\.dicebear\.com\/.+\/)svg(\?.*)?$/i;
const WIKIMEDIA_DELIVERY_URL = /^https:\/\/upload\.wikimedia\.org\//i;
const WIKIMEDIA_ORIGINAL_BITMAP_URL =
  /^(https:\/\/upload\.wikimedia\.org\/wikipedia\/commons\/)([0-9a-f]\/[^/]+\/)([^/?#]+\.(?:gif|jpe?g|png|webp))(?:[?#].*)?$/i;
const WIKIMEDIA_THUMBNAIL_WIDTH = 1280;
export const WIKIMEDIA_USER_AGENT = "CafeStoryMobile/1.0 ReactNative/0.81";

export function normalizeMobileImageUri(uri: string) {
  const trimmedUri = uri.trim();

  return trimmedUri.replace(DICEBEAR_SVG_URL, "$1png$2");
}

export function getWikimediaThumbnailUri(uri: string) {
  const match = uri.match(WIKIMEDIA_ORIGINAL_BITMAP_URL);

  if (!match) {
    return uri;
  }

  const [, baseUrl, hashPath, fileName] = match;

  return `${baseUrl}thumb/${hashPath}${fileName}/${WIKIMEDIA_THUMBNAIL_WIDTH}px-${fileName}`;
}

export function isWikimediaDeliveryUri(uri: string) {
  return WIKIMEDIA_DELIVERY_URL.test(normalizeMobileImageUri(uri));
}

function withWikimediaUserAgent(uri: string): ImageURISource {
  return {
    headers: {
      "User-Agent": WIKIMEDIA_USER_AGENT,
    },
    uri,
  };
}

export function getMobileImageSources(uri: string): ImageURISource[] {
  const normalizedUri = normalizeMobileImageUri(uri);

  if (isWikimediaDeliveryUri(normalizedUri)) {
    const thumbnailUri = getWikimediaThumbnailUri(normalizedUri);
    const sources = [withWikimediaUserAgent(thumbnailUri)];

    if (thumbnailUri !== normalizedUri) {
      sources.push(withWikimediaUserAgent(normalizedUri));
    }

    return sources;
  }

  return [{ uri: normalizedUri }];
}

export function getMobileImageSource(uri: string): ImageURISource {
  return getMobileImageSources(uri)[0];
}
