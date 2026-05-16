# Validation Reference

Load this reference when choosing validation commands or writing final verification notes.

## Deterministic Validation Order

1. Static project structure:
   - `python .agents/skills/cafestory-engineering-workflows/scripts/check_project.py .`
2. Backend:
   - Inspect `1-cafe-story-backend-javaspring/pom.xml`.
   - Run the narrowest relevant Maven test/build command.
3. Web:
   - Inspect `2-cafe-story-nextjs-web/package.json`.
   - Run available lint/typecheck/test/build scripts.
4. Mobile:
   - Inspect `3-cafe-story-reactnative-mobile/package.json`.
   - Run available lint/typecheck/test scripts.

## Final Report Format

Include:
- What changed.
- Validation commands run.
- Commands that failed and why.
- Known unvalidated areas.
- Any assumptions made because the codebase did not define behavior.
