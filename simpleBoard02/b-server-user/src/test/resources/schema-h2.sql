-- ROLE
DROP TABLE IF EXISTS USERS;
DROP TABLE IF EXISTS "ROLE";

CREATE TABLE "ROLE" ( id bigint generated by default as identity, description varchar(255), grade varchar(255) not null unique, primary key (id) );

-- USERS
CREATE TABLE USERS ( withdrawal bit default 0 comment '탈퇴 여부', created_at timestamp(6), id bigint generated by default as identity, modified_at timestamp(6), role_id bigint not null, nickname varchar(30) not null, email varchar(50) not null, password varchar(255) not null, primary key (id) );

--
-- DROP TABLE IF EXISTS USERS;
-- DROP TABLE IF EXISTS "ROLE";
--
-- CREATE TABLE "ROLE" (
--          id bigint generated by default as identity,
--          description varchar(255),
--          grade varchar(255) not null unique,
--          primary key (id)
-- );
--
-- CREATE TABLE USERS (
--          withdrawal bit default 0 comment '탈퇴 여부',
--          created_at timestamp(6),
--          id bigint generated by default as identity,
--          modified_at timestamp(6),
--          role_id bigint not null,
--          nickname varchar(30) not null,
--          email varchar(50) not null,
--          password varchar(255) not null,
--          primary key (id)
-- );