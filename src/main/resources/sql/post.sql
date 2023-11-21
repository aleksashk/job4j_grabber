create table post(
    id serial primary key,
    name varchar(255) not null,
    description text not null,
    link text not null unique,
    created timestamp with time zone not null
);