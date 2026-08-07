-- flyway:executeInTransaction=false

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'ad_fee_type') THEN
        CREATE TYPE ad_fee_type AS ENUM (
            'FEED_10000_IMPRESSIONS_OR_30_DAYS',
            'CAFE_AD_STARTER',
            'CAFE_AD_GROWTH',
            'CAFE_AD_PREMIUM'
        );
    ELSE
        ALTER TYPE ad_fee_type ADD VALUE IF NOT EXISTS 'CAFE_AD_STARTER';
        ALTER TYPE ad_fee_type ADD VALUE IF NOT EXISTS 'CAFE_AD_GROWTH';
        ALTER TYPE ad_fee_type ADD VALUE IF NOT EXISTS 'CAFE_AD_PREMIUM';
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

INSERT INTO ad_fees (ad_fee_id, fee_type, price, currency, status, created_at, updated_at)
VALUES
    ('49000000-0000-4000-8000-000000000001', 'CAFE_AD_STARTER', 49000.00, 'VND', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('99000000-0000-4000-8000-000000000001', 'CAFE_AD_GROWTH', 99000.00, 'VND', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('14900000-0000-4000-8000-000000000001', 'CAFE_AD_PREMIUM', 149000.00, 'VND', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (fee_type) DO UPDATE
SET price = EXCLUDED.price,
    currency = EXCLUDED.currency,
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP;
