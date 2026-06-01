-- PostgreSQL migration proposal for CafeStory blog save and rating interactions.
-- Apply manually because this project does not currently use Flyway or Liquibase.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS blog_saves (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    blog_id uuid NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    created_at timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_blog_saves_user_blog UNIQUE (user_id, blog_id)
);

CREATE INDEX IF NOT EXISTS idx_blog_saves_user_id ON blog_saves(user_id);
CREATE INDEX IF NOT EXISTS idx_blog_saves_blog_id ON blog_saves(blog_id);
CREATE INDEX IF NOT EXISTS idx_blog_saves_created_at ON blog_saves(created_at);

CREATE TABLE IF NOT EXISTS blog_ratings (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    blog_id uuid NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    rating integer NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    CONSTRAINT uk_blog_ratings_user_blog UNIQUE (user_id, blog_id),
    CONSTRAINT ck_blog_ratings_rating_range CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX IF NOT EXISTS idx_blog_ratings_user_id ON blog_ratings(user_id);
CREATE INDEX IF NOT EXISTS idx_blog_ratings_blog_id ON blog_ratings(blog_id);
CREATE INDEX IF NOT EXISTS idx_blog_ratings_rating ON blog_ratings(rating);
