CREATE TABLE IF NOT EXISTS ARTICLES ( article_id bigint not null, created_at timestamp(6), id bigint generated by default as identity, modified_at timestamp(6), user_id bigint not null, title varchar(60) not null, content clob not null, primary key (id) );
--
-- CREATE TABLE IF NOT EXISTS ARTICLES (
--     article_id bigint not null,
--     created_at timestamp(6),
--     id bigint generated by default as identity,
--     modified_at timestamp(6),
--     user_id bigint not null,
--     title varchar(60) not null,
--     content clob not null,
--     primary key (id)
-- );
