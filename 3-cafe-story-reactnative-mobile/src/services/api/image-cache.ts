import { Directory, File, Paths } from "expo-file-system";
import { Platform, type ImageURISource } from "react-native";

import {
  getMobileImageSource,
  getMobileImageSources,
  isWikimediaDeliveryUri,
} from "./image-delivery";

const IMAGE_CACHE_DIRECTORY = "cafestory-remote-images";
const pendingDownloads = new Map<string, Promise<ImageURISource>>();

function hashUri(uri: string) {
  let hash = 2166136261;

  for (let index = 0; index < uri.length; index += 1) {
    hash ^= uri.charCodeAt(index);
    hash = Math.imul(hash, 16777619);
  }

  return (hash >>> 0).toString(16);
}

function getImageExtension(uri: string) {
  return uri.match(/\.(gif|jpe?g|png|webp)(?:[/?#]|$)/i)?.[1]?.toLowerCase() ?? "img";
}

function getCacheDirectory() {
  const directory = new Directory(Paths.cache, IMAGE_CACHE_DIRECTORY);
  directory.create({ idempotent: true, intermediates: true });
  return directory;
}

function removeFileIfPresent(file: File) {
  if (file.exists) {
    file.delete();
  }
}

async function downloadSourceToCache(source: ImageURISource) {
  const sourceUri = source.uri;

  if (!sourceUri) {
    throw new Error("Remote image source is missing its URI");
  }

  const existingDownload = pendingDownloads.get(sourceUri);

  if (existingDownload) {
    return existingDownload;
  }

  const download = (async () => {
    const directory = getCacheDirectory();
    const fileName = `${hashUri(sourceUri)}.${getImageExtension(sourceUri)}`;
    const cachedFile = new File(directory, fileName);

    if (cachedFile.exists && cachedFile.size > 0) {
      return { uri: cachedFile.uri };
    }

    removeFileIfPresent(cachedFile);

    const temporaryFile = new File(directory, `${fileName}.download`);
    removeFileIfPresent(temporaryFile);

    try {
      const downloadedFile = await File.downloadFileAsync(sourceUri, temporaryFile, {
        headers: source.headers,
        idempotent: true,
      });

      if (!downloadedFile.exists || downloadedFile.size <= 0) {
        throw new Error("Downloaded image file is empty");
      }

      downloadedFile.move(cachedFile);
      return { uri: cachedFile.uri };
    } catch (error) {
      removeFileIfPresent(temporaryFile);
      throw error;
    }
  })();

  pendingDownloads.set(sourceUri, download);

  try {
    return await download;
  } finally {
    pendingDownloads.delete(sourceUri);
  }
}

export function needsMobileImageCache(uri: string) {
  return Platform.OS !== "web" && isWikimediaDeliveryUri(uri);
}

export async function resolveMobileImageSource(uri: string): Promise<ImageURISource> {
  if (!needsMobileImageCache(uri)) {
    return getMobileImageSource(uri);
  }

  let latestError: unknown;

  for (const source of getMobileImageSources(uri)) {
    try {
      return await downloadSourceToCache(source);
    } catch (error) {
      latestError = error;
    }
  }

  throw latestError ?? new Error("Unable to cache the remote image");
}
