create table if not exists accounts (
    id bigserial primary key,
    user_id bigint not null,
    status varchar not null,
    total_balance numeric not null default 0,
    hold_balance numeric not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    foreign key (user_id) references users (id)
);