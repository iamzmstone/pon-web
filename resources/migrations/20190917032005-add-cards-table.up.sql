create table cards(
  id        int not null auto_increment primary key,
  olt_id    int not null,
  slot      int,
  card_type varchar(16),
  model     varchar(16),
  port_cnt  int,
  hard_ver  varchar(16),
  soft_ver  varchar(16),
  status    varchar(16),
  upd_time  timestamp,
  foreign key (olt_id) references olts(id) on delete cascade);