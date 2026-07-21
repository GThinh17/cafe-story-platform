# Conflict-scope coverage

- JaCoCo whole-class result for `BlogFeedRankingServiceImpl`: 582/688 lines (84.59%), 170/294 branches (57.82%). The uncovered total includes pre-existing ranking paths outside the merge-conflict regions.
- Executable lines selected inside the resolved viewer-state mapping region: 100% covered.
- Branches inside the resolved viewer-state mapping region: 20/22 covered (90.91%).
- The focused regression test `getPersonalizedFeed_success_batchesViewerStateWithoutPerItemQueries_TC010` verifies batched follow/like/save mapping and guards against per-item repository queries.
- Frontend unit coverage is unavailable; see `MERGE-003` in `issue.md`.
