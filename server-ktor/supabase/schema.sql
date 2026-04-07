-- IdleStables backend (MVP)
-- Best-practice stance: server-only writes/reads; clients do NOT talk to Supabase directly.
-- Apply in Supabase SQL editor.

-- ==========================================================
-- 0) Extensions
-- ==========================================================
create extension if not exists pgcrypto;

-- ==========================================================
-- 1) Raw index table (stores program-owned accounts as fetched)
-- ==========================================================
create table if not exists public.program_accounts (
  pubkey text primary key,
  owner_program text not null,
  lamports bigint not null,
  data_base64 text not null,
  data_encoding text not null default 'base64',
  rpc_slot bigint,
  updated_at timestamptz not null default now()
);

create index if not exists program_accounts_owner_program_idx
  on public.program_accounts(owner_program);

-- ==========================================================
-- 2) Decoded tables (query-friendly)
-- NOTE: these will be populated by the Ktor backend decoder.
-- ==========================================================

create table if not exists public.tracks (
  track_pubkey text primary key,
  config_pubkey text,
  track_id integer,
  name text,
  distance text,
  cadence_minutes integer,
  base_entry_fee bigint,
  updated_at timestamptz not null default now()
);

create index if not exists tracks_track_id_idx
  on public.tracks(track_id);

create table if not exists public.races (
  race_pubkey text primary key,
  track_pubkey text,
  scheduled_ts bigint,
  entry_closes_ts bigint,
  field_size integer,
  is_mega_cup boolean,
  status text,
  resolved boolean,
  top1 text,
  top2 text,
  top3 text,
  prize1 bigint,
  prize2 bigint,
  prize3 bigint,
  updated_at timestamptz not null default now()
);

create index if not exists races_track_scheduled_idx
  on public.races(track_pubkey, scheduled_ts desc);

create index if not exists races_resolved_idx
  on public.races(resolved);

create table if not exists public.horses (
  horse_pubkey text primary key,
  owner_pubkey text,
  seed bigint,
  speed integer,
  stamina integer,
  focus integer,
  temperament integer,
  updated_at timestamptz not null default now()
);

create index if not exists horses_owner_idx
  on public.horses(owner_pubkey);

-- ==========================================================
-- 3) Server-only access posture (RLS ON + no client policies)
-- ==========================================================
alter table public.program_accounts enable row level security;
alter table public.tracks enable row level security;
alter table public.races enable row level security;
alter table public.horses enable row level security;

-- Intentionally NO policies created. With RLS enabled, anon/authenticated clients get no access.
-- The Ktor backend uses the service role key over PostgREST and bypasses RLS.

-- ==========================================================
-- 4) Convenience RPC function for backend health checks (optional)
-- ==========================================================
create or replace function public.ping() returns text
language sql stable
as $$
  select 'ok';
$$;
