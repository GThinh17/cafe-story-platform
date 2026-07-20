ALTER TABLE users
ADD COLUMN IF NOT EXISTS hide_cafe_page_on_profile boolean NOT NULL DEFAULT false;
