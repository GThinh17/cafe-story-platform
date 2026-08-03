# Definition of Done — Resolve Report with AI V2

## Gate 0 dossier

- policy/rule/evidence semantics approved;
- 12/12 business decisions recorded;
- current gaps traceable;
- Sprint 1 Detailed Design review-ready;
- no source/runtime mutation before G0-12.

## Sprint 1 implementation

- A0 enforced at FE/BE/worker;
- Contract V2, evidence refs, semantic validation and versioning implemented;
- BLOG/COMMENT minimum evidence implemented;
- USER/PAGE manual-only and no provider call;
- signed n8n boundary;
- evidence-first UI, raw hidden, bulk semantics separated;
- migration additive and legacy readable;
- focused/broader/E2E/security/side-effect checks pass;
- changed-file coverage `100%` line and `>=85%` branch;
- runtime evidence complete or status honestly `PARTIAL/BLOCKED`;
- rollout/rollback/owners/monitoring ready.

## Cannot be DONE when

- mandatory tests did not run;
- safe fixture/runtime unavailable;
- any AI-triggered mutation occurs under A0;
- secrets/raw payload leak;
- only schema shape—not semantics—was tested;
- published n8n readiness was inferred from liveness;
- unresolved P0/P1 safety defect remains.
