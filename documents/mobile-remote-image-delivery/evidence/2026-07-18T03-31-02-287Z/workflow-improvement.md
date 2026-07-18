# Workflow improvement

- Check Cloudinary Fetch entitlement before designing client delivery URLs.
- Check Wikimedia's current standard thumbnail sizes; arbitrary 1080px direct thumbnails are rejected, while 1280px is supported.
- Run the baseline mobile typecheck before edits because this project exceeds Node's default stack.
- Keep a device or `adb` available when image rendering needs runtime proof.
- Test the actual native image transport before treating React Native `source.headers` as equivalent to a direct OkHttp/file download.
- Prefer a temporary-file-to-cache promotion so interrupted Android downloads are never reused as valid images.
- Add focused unit tests for pure mobile URL adapters in a separate tooling task.
