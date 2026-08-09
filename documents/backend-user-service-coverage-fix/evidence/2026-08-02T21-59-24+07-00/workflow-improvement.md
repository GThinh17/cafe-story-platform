# Workflow Improvement

- Check `mvn verify` earlier when the backend has a hard JaCoCo rule, because `mvn test` can pass while `verify` still fails.
- For migration deletion questions, query `flyway_schema_history` and physical column metadata read-only before touching SQL files.
- Capture Maven output to a raw log file in the next run if the failure is expected to be noisy.
