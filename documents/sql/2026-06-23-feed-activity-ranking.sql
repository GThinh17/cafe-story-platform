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
