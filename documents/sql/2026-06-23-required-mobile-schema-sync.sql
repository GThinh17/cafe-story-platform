-- Required schema sync for current CafeStory backend/mobile API.
-- Run this once in the PostgreSQL/Supabase database used by the backend.
-- It is safe to rerun because columns/indexes/constraints use IF NOT EXISTS guards where PostgreSQL supports them.

-- 1) Actor context support for user/page actions.

alter table blog_likes
    add column if not exists actor_context_type varchar(32) not null default 'USER',
    add column if not exists actor_cafe_page_id uuid;

do $$
begin
    if not exists (select 1 from pg_constraint where conname = 'fk_blog_likes_actor_cafe_page') then
        alter table blog_likes
            add constraint fk_blog_likes_actor_cafe_page
                foreign key (actor_cafe_page_id) references cafe_pages(id);
    end if;
end $$;

alter table comments
    add column if not exists actor_context_type varchar(32) not null default 'USER',
    add column if not exists actor_cafe_page_id uuid;

do $$
begin
    if not exists (select 1 from pg_constraint where conname = 'fk_comments_actor_cafe_page') then
        alter table comments
            add constraint fk_comments_actor_cafe_page
                foreign key (actor_cafe_page_id) references cafe_pages(id);
    end if;
end $$;

alter table blog_shares
    add column if not exists actor_context_type varchar(32) not null default 'USER',
    add column if not exists actor_cafe_page_id uuid;

do $$
begin
    if not exists (select 1 from pg_constraint where conname = 'fk_blog_shares_actor_cafe_page') then
        alter table blog_shares
            add constraint fk_blog_shares_actor_cafe_page
                foreign key (actor_cafe_page_id) references cafe_pages(id);
    end if;
end $$;

alter table notifications
    add column if not exists actor_context_type varchar(32) not null default 'USER',
    add column if not exists actor_cafe_page_id uuid,
    add column if not exists target_type varchar(32),
    add column if not exists target_cafe_page_id uuid;

do $$
begin
    if not exists (select 1 from pg_constraint where conname = 'fk_notifications_actor_cafe_page') then
        alter table notifications
            add constraint fk_notifications_actor_cafe_page
                foreign key (actor_cafe_page_id) references cafe_pages(id);
    end if;
end $$;

alter table payments
    add column if not exists cafe_page_id uuid;

do $$
begin
    if not exists (select 1 from pg_constraint where conname = 'fk_payments_cafe_page') then
        alter table payments
            add constraint fk_payments_cafe_page
                foreign key (cafe_page_id) references cafe_pages(id);
    end if;
end $$;

do $$
declare
    constraint_name text;
begin
    select c.conname into constraint_name
    from pg_constraint c
    join pg_class t on t.oid = c.conrelid
    join pg_namespace n on n.oid = t.relnamespace
    where n.nspname = current_schema()
      and t.relname = 'blog_likes'
      and c.contype = 'u'
      and (
          select array_agg(a.attname::text order by a.attname::text)
          from unnest(c.conkey) k(attnum)
          join pg_attribute a on a.attrelid = t.oid and a.attnum = k.attnum
      ) = array['blog_id', 'user_id'];

    if constraint_name is not null then
        execute format('alter table blog_likes drop constraint %I', constraint_name);
    end if;
end $$;

create unique index if not exists uk_blog_likes_user_actor
    on blog_likes(blog_id, user_id)
    where actor_context_type = 'USER' and actor_cafe_page_id is null;

create unique index if not exists uk_blog_likes_page_actor
    on blog_likes(blog_id, actor_cafe_page_id)
    where actor_context_type = 'CAFE_PAGE' and actor_cafe_page_id is not null;

create index if not exists idx_blog_likes_actor_page
    on blog_likes(actor_cafe_page_id, created_at);

create index if not exists idx_comments_actor_page_created
    on comments(actor_cafe_page_id, created_at);

create index if not exists idx_blog_shares_actor_page_created
    on blog_shares(actor_cafe_page_id, created_at);

create index if not exists idx_notifications_actor_page
    on notifications(actor_cafe_page_id, created_at);

create index if not exists idx_notifications_target_page
    on notifications(target_cafe_page_id, created_at);

create index if not exists idx_payments_cafe_page
    on payments(cafe_page_id, created_at);

-- 2) Feed activity ranking support.

alter table blog_recommendation_scores
    add column if not exists activity_score double precision not null default 0,
    add column if not exists own_author_score double precision not null default 0,
    add column if not exists reviewer_score double precision not null default 0,
    add column if not exists seen_penalty double precision not null default 0,
    add column if not exists repetition_penalty double precision not null default 0;

create table if not exists feed_impressions (
    id uuid primary key,
    user_id uuid not null references users(user_id),
    blog_id uuid not null references blogs(id),
    position integer,
    shown_at timestamp not null,
    clicked boolean not null default false,
    dismissed boolean not null default false
);

create index if not exists idx_feed_impressions_user_blog_shown
    on feed_impressions(user_id, blog_id, shown_at desc);

create index if not exists idx_feed_impressions_user_shown
    on feed_impressions(user_id, shown_at desc);

create index if not exists idx_blog_recommendation_scores_user_window_rank
    on blog_recommendation_scores(user_id, window_type, context_region_id, computed_at, rank_position);

-- 3) Verification query. It should return zero rows after a successful schema sync.

with required_columns(table_name, column_name) as (
    values
        ('blog_likes', 'actor_context_type'),
        ('blog_likes', 'actor_cafe_page_id'),
        ('comments', 'actor_context_type'),
        ('comments', 'actor_cafe_page_id'),
        ('blog_shares', 'actor_context_type'),
        ('blog_shares', 'actor_cafe_page_id'),
        ('notifications', 'actor_context_type'),
        ('notifications', 'actor_cafe_page_id'),
        ('notifications', 'target_type'),
        ('notifications', 'target_cafe_page_id'),
        ('payments', 'cafe_page_id'),
        ('blog_recommendation_scores', 'activity_score'),
        ('blog_recommendation_scores', 'own_author_score'),
        ('blog_recommendation_scores', 'reviewer_score'),
        ('blog_recommendation_scores', 'seen_penalty'),
        ('blog_recommendation_scores', 'repetition_penalty')
)
select required_columns.table_name, required_columns.column_name
from required_columns
left join information_schema.columns existing_columns
    on existing_columns.table_schema = current_schema()
    and existing_columns.table_name = required_columns.table_name
    and existing_columns.column_name = required_columns.column_name
where existing_columns.column_name is null;
