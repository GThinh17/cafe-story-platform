-- PostgreSQL migration proposal for CafeStory blog tagged users.
-- Apply manually because this project does not currently use Flyway or Liquibase.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

create table if not exists blog_tagged_users (
    id uuid primary key default gen_random_uuid(),
    blog_id uuid not null references blogs(id) on delete cascade,
    tagged_user_id uuid not null references users(user_id) on delete cascade,
    tagged_by_user_id uuid not null references users(user_id) on delete cascade,
    created_at timestamp not null default now(),
    constraint uk_blog_tagged_users_blog_user unique (blog_id, tagged_user_id)
);

create index if not exists idx_blog_tagged_users_blog_id
    on blog_tagged_users(blog_id);

create index if not exists idx_blog_tagged_users_tagged_user_id
    on blog_tagged_users(tagged_user_id);

create index if not exists idx_blog_tagged_users_tagged_by_user_id
    on blog_tagged_users(tagged_by_user_id);
