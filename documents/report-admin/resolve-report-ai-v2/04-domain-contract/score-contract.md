# Risk and Assessment Contract 2.0

Numeric `confidenceScore/riskScore` are deprecated legacy fields.

| Dimension | Values | Authority |
|---|---|---|
| Evidence quality | HIGH/MEDIUM/LOW/UNUSABLE | Derived from provenance/availability |
| Evidence sufficiency | SUFFICIENT/INSUFFICIENT/CONFLICTED/UNASSESSABLE | Backend semantic gate |
| Violation likelihood | HIGH/MEDIUM/LOW/UNKNOWN | Model proposal validated against evidence |
| Harm severity | CRITICAL/HIGH/MEDIUM/LOW/UNKNOWN | Finding context |
| Action risk | CRITICAL/HIGH/MEDIUM/LOW | Backend deterministic resolver |

No dimension:

- is a calibrated probability;
- can replace Evidence ID;
- can enable automation;
- can turn missing evidence into `REJECT`;
- may be merged into one generic `riskScore`.
