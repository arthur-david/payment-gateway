create table if not exists refresh_tokens (
    id bigserial primary key,
    jti varchar not null unique,
    user_id bigint not null,
    issued_at timestamp not null,
    expires_at timestamp not null,
    used boolean not null default false,
    revoked boolean not null default false,
    user_agent varchar,
    ips varchar,
    foreign key (user_id) references users (id)
);