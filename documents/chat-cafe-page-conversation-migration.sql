alter table conversations
    add column if not exists cafe_page_id uuid;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'fk_conversations_cafe_page'
    ) then
        alter table conversations
            add constraint fk_conversations_cafe_page
            foreign key (cafe_page_id)
            references cafe_pages(id);
    end if;
end $$;

create index if not exists idx_conversations_type_page
    on conversations(type, cafe_page_id);

create index if not exists idx_conversations_updated_at
    on conversations(updated_at);

alter table chat_messages
    add column if not exists sender_context_type varchar(40) not null default 'USER',
    add column if not exists sender_cafe_page_id uuid;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'fk_chat_messages_sender_cafe_page'
    ) then
        alter table chat_messages
            add constraint fk_chat_messages_sender_cafe_page
            foreign key (sender_cafe_page_id)
            references cafe_pages(id);
    end if;
end $$;

create index if not exists idx_chat_messages_sender_context_page
    on chat_messages(sender_context_type, sender_cafe_page_id);
