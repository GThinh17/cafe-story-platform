-- PostgreSQL migration proposal for CafeStory cafe page ratings.
-- Apply manually because this project does not currently use Flyway or Liquibase.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS cafe_page_ratings (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    cafe_page_id uuid NOT NULL REFERENCES cafe_pages(id) ON DELETE CASCADE,
    rating integer NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    CONSTRAINT uk_cafe_page_ratings_user_page UNIQUE (user_id, cafe_page_id),
    CONSTRAINT ck_cafe_page_ratings_rating_range CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX IF NOT EXISTS idx_cafe_page_ratings_user_id ON cafe_page_ratings(user_id);
CREATE INDEX IF NOT EXISTS idx_cafe_page_ratings_cafe_page_id ON cafe_page_ratings(cafe_page_id);
CREATE INDEX IF NOT EXISTS idx_cafe_page_ratings_rating ON cafe_page_ratings(rating);
