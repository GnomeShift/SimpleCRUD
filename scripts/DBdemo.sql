create table if not exists client (
    id integer primary key autoincrement,
    name text,
    createdAt timestamp,
    updatedAt timestamp
);