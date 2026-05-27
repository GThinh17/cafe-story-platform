-- Apply manually because this project does not currently use Flyway or Liquibase.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'ad_status') THEN
        CREATE TYPE ad_status AS ENUM ('DRAFT', 'ACTIVE', 'PAUSED', 'EXPIRED', 'REJECTED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'ad_fee_type') THEN
        CREATE TYPE ad_fee_type AS ENUM ('FEED_10000_IMPRESSIONS_OR_30_DAYS');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS ad_fees (
    ad_fee_id uuid PRIMARY KEY,
    fee_type ad_fee_type NOT NULL UNIQUE,
    price decimal(12,2) NOT NULL,
    currency varchar(10) NOT NULL DEFAULT 'VND',
    status boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL,
    updated_at timestamp,
    CONSTRAINT chk_ad_fees_price_positive CHECK (price > 0)
);

CREATE TABLE IF NOT EXISTS ad_campaigns (
    ad_campaign_id uuid PRIMARY KEY,
    cafe_page_id uuid NOT NULL REFERENCES cafe_pages(id),
    payment_id uuid UNIQUE REFERENCES payments(payment_id),
    title varchar(160) NOT NULL,
    description text,
    image_url text,
    target_url text,
    status ad_status NOT NULL DEFAULT 'DRAFT',
    start_at timestamp,
    end_at timestamp,
    priority int NOT NULL DEFAULT 1,
    max_impressions int NOT NULL DEFAULT 10000,
    served_impressions int NOT NULL DEFAULT 0,
    max_duration_days int NOT NULL DEFAULT 30,
    created_at timestamp NOT NULL,
    updated_at timestamp,
    CONSTRAINT chk_ad_campaigns_max_impressions CHECK (max_impressions = 10000),
    CONSTRAINT chk_ad_campaigns_max_duration_days CHECK (max_duration_days = 30),
    CONSTRAINT chk_ad_campaigns_served_impressions_non_negative CHECK (served_impressions >= 0),
    CONSTRAINT chk_ad_campaigns_served_impressions_max CHECK (served_impressions <= max_impressions)
);

CREATE TABLE IF NOT EXISTS ad_target_regions (
    ad_target_region_id uuid PRIMARY KEY,
    ad_campaign_id uuid NOT NULL REFERENCES ad_campaigns(ad_campaign_id) ON DELETE CASCADE,
    province varchar(120),
    city varchar(120),
    area varchar(120),
    ward varchar(120),
    created_at timestamp NOT NULL,
    updated_at timestamp
);

CREATE TABLE IF NOT EXISTS ad_impressions (
    ad_impression_id uuid PRIMARY KEY,
    ad_campaign_id uuid NOT NULL REFERENCES ad_campaigns(ad_campaign_id) ON DELETE CASCADE,
    user_id uuid REFERENCES users(user_id),
    shown_at timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS ad_clicks (
    ad_click_id uuid PRIMARY KEY,
    ad_campaign_id uuid NOT NULL REFERENCES ad_campaigns(ad_campaign_id) ON DELETE CASCADE,
    user_id uuid REFERENCES users(user_id),
    clicked_at timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS ad_daily_stats (
    ad_daily_stat_id uuid PRIMARY KEY,
    ad_campaign_id uuid NOT NULL REFERENCES ad_campaigns(ad_campaign_id) ON DELETE CASCADE,
    stat_date date NOT NULL,
    impressions int NOT NULL DEFAULT 0,
    clicks int NOT NULL DEFAULT 0,
    created_at timestamp NOT NULL,
    updated_at timestamp,
    CONSTRAINT uk_ad_daily_stats_campaign_date UNIQUE (ad_campaign_id, stat_date)
);

ALTER TABLE payments
    ALTER COLUMN extra_fee_id DROP NOT NULL;

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS ad_fee_id uuid REFERENCES ad_fees(ad_fee_id);

ALTER TABLE payments
    DROP CONSTRAINT IF EXISTS chk_payments_exactly_one_fee;

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_exactly_one_fee
    CHECK (
        (extra_fee_id IS NOT NULL AND ad_fee_id IS NULL)
        OR (extra_fee_id IS NULL AND ad_fee_id IS NOT NULL)
    );

CREATE INDEX IF NOT EXISTS idx_ad_campaigns_status_start_end ON ad_campaigns(status, start_at, end_at);
CREATE INDEX IF NOT EXISTS idx_ad_campaigns_cafe_page_id ON ad_campaigns(cafe_page_id);
CREATE INDEX IF NOT EXISTS idx_ad_campaigns_payment_id ON ad_campaigns(payment_id);
CREATE INDEX IF NOT EXISTS idx_ad_campaigns_status_served_impressions ON ad_campaigns(status, served_impressions);
CREATE INDEX IF NOT EXISTS idx_ad_target_regions_ad_campaign_id ON ad_target_regions(ad_campaign_id);
CREATE INDEX IF NOT EXISTS idx_ad_target_regions_province ON ad_target_regions(province);
CREATE INDEX IF NOT EXISTS idx_ad_target_regions_city ON ad_target_regions(city);
CREATE INDEX IF NOT EXISTS idx_ad_target_regions_area ON ad_target_regions(area);
CREATE INDEX IF NOT EXISTS idx_ad_target_regions_ward ON ad_target_regions(ward);
CREATE INDEX IF NOT EXISTS idx_ad_impressions_campaign_shown ON ad_impressions(ad_campaign_id, shown_at);
CREATE INDEX IF NOT EXISTS idx_ad_impressions_user_campaign_shown ON ad_impressions(user_id, ad_campaign_id, shown_at);
CREATE INDEX IF NOT EXISTS idx_ad_clicks_campaign_clicked ON ad_clicks(ad_campaign_id, clicked_at);
CREATE INDEX IF NOT EXISTS idx_ad_clicks_user_campaign_clicked ON ad_clicks(user_id, ad_campaign_id, clicked_at);
CREATE INDEX IF NOT EXISTS idx_ad_daily_stats_campaign_date ON ad_daily_stats(ad_campaign_id, stat_date);
CREATE INDEX IF NOT EXISTS idx_payments_buyer_id ON payments(buyer_id);
CREATE INDEX IF NOT EXISTS idx_payments_extra_fee_id ON payments(extra_fee_id);
CREATE INDEX IF NOT EXISTS idx_payments_ad_fee_id ON payments(ad_fee_id);
CREATE INDEX IF NOT EXISTS idx_payments_payment_status ON payments(payment_status);
