# Sprint 1 Incident Runbook

## Trigger

- unexpected target/report mutation;
- auto-apply job created under A0;
- secret/PII/raw provider leak;
- forged/replayed webhook accepted;
- widespread wrong rule/action or stale-result reuse.

## Response

1. Enable/confirm A0 kill switch and disable Ask AI if necessary.
2. Stop worker/provider traffic without deleting evidence.
3. Record incident time, correlation IDs, versions and affected IDs.
4. Inventory jobs, recommendations and target/report before-after state.
5. Contain credential leak and rotate affected secret.
6. Restore affected target only through approved rollback path.
7. Preserve audit; do not rewrite/delete failure.
8. Communicate scope and current safety state.
9. Add regression test before re-enable.

## Severity

- `P0`: mutation/credential exposure/forged accepted request.
- `P1`: broad semantic corruption or inability to guarantee A0.
- `P2`: isolated provider/UI degradation without mutation.

P0 requires named Operations and Security/Privacy owners.
