alter table if exists transactions add column if not exists status varchar not null;
alter table if exists transactions add column if not exists authorization_identifier varchar not null;
alter table if exists transactions add column if not exists hold_balance_id bigint references hold_balances(id);