create table if not exists transactions (
    id bigserial primary key,
    account_id bigint not null references accounts(id),
    amount numeric not null,
    type varchar not null,
    purpose varchar not null,
    reference text,
    charge_id bigint references charges(id),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);