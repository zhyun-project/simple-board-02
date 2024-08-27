-- ROLE
drop table if exists `simple-board02`.`role`;
create table `simple-board02`.`role` (
  id bigint not null,
  description varchar(255),
  grade varchar(255) not null,
  primary key (id)
) engine=InnoDB;

-- USERS
create table if not exists `simple-board02`.`users` (
    withdrawal bit default 0 comment '탈퇴 여부',
   created_at datetime(6),
   id bigint not null auto_increment,
   modified_at datetime(6),
   role_id bigint not null,
   nickname varchar(30) not null,
   email varchar(50) not null,
   password varchar(255) not null,
   primary key (id)
) engine=InnoDB;