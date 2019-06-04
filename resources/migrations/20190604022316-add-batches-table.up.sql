create table batches(
  id int not null auto_increment primary key,
  name varchar(256) not null,
  start_time datetime,
  end_time datetime,
  finished bool default 0,
  upd_time timestamp);