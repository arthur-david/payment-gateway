create table if not exists hold_balances (
    id bigserial primary key,
    account_id bigint not null references accounts(id),
    amount numeric not null,
    type varchar not null,
    status varchar not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);