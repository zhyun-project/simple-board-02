create table if not exists `simple-board02`.`articles` (
    article_id bigint not null,
    created_at datetime(6),
    id bigint not null auto_increment,
    modified_at datetime(6),
    user_id bigint not null,
    title varchar(60) not null,
    content tinytext not null,
    primary key (id)
) engine=InnoDB;