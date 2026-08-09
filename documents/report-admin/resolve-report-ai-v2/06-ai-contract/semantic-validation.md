# AI Semantic Validation

Backend validates:

1. contract/correlation/version equality;
2. allowed target and Rule IDs;
3. unique findings;
4. Evidence ID referential integrity;
5. rule/target applicability;
6. evidence sufficiency and critical missing/conflict;
7. decision/action matrix;
8. USER/PAGE hard boundary;
9. Backend-derived action risk;
10. signature/freshness and snapshot freshness.

Outcome:

- valid → persist recommendation;
- trusted but semantically invalid → sanitized manual fallback + blocked reasons;
- untrusted/auth/schema boundary failure → operational error, no content decision;
- stale → do not reuse result/action.
