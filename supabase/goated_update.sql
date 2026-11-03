alter table public.reviews
    alter column rating type numeric(2,1)
    using rating::numeric(2,1);

alter table public.reviews
    drop constraint if exists reviews_rating_half_step_check;

alter table public.reviews
    add constraint reviews_rating_half_step_check
    check (
        rating is null
        or (
            rating >= 0.5
            and rating <= 5.0
            and mod(rating * 2, 1) = 0
        )
    );

create unique index if not exists reviews_user_game_unique
    on public.reviews(user_id, game_id);

alter table public.games
    add column if not exists rating_count integer not null default 0;

update public.games g
set rating_count = (
    select count(*) from public.reviews r
    where r.game_id = g.id and r.rating is not null
);

alter table public.profiles
    add column if not exists avatar_color text not null default 'blue';

alter table public.profiles
    add column if not exists banner_style text not null default 'blue';

alter table public.profiles
    drop constraint if exists profiles_avatar_color_check;

alter table public.profiles
    add constraint profiles_avatar_color_check
    check (avatar_color in ('blue', 'violet', 'green', 'orange'));

alter table public.profiles
    drop constraint if exists profiles_banner_style_check;

alter table public.profiles
    add constraint profiles_banner_style_check
    check (banner_style in ('blue', 'violet', 'green', 'ember'));

create or replace function public.refresh_game_rating()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    target_game_id public.games.id%type;
begin
    target_game_id := coalesce(new.game_id, old.game_id);

    update public.games
    set
        average_rating = (
            select round(avg(rating)::numeric, 2)
            from public.reviews
            where game_id = target_game_id
              and rating is not null
        ),
        rating_1_count = (select count(*) from public.reviews where game_id = target_game_id and rating = 1),
        rating_2_count = (select count(*) from public.reviews where game_id = target_game_id and rating = 2),
        rating_3_count = (select count(*) from public.reviews where game_id = target_game_id and rating = 3),
        rating_4_count = (select count(*) from public.reviews where game_id = target_game_id and rating = 4),
        rating_5_count = (select count(*) from public.reviews where game_id = target_game_id and rating = 5),
        rating_count = (select count(*) from public.reviews where game_id = target_game_id and rating is not null)
    where id = target_game_id;

    if tg_op = 'DELETE' then
        return old;
    end if;
    return new;
end;
$$;

drop trigger if exists reviews_refresh_game_rating on public.reviews;

create trigger reviews_refresh_game_rating
after insert or update or delete on public.reviews
for each row execute function public.refresh_game_rating();

create or replace function public.enforce_favorites_limit()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    favorite_count integer;
begin
    perform pg_advisory_xact_lock(hashtext(new.user_id::text));

    select count(*)
    into favorite_count
    from public.favorites
    where user_id = new.user_id;

    if favorite_count >= 4 then
        raise exception 'В избранном может быть не более четырёх игр';
    end if;

    return new;
end;
$$;

drop trigger if exists favorites_limit_four on public.favorites;

create trigger favorites_limit_four
before insert on public.favorites
for each row execute function public.enforce_favorites_limit();


alter table public.profiles enable row level security;

drop policy if exists profiles_update_own_style on public.profiles;

create policy profiles_update_own_style
on public.profiles
for update
to authenticated
using (id = auth.uid())
with check (id = auth.uid());

grant update (avatar_color, banner_style) on public.profiles to authenticated;
