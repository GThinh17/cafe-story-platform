# Evidence Retention and Privacy

L1 engineering baseline:

| Data | Default |
|---|---:|
| Secret/credential | Never persist |
| Raw provider payload | Disabled; incident max 7 days |
| Sanitized evidence snapshot | 90 days after closure |
| Structured recommendation/decision | 365 days |
| Action/rollback/security audit | 730 days |

Principles:

- minimum necessary fields;
- role-based access;
- encryption in transit/at rest;
- deletion job with observable result;
- legal/privacy hold can override;
- no raw PII in explanation/log/evidence bundle;
- retention is reviewed before activation and is not AI-controlled.
