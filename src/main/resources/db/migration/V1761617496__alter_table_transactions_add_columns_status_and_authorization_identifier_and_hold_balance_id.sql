alter table transactions add column status varchar not null;
alter table transactions add column authorization_identifier varchar not null;
alter table transactions add column hold_balance_id bigint references hold_balances(id);