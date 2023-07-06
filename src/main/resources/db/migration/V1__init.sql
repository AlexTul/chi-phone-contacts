create table user_statuses
(
    id          int  not null primary key,
    description text not null
);

insert into user_statuses (id, description)
values (0, 'ACTIVE');
insert into user_statuses (id, description)
values (1, 'SUSPENDED');

create table users
(
    id              bigserial primary key,
    email           text        not null,
    nickname        text        not null,
    status          int         not null references user_statuses (id),
    password        text        not null,
    created_at      timestamptz not null default now(),
    activation_code text
);

create unique index users_email_uindex on users (email);
create unique index users_nickname_uindex on users (nickname);

create table authorities
(
    id    int primary key,
    value text not null
);

create unique index authorities_value_uindex on authorities (value);

create table user_authorities
(
    user_id      bigint not null,
    authority_id int    not null,
    primary key (user_id, authority_id),
    constraint user_authorities_users_fk foreign key (user_id)
        references users (id) on delete cascade,
    constraint user_authorities_authorities_fk foreign key (authority_id)
        references authorities (id) on delete cascade
);

insert into authorities (id, value)
values (0, 'ROLE_USER');
insert into authorities (id, value)
values (1, 'ROLE_ADMIN');

create table refresh_tokens
(
    value     uuid        not null primary key,
    user_id   bigint      not null,
    issued_at timestamptz not null,
    expire_at timestamptz not null,
    next      uuid,
    constraint refresh_tokens_user_fk foreign key (user_id)
        references users (id) on delete cascade,
    constraint refresh_tokens_next_fk foreign key (next)
        references refresh_tokens (value) on delete cascade
);

create procedure prune_refresh_tokens()
    language SQL
as
$$
delete
from refresh_tokens rt
where rt.expire_at < current_timestamp
   or rt.user_id in (select u.id from users u where u.id = rt.user_id and u.status = 1)
$$;

create table contacts
(
    id      bigserial not null primary key,
    name    text      not null unique,
    user_id bigint    not null,
    constraint contacts_user_fk foreign key (user_id)
        references users (id) on delete cascade
);

create unique index contacts_name_index on contacts (name);

create table emails
(
    id         bigserial not null primary key,
    value      text      not null unique,
    contact_id bigint    not null,
    constraint emails_contact_fk foreign key (contact_id)
        references contacts (id) on delete cascade
);

create unique index emails_value_index on emails (value);

create table phones
(
    id         bigserial not null primary key,
    value      text      not null unique,
    contact_id bigint    not null,
    constraint phones_contact_fk foreign key (contact_id)
        references contacts (id) on delete cascade
);

create unique index phones_value_index on phones (value);