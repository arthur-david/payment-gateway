create table if not exists charge_payments (
    id bigserial primary key,
    charge_id bigint not null references charges(id),
    payment_method varchar not null,
    authorization_identifier varchar not null,
    card_number varchar,
    paid_at timestamp,
    cancelled_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);