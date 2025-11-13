DROP TABLE IF EXISTS users;
CREATE TABLE users(id bigint primary key auto_increment, name varchar(255));

INSERT INTO users (name) values ('name_1');
INSERT INTO users (name) values ('name_2');
INSERT INTO users (name) values ('name_3');
