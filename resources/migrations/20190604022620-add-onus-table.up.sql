create table onus(
  id int not null auto_increment primary key,
  olt_id int not null,
  batch_id int not null,
  pon varchar(8) not null,
  oid int not null,
  sn varchar(128),
  upd_time timestamp,
  foreign key (olt_id) references olts(id) on delete cascade,
  foreign key (batch_id) references batches(id) on delete cascade);