create table post(
    id serial primary key,
    name varchar(255) not null,
    link text not null unique,
    description text not null,
    created timestamp with time zone not null
);