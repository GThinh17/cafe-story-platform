const DICEBEAR_API_HOST = "api.dicebear.com";
const DICEBEAR_PNG_MAX_SIZE = 256;
const DICEBEAR_PNG_MIN_SIZE = 64;

function isDiceBearHost(hostname: string) {
  return hostname.toLowerCase() === DICEBEAR_API_HOST;
}

function resolveDiceBearRasterSize(displaySize: number) {
  const requestedSize = Math.ceil(displaySize * 2);
  return Math.min(DICEBEAR_PNG_MAX_SIZE, Math.max(DICEBEAR_PNG_MIN_SIZE, requestedSize));
}

export function resolveAvatarImageUri(uri: string | null | undefined, displaySize: number) {
  const trimmedUri = uri?.trim();

  if (!trimmedUri) {
    return null;
  }

  try {
    const parsedUrl = new URL(trimmedUri);

    if (!isDiceBearHost(parsedUrl.hostname)) {
      return trimmedUri;
    }

    const pathParts = parsedUrl.pathname.split("/");
    const formatIndex = pathParts.findIndex((part) => part.toLowerCase() === "svg");

    if (formatIndex === -1) {
      return trimmedUri;
    }

    pathParts[formatIndex] = "png";
    parsedUrl.pathname = pathParts.join("/");

    if (!parsedUrl.searchParams.has("size")) {
      parsedUrl.searchParams.set("size", String(resolveDiceBearRasterSize(displaySize)));
    }

    return parsedUrl.toString();
  } catch {
    return trimmedUri;
  }
}
