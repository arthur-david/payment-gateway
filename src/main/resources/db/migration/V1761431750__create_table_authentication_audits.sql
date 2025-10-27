create table if not exists authentication_audits (
    id bigserial primary key,
    user_id bigint not null,
    ips varchar,
    action varchar not null,
    success boolean,
    message varchar,
    created_at timestamp not null default current_timestamp,
    foreign key (user_id) references users (id)
);