ALTER TABLE blog_recommendation_scores
    ADD COLUMN IF NOT EXISTS formula_version VARCHAR(32) NOT NULL DEFAULT 'LEGACY_V1',
    ADD COLUMN IF NOT EXISTS relationship_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS interest_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS engagement_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS quality_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS location_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS diversity_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS unseen_score DOUBLE PRECISION NOT NULL DEFAULT 0;

UPDATE blog_recommendation_scores
SET formula_version = 'LEGACY_V1'
WHERE formula_version IS NULL OR formula_version = '';

CREATE INDEX IF NOT EXISTS idx_blog_recommendation_scores_formula
    ON blog_recommendation_scores (user_id, window_type, context_region_id, formula_version, computed_at);
