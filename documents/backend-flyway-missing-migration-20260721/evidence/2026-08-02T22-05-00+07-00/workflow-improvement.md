# Workflow Improvement

- For backend startup complaints, run or inspect a real runtime smoke before relying on `mvn test` or `mvn verify`.
- When Flyway reports a missing applied migration, search git history for the exact SQL content before considering metadata repair.
- Quote PowerShell Maven `-D...` properties consistently; unquoted `-Dspring-boot.run.arguments=...` is parsed incorrectly.
