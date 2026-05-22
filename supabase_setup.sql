-- ============================================================
--  CalcApp — Supabase SQL Setup
--  Run this entire script in: Supabase > SQL Editor > New Query
-- ============================================================

-- 1. Users table
create table if not exists users (
    id            serial primary key,
    username      text unique not null,
    password_hash text not null,
    role          text not null default 'user',
    created_at    timestamptz default now()
);

-- 2. Calculation history table
create table if not exists calc_history (
    id         serial primary key,
    user_id    int references users(id) on delete cascade,
    expression text not null,
    result     text not null,
    created_at timestamptz default now()
);

-- 3. Enable Row Level Security
alter table users        enable row level security;
alter table calc_history enable row level security;

-- 4. Policies — allow anon key full access (suitable for desktop app)
--    (Tighten these in production with user-specific policies)
drop policy if exists "anon_all_users"   on users;
drop policy if exists "anon_all_history" on calc_history;

create policy "anon_all_users"
    on users for all
    using (true)
    with check (true);

create policy "anon_all_history"
    on calc_history for all
    using (true)
    with check (true);

-- ============================================================
--  Done! You can now run CalcApp and register your first user.
-- ============================================================
