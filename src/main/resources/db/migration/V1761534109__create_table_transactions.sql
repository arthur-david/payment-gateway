create table if not exists transactions (
    id bigserial primary key,
    originator_account_id bigint references accounts(id),
    destination_account_id bigint references accounts(id),
    amount numeric not null,
    type varchar not null,
    purpose varchar not null,
    charge_id bigint references charges(id),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);