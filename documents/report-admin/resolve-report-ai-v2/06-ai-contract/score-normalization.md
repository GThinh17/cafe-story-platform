# Legacy Score Normalization

Sprint 1 does not convert numeric V1 values into V2 categorical truth.

- V1 `confidenceScore/riskScore` remain nullable legacy display fields.
- No `0..1` to `0..100` normalization for V2.
- No threshold scheduling.
- FE labels V1 scores uncalibrated/non-authoritative.
- V2 uses categorical likelihood/harm/action risk and evidence sufficiency.
- Analytics must segment by contract version.

Historical numeric values are not backfilled into findings or evidence.
