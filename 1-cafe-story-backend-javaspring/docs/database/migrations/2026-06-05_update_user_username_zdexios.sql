-- PostgreSQL migration proposal for updating a CafeStory username.
-- Apply manually because this project does not currently use Flyway or Liquibase.

begin;

do $$
declare
    target_user_id uuid := '1b358b35-69f6-4c31-ad7b-eb2fd4e58c12';
    target_username text := 'zdexios';
begin
    if exists (
        select 1
        from users
        where user_id <> target_user_id
          and lower(user_name) = lower(target_username)
    ) then
        raise exception 'Username "%" is already used by another user.', target_username;
    end if;

    update users
    set user_name = target_username
    where user_id = target_user_id;

    if not found then
        raise exception 'User "%" was not found.', target_user_id;
    end if;
end $$;

commit;
