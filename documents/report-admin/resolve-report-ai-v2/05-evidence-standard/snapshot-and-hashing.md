# Snapshot and Hashing

- Backend constructs canonical JSON from the exact fields sent for evaluation.
- SHA-256 hash is stored with snapshot version and capture/update times.
- Stable field ordering uses one canonicalization implementation tested against n8n fixtures.
- Hash identifies content/version; it is not authorization.
- Changed target after snapshot invalidates reuse.
- Missing field and explicit null remain distinguishable.
- Raw media binary is not embedded; verified media observations have their own digest/reference.

The same canonical form supports idempotency and HMAC body digest.
