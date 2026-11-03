-- =====================================================================
-- Goated: исправление регистрации/входа, RLS, оценок и рецензий
-- Выполнить целиком в Supabase Dashboard -> SQL Editor.
-- Скрипт идемпотентный: его можно запускать повторно.
-- Таблицы не пересоздаются, пользовательские данные не удаляются.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. profiles: RLS
-- Причина неработающей регистрации: на profiles включён RLS, но была
-- только политика UPDATE. Поэтому приложение не могло ни создать
-- профиль (INSERT), ни прочитать его (SELECT), а reviews/favorites/
-- activity_feed падали на внешнем ключе user_id -> profiles.id.
-- ---------------------------------------------------------------------
alter table public.profiles enable row level security;

drop policy if exists profiles_select_all on public.profiles;
create policy profiles_select_all
    on public.profiles for select
    to anon, authenticated
    using (true);

drop policy if exists profiles_insert_own on public.profiles;
create policy profiles_insert_own
    on public.profiles for insert
    to authenticated
    with check (id = auth.uid());

drop policy if exists profiles_update_own_style on public.profiles;
drop policy if exists profiles_update_own on public.profiles;
create policy profiles_update_own
    on public.profiles for update
    to authenticated
    using (id = auth.uid())
    with check (id = auth.uid());

-- Публично читаются все столбцы профиля, кроме email.
-- Email нужен только для входа по логину и выдаётся функцией ниже.
do $$
declare
    cols text;
begin
    select string_agg(quote_ident(column_name), ', ')
    into cols
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'profiles'
      and column_name <> 'email';

    execute 'revoke select on public.profiles from anon, authenticated';
    execute format('grant select (%s) on public.profiles to anon, authenticated', cols);
end $$;

grant insert, update on public.profiles to authenticated;

-- ---------------------------------------------------------------------
-- 2. Уникальность username (без учёта регистра)
-- Индекс создаётся только если в таблице нет дубликатов,
-- чтобы скрипт не падал на существующих данных.
-- ---------------------------------------------------------------------
do $$
begin
    if not exists (
        select 1 from public.profiles
        where username is not null
        group by lower(username)
        having count(*) > 1
    ) then
        create unique index if not exists profiles_username_lower_unique
            on public.profiles (lower(username));
    else
        raise notice 'Найдены повторяющиеся username - уникальный индекс не создан';
    end if;
end $$;

-- ---------------------------------------------------------------------
-- 3. Автоматическое создание профиля при регистрации
-- Username передаётся приложением в user_metadata при signUp.
-- ---------------------------------------------------------------------
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, username, email)
    values (
        new.id,
        coalesce(nullif(trim(new.raw_user_meta_data ->> 'username'), ''),
                 'user_' || left(new.id::text, 8)),
        new.email
    )
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- Профили для уже зарегистрированных пользователей, у которых профиля нет
-- (они появились, пока регистрация была сломана).
insert into public.profiles (id, username, email)
select u.id,
       coalesce(nullif(trim(u.raw_user_meta_data ->> 'username'), ''),
                'user_' || left(u.id::text, 8)),
       u.email
from auth.users u
where not exists (select 1 from public.profiles p where p.id = u.id)
on conflict (id) do nothing;

-- ---------------------------------------------------------------------
-- 4. Вход по логину: получить email по username
-- ---------------------------------------------------------------------
create or replace function public.get_email_by_username(p_username text)
returns text
language sql
stable
security definer
set search_path = public
as $$
    select coalesce(p.email, u.email)
    from public.profiles p
    left join auth.users u on u.id = p.id
    where lower(p.username) = lower(trim(p_username))
    limit 1;
$$;

revoke all on function public.get_email_by_username(text) from public;
grant execute on function public.get_email_by_username(text) to anon, authenticated;

-- ---------------------------------------------------------------------
-- 5. reviews: оценки с шагом 0.5, одна запись на пользователя и игру
-- ---------------------------------------------------------------------
alter table public.reviews
    alter column rating type numeric(2,1) using rating::numeric(2,1);

alter table public.reviews alter column rating drop not null;

alter table public.reviews
    drop constraint if exists reviews_rating_half_step_check;
alter table public.reviews
    add constraint reviews_rating_half_step_check
    check (rating is null or (rating >= 0.5 and rating <= 5.0 and mod(rating * 2, 1) = 0));

create unique index if not exists reviews_user_game_unique
    on public.reviews (user_id, game_id);

alter table public.reviews enable row level security;

drop policy if exists reviews_select_all on public.reviews;
create policy reviews_select_all on public.reviews
    for select to anon, authenticated using (true);

drop policy if exists reviews_insert_own on public.reviews;
create policy reviews_insert_own on public.reviews
    for insert to authenticated with check (user_id = auth.uid());

drop policy if exists reviews_update_own on public.reviews;
create policy reviews_update_own on public.reviews
    for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists reviews_delete_own on public.reviews;
create policy reviews_delete_own on public.reviews
    for delete to authenticated using (user_id = auth.uid());

grant select on public.reviews to anon, authenticated;
grant insert, update, delete on public.reviews to authenticated;

-- ---------------------------------------------------------------------
-- 6. favorites и activity_feed: доступ только к своим записям
-- ---------------------------------------------------------------------
alter table public.favorites enable row level security;

drop policy if exists favorites_select_own on public.favorites;
create policy favorites_select_own on public.favorites
    for select to authenticated using (user_id = auth.uid());

drop policy if exists favorites_insert_own on public.favorites;
create policy favorites_insert_own on public.favorites
    for insert to authenticated with check (user_id = auth.uid());

drop policy if exists favorites_delete_own on public.favorites;
create policy favorites_delete_own on public.favorites
    for delete to authenticated using (user_id = auth.uid());

grant select, insert, delete on public.favorites to authenticated;

alter table public.activity_feed enable row level security;

drop policy if exists activity_select_own on public.activity_feed;
create policy activity_select_own on public.activity_feed
    for select to authenticated using (user_id = auth.uid());

drop policy if exists activity_insert_own on public.activity_feed;
create policy activity_insert_own on public.activity_feed
    for insert to authenticated with check (user_id = auth.uid());

drop policy if exists activity_delete_own on public.activity_feed;
create policy activity_delete_own on public.activity_feed
    for delete to authenticated using (user_id = auth.uid());

grant select, insert, delete on public.activity_feed to authenticated;

-- Ограничение избранного (не более 4 игр) - на случай, если прошлый скрипт не выполнялся
create or replace function public.enforce_favorites_limit()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    perform pg_advisory_xact_lock(hashtext(new.user_id::text));
    if (select count(*) from public.favorites where user_id = new.user_id) >= 4 then
        raise exception 'В избранном может быть не более четырёх игр';
    end if;
    return new;
end;
$$;

drop trigger if exists favorites_limit_four on public.favorites;
create trigger favorites_limit_four
    before insert on public.favorites
    for each row execute function public.enforce_favorites_limit();

-- ---------------------------------------------------------------------
-- 7. Профиль: оформление
-- ---------------------------------------------------------------------
alter table public.profiles add column if not exists avatar_color text not null default 'blue';
alter table public.profiles add column if not exists banner_style text not null default 'blue';

-- ---------------------------------------------------------------------
-- 8. Средний рейтинг по фактическим оценкам пользователей
-- rating_N_count - число оценок, попадающих в N-ю звезду (0.5..1 -> 1, 1.5..2 -> 2 ...)
-- ---------------------------------------------------------------------
alter table public.games add column if not exists rating_count integer not null default 0;

create or replace function public.recalculate_game_rating(target_game_id uuid)
returns void
language sql
security definer
set search_path = public
as $$
    update public.games g
    set average_rating = s.avg_rating,
        rating_count   = s.cnt,
        rating_1_count = s.c1,
        rating_2_count = s.c2,
        rating_3_count = s.c3,
        rating_4_count = s.c4,
        rating_5_count = s.c5
    from (
        select round(avg(rating)::numeric, 2)          as avg_rating,
               count(rating)::int                       as cnt,
               count(*) filter (where ceil(rating) = 1)::int as c1,
               count(*) filter (where ceil(rating) = 2)::int as c2,
               count(*) filter (where ceil(rating) = 3)::int as c3,
               count(*) filter (where ceil(rating) = 4)::int as c4,
               count(*) filter (where ceil(rating) = 5)::int as c5
        from public.reviews
        where game_id = target_game_id and rating is not null
    ) s
    where g.id = target_game_id;
$$;

create or replace function public.refresh_game_rating()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if tg_op in ('INSERT', 'UPDATE') then
        perform public.recalculate_game_rating(new.game_id);
    end if;
    if tg_op in ('UPDATE', 'DELETE') and (tg_op = 'DELETE' or old.game_id <> new.game_id) then
        perform public.recalculate_game_rating(old.game_id);
    end if;
    return coalesce(new, old);
end;
$$;

drop trigger if exists reviews_refresh_game_rating on public.reviews;
create trigger reviews_refresh_game_rating
    after insert or update or delete on public.reviews
    for each row execute function public.refresh_game_rating();

-- Пересчитать рейтинг всех игр по реальным оценкам
-- (заменяет тестовые значения average_rating, введённые вручную).
select public.recalculate_game_rating(id) from public.games;
