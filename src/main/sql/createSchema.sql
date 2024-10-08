drop table if exists cards;
drop table if exists lists cascade;
drop table if exists user_board;
drop table if exists boards cascade;
drop table if exists users;

create table users (
    number serial,
    name varchar(80) not null,
    email varchar(80) not null unique,
    password varchar(80) not null,
    token varchar(80) not null unique,
    primary key(number),
    constraint EmailFormat check ( email like '%@%.%' )
);

create table boards (
    id serial unique,
    description varchar(80) not null,
    name varchar(80) unique,
    primary key(id)
);

create table user_board(
    userNumber integer,
    boardId integer,
    primary key (userNumber, boardId),
    constraint userNumber foreign key (userNumber) references users(number),
    constraint boardName foreign key (boardId) references boards(id)
);

create table lists (
    id serial,
    name varchar(80) not null,
    boardId integer,
    primary key(id, boardId),
    constraint boardName foreign key (boardId) references boards(id)
);

create table cards (
    id serial unique,
    index integer,
    name varchar(80) not null,
    description varchar(80) not null,
    creationDt date not null,
    conclusionDt date,
    listId integer,
    boardId integer,
    primary key (id, boardId),
    constraint boardId foreign key (boardId) references boards(id)
);

