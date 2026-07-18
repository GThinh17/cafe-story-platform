import { useCallback, useEffect, useMemo, useState } from "react";
import type { ImageURISource } from "react-native";

import {
  needsMobileImageCache,
  resolveMobileImageSource,
} from "../services/api/image-cache";
import { getMobileImageSource } from "../services/api/image-delivery";

type ResolvedImageState = {
  failed: boolean;
  requestUri: string | null;
  source: ImageURISource | null;
};

function getInitialSource(uri: string | null) {
  if (!uri) {
    return null;
  }

  return needsMobileImageCache(uri) ? null : getMobileImageSource(uri);
}

export function useMobileImageSource(uri: string | null) {
  const initialSource = useMemo(() => getInitialSource(uri), [uri]);
  const [resolvedState, setResolvedState] = useState<ResolvedImageState>({
    failed: false,
    requestUri: uri,
    source: initialSource,
  });

  const currentState =
    resolvedState.requestUri === uri
      ? resolvedState
      : { failed: false, requestUri: uri, source: initialSource };

  useEffect(() => {
    let active = true;

    setResolvedState({ failed: false, requestUri: uri, source: initialSource });

    if (!uri || initialSource) {
      return () => {
        active = false;
      };
    }

    resolveMobileImageSource(uri)
      .then((source) => {
        if (active) {
          setResolvedState({ failed: false, requestUri: uri, source });
        }
      })
      .catch(() => {
        if (active) {
          setResolvedState({ failed: true, requestUri: uri, source: null });
        }
      });

    return () => {
      active = false;
    };
  }, [initialSource, uri]);

  const markFailed = useCallback(() => {
    if (!uri) {
      return;
    }

    setResolvedState({ failed: true, requestUri: uri, source: null });
  }, [uri]);

  return {
    failed: currentState.failed,
    loading: !currentState.failed && !currentState.source,
    markFailed,
    source: currentState.source,
  };
}
