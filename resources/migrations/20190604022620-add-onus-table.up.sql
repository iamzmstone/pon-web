create table onus(
  id int not null auto_increment primary key,
  olt_id int not null,
  pon varchar(8) not null,
  oid int not null,
  sn varchar(128),
  type varchar(16),
  auth varchar(16),
  model varchar(16),
  upd_time timestamp,
  foreign key (olt_id) references olts(id) on delete cascade);
