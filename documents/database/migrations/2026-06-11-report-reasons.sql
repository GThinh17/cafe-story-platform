-- CafeStory report reason catalog proposal.
-- The backend currently uses spring.jpa.hibernate.ddl-auto=update, so this file is
-- a PostgreSQL-compatible migration proposal for environments that apply SQL manually.

create extension if not exists pgcrypto;

create table if not exists report_reasons (
    id uuid primary key,
    code varchar(80) not null unique,
    label_vi varchar(160) not null,
    description_vi text,
    target_type varchar(40),
    severity integer not null default 1,
    requires_description boolean not null default false,
    is_active boolean not null default true,
    sort_order integer not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp
);

alter table content_reports
    add column if not exists reason_id uuid;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'fk_content_reports_reason'
    ) then
        alter table content_reports
            add constraint fk_content_reports_reason
            foreign key (reason_id)
            references report_reasons (id);
    end if;
end $$;

insert into report_reasons (
    id,
    code,
    label_vi,
    target_type,
    severity,
    requires_description,
    is_active,
    sort_order,
    created_at,
    updated_at
)
values
    (gen_random_uuid(), 'DISLIKE_CONTENT', 'Chỉ là tôi không thích nội dung này', null, 1, false, true, 10, current_timestamp, current_timestamp),
    (gen_random_uuid(), 'BULLYING_OR_UNWANTED_CONTACT', 'Bắt nạt hoặc liên hệ theo cách không mong muốn', null, 3, false, true, 20, current_timestamp, current_timestamp),
    (gen_random_uuid(), 'SELF_HARM_OR_ABNORMAL_EATING', 'Tự tử, tự gây thương tích hoặc ăn uống thất thường', null, 5, true, true, 30, current_timestamp, current_timestamp),
    (gen_random_uuid(), 'VIOLENCE_HATE_OR_EXPLOITATION', 'Bạo lực, thù ghét hoặc bóc lột', null, 5, true, true, 40, current_timestamp, current_timestamp),
    (gen_random_uuid(), 'RESTRICTED_GOODS', 'Bán hoặc quảng bá mặt hàng bị hạn chế', null, 4, false, true, 50, current_timestamp, current_timestamp),
    (gen_random_uuid(), 'NUDITY_OR_SEXUAL_ACTIVITY', 'Ảnh khỏa thân hoặc hoạt động tình dục', null, 5, true, true, 60, current_timestamp, current_timestamp),
    (gen_random_uuid(), 'SCAM_FRAUD_OR_SPAM', 'Lừa đảo, gian lận hoặc spam', null, 4, false, true, 70, current_timestamp, current_timestamp),
    (gen_random_uuid(), 'FALSE_INFORMATION', 'Thông tin sai sự thật', 'BLOG', 3, false, true, 80, current_timestamp, current_timestamp),
    (gen_random_uuid(), 'INTELLECTUAL_PROPERTY', 'Quyền sở hữu trí tuệ', null, 3, true, true, 90, current_timestamp, current_timestamp)
on conflict (code) do nothing;

create index if not exists idx_report_reasons_active_severity
    on report_reasons (is_active, severity desc, sort_order asc);

create index if not exists idx_content_reports_reason_id
    on content_reports (reason_id);
