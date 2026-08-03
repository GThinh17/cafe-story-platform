# Evidence Source Types

## Allowed trong Common Evidence Envelope

| Type | May substantiate? | Example |
|---|---|---|
| `PLATFORM_RECORD` | Yes, within owned field | status/identity/ownership relation |
| `TARGET_SNAPSHOT` | Yes | exact reported text/state |
| `VERIFIED_MEDIA_OBSERVATION` | Yes, khi collector được approve | trusted vision/tool observation |
| `AUTHORITATIVE_EXTERNAL_REFERENCE` | Yes, within authority/freshness | official source |

Source type does not guarantee relevance, freshness or sufficiency.

## Không được nằm trong `evidence[]`

| Type | Object đúng |
|---|---|
| `REPORTER_CLAIM` | `reportClaim` |
| `DERIVED_SIGNAL` | `investigationSignals` hoặc audit context |
| `MODEL_MEMORY` | Forbidden |
| `AI_RATIONALE` | Finding/recommendation rationale, không phải evidence |

Evidence Kind tương thích với từng source type được khóa tại `evidence-kind-catalog.md`.
