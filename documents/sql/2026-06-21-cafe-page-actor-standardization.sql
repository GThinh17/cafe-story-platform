-- CafePage actor/target standardization.
-- Apply manually because the backend currently uses JPA_DDL_AUTO=none and has no Flyway/Liquibase runner.

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
