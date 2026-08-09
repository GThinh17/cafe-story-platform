# Workflow Improvement

- Check Flyway validation with the real configured database before declaring backend runtime success.
- Treat applied migration files as immutable once they have reached Supabase.
- For future schema changes, add a new versioned migration instead of editing a previously applied migration.
- If a repair is necessary, first query the existing schema read-only and record exactly which `flyway_schema_history` row will change.
