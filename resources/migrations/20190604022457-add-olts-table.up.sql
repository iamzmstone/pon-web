create table olts(
  id int not null auto_increment primary key,
  name varchar(256),
  ip varchar(64),
  brand varchar(64),
  dev_code varchar(256),
  machine_room varchar(64),
  category varchar(64),
  upd_time timestamp);