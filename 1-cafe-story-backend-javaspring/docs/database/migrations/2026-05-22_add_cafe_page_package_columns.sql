ALTER TABLE extra_fees
ADD COLUMN IF NOT EXISTS max_members integer;

ALTER TABLE cafe_pages
ADD COLUMN IF NOT EXISTS max_members integer NOT NULL DEFAULT 2;

ALTER TABLE cafe_pages
ADD COLUMN IF NOT EXISTS page_active boolean NOT NULL DEFAULT false;

ALTER TABLE cafe_pages
ADD COLUMN IF NOT EXISTS page_expires_at timestamp;
