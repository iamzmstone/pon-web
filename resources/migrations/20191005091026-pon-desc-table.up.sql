create table pon_desc(
  id int not null auto_increment primary key,
  olt_id int not null,
  pon varchar(8) not null,
  name varchar(128),
  upd_time timestamp,
  foreign key (olt_id) references olts(id) on delete cascade);

  
  
