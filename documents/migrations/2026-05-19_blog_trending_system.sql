-- PostgreSQL migration proposal for CafeStory blog trending MVP.
-- Apply manually because this project does not currently use Flyway or Liquibase.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'blog_event_type') THEN
        CREATE TYPE blog_event_type AS ENUM ('VIEW', 'LIKE', 'COMMENT', 'SHARE', 'SAVE', 'REPORT');
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'trend_window_type') THEN
        CREATE TYPE trend_window_type AS ENUM ('HOUR_24', 'DAY_7', 'MONTH_1');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS blog_events (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    blog_id uuid NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    user_id uuid REFERENCES users(user_id) ON DELETE SET NULL,
    event_type blog_event_type NOT NULL,
    weight double precision NOT NULL DEFAULT 1.0,
    created_at timestamp NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_blog_events_blog_id ON blog_events(blog_id);
CREATE INDEX IF NOT EXISTS idx_blog_events_user_id ON blog_events(user_id);
CREATE INDEX IF NOT EXISTS idx_blog_events_event_type ON blog_events(event_type);
CREATE INDEX IF NOT EXISTS idx_blog_events_created_at ON blog_events(created_at);

CREATE TABLE IF NOT EXISTS blog_daily_metrics (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    blog_id uuid NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    metric_date date NOT NULL,
    views bigint NOT NULL DEFAULT 0,
    likes bigint NOT NULL DEFAULT 0,
    comments bigint NOT NULL DEFAULT 0,
    shares bigint NOT NULL DEFAULT 0,
    saves bigint NOT NULL DEFAULT 0,
    reports bigint NOT NULL DEFAULT 0,
    avg_read_seconds double precision NOT NULL DEFAULT 0,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    CONSTRAINT uk_blog_daily_metrics_blog_date UNIQUE (blog_id, metric_date)
);

CREATE TABLE IF NOT EXISTS blog_trending_scores (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    blog_id uuid NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    window_type trend_window_type NOT NULL,
    trend_score double precision NOT NULL,
    rank_position integer NOT NULL,
    reason text,
    computed_at timestamp NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_blog_trending_scores_window_computed
    ON blog_trending_scores(window_type, computed_at);
CREATE INDEX IF NOT EXISTS idx_blog_trending_scores_rank
    ON blog_trending_scores(rank_position);

CREATE TABLE IF NOT EXISTS blog_ranking_overrides (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    blog_id uuid NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    boost_score double precision NOT NULL DEFAULT 0,
    is_pinned boolean NOT NULL DEFAULT false,
    reason text,
    start_at timestamp NOT NULL,
    end_at timestamp,
    created_by uuid REFERENCES users(user_id) ON DELETE SET NULL,
    created_at timestamp NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_blog_ranking_overrides_blog_id
    ON blog_ranking_overrides(blog_id);
CREATE INDEX IF NOT EXISTS idx_blog_ranking_overrides_active_window
    ON blog_ranking_overrides(start_at, end_at);

