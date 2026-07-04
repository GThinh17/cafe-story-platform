alter table ai_moderation_results
    add column if not exists comment_id uuid;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'fk_ai_moderation_results_comment'
    ) then
        alter table ai_moderation_results
            add constraint fk_ai_moderation_results_comment
            foreign key (comment_id)
            references comments(id)
            on delete cascade;
    end if;
end $$;

create index if not exists idx_ai_moderation_comment_decision
    on ai_moderation_results(comment_id, decision);
