# Semantic Invariants — Contract 2.0

1. Every finding uses an allowed Rule ID/version.
2. Every referenced Evidence ID exists in the request snapshot.
3. AI rationale never appears in evidence references.
4. Critical missing/conflict forces manual/no action.
5. Sufficient evidence is evaluated per finding and candidate action.
6. Reporter claim/count/reason severity are not violation evidence.
7. Model memory is not authoritative evidence.
8. Action risk is Backend-derived.
9. USER/PAGE are manual-only in Sprint 1.
10. A0 blocks schedule, execution and auto report closure.
11. Operational failure is not content decision.
12. Snapshot/version/correlation mismatch fails closed.
13. Legacy record remains legacy; no evidence backfill.
14. n8n cannot create policy/rule authority.
15. Backend validation runs even when n8n strict schema passes.
