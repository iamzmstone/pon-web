create table olts(
  id int not null auto_increment primary key,
  name varchar(256),
  ip varchar(64),
  brand varchar(16),
  upd_time timestamp);