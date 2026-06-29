-- Drop badge column from reviewer_ranking_snapshot.
-- Badge is now sourced exclusively from reviewer_badges (auto-upserted by the MONTHLY snapshot job).
ALTER TABLE reviewer_ranking_snapshot DROP COLUMN IF EXISTS badge;
