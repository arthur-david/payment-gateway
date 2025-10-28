create table if not exists charges (
    id bigserial primary key,
    identifier varchar unique not null,
    originator_user_id bigint not null references users(id),
    destination_user_id bigint not null references users(id),
    amount numeric not null,
    description text,
    status varchar not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);