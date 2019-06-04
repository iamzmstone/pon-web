create table onu_states(
  id int not null auto_increment primary key,
  onu_id int not null,
  batch_id int not null,
  state varchar(16) not null,
  rx_power decimal(10,3) not null,
  in_Bps int not null,
  out_Bps int not null,
  in_bw int not null,
  out_bw int not null,
  upd_time timestamp,
  foreign key (onu_id) references onus(id) on delete cascade,
  foreign key (batch_id) references batches(id) on delete cascade);