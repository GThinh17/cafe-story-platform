-- PostgreSQL migration proposal for cached personalized feed recommendations.
-- Apply manually because this project does not currently use Flyway or Liquibase.

CREATE TABLE IF NOT EXISTS blog_recommendation_scores (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    blog_id uuid NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    window_type trend_window_type NOT NULL,
    context_region_id uuid REFERENCES regions(region_id) ON DELETE SET NULL,
    feed_score double precision NOT NULL,
    trending_score double precision NOT NULL DEFAULT 0,
    followed_page_score double precision NOT NULL DEFAULT 0,
    followed_user_score double precision NOT NULL DEFAULT 0,
    same_region_score double precision NOT NULL DEFAULT 0,
    freshness_score double precision NOT NULL DEFAULT 0,
    report_penalty double precision NOT NULL DEFAULT 0,
    rank_position integer NOT NULL,
    reason text,
    computed_at timestamp NOT NULL,
    created_at timestamp NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_blog_recommendation_scores_user_blog_window_region
    ON blog_recommendation_scores(
        user_id,
        blog_id,
        window_type,
        COALESCE(context_region_id, '00000000-0000-0000-0000-000000000000'::uuid)
    );

CREATE INDEX IF NOT EXISTS idx_blog_recommendation_scores_user_window_computed
    ON blog_recommendation_scores(user_id, window_type, context_region_id, computed_at);

CREATE INDEX IF NOT EXISTS idx_blog_recommendation_scores_rank
    ON blog_recommendation_scores(user_id, window_type, context_region_id, rank_position);

CREATE INDEX IF NOT EXISTS idx_blog_recommendation_scores_blog_id
    ON blog_recommendation_scores(blog_id);
