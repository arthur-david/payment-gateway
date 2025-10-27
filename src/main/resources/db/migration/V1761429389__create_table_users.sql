create table if not exists users (
    id bigserial primary key,
    name varchar not null,
    cpf varchar not null unique,
    email varchar not null unique,
    password varchar not null,
    last_changed_password_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);