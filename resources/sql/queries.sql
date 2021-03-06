-- :name add-olt :i!
-- :doc add a new olt record
INSERT INTO olts
(name, ip)
VALUES (:name, :ip)

-- :name get-olt-by-name :? :1
-- :doc retrieve an olt record by name provided
SELECT * FROM olts
 WHERE name = :name

-- :name get-olt-by-id :? :1
-- :doc retrieve an olt record by id provided
SELECT * FROM olts
 WHERE id = :id

-- :name all-olts :? :*
-- :doc retrieve all olt records
SELECT * FROM olts
ORDER BY name

-- :name all-cards :? :*
-- :doc retrieve all card records
SELECT * FROM cards

-- :name olt-cards :? :*
-- :doc retrieve cards records for a given olt
SELECT * FROM cards
 WHERE olt_id = :olt_id

-- :name add-batch :i!
-- :doc add a new batch record
INSERT INTO batches
(name, start_time)
VALUES (:name, :start_time)

-- :name latest-done-batch :? :1
-- :doc get latest batch which is finish
SELECT * FROM batches
 WHERE finished is true
 ORDER BY id DESC
 LIMIT 1

-- :name get-batch-by-name :? :1
-- :doc retrieve a batch record by name provided
SELECT * FROM batches
WHERE name = :name

-- :name done-batches :? :*
-- :doc retrieve all batch records finished
SELECT id, name, date_format(start_time, '%Y-%m-%d %H:%i:%s') st,
       date_format(end_time, '%Y-%m-%d %H:%i:%s') et
  FROM batches
 WHERE finished = true

-- :name pon-desc-card
-- :doc retrieve pon_desc record for given olt_id and slot
SELECT a.*, b.name olt_name FROM pon_desc a, olts b
 WHERE a.olt_id = b.id
   AND olt_id = :olt_id
   AND pon like :pon

-- :name add-onu :i!
-- :doc add a new onu record
INSERT INTO onus
(olt_id, batch_id, pon, oid, sn)
VALUES (:olt_id, :batch_id, :pon, :oid, :sn)

-- :name all-onus :? :*
-- :doc retrieve all onus
SELECT a.*, b.name FROM onus a, olts b
 WHERE a.olt_id = b.id
 LIMIT :s, :l

-- :name onu-cnt :? :1
-- :doc retrieve count of onus
SELECT count(*) cnt FROM onus

-- :name get-onu-by-id :? :1
-- :doc retrieve onu record by id
SELECT * FROM onus
 WHERE id = :id

-- :name get-onu :? :1
-- :doc retrieve onu record by pon and oid
SELECT * FROM onus
 WHERE pon = :pon
   AND oid = :oid
   AND olt_id = :olt_id

-- :name olt-onus :? :*
-- :doc retrieve all onus of an olt
SELECT * FROM onus
 WHERE olt_id = :olt_id

-- :name batch-onus :? :*
-- :doc retrieve all onus of a batch
SELECT a.*, b.name FROM onus a, olts b
 WHERE a.olt_id = b.id
   AND batch_id = :batch_id
 LIMIT :s, :l

-- :name add-state :i!
-- :doc add a new onu_state record
INSERT INTO onu_states
(onu_id, batch_id, state, rx_power, in_bps, out_bps, in_bw, out_bw)
VALUES (:onu_id, :batch_id, :state, :rx_power, :in_bps, :out_bps, :in_bw, :out_bw)

-- :name get-state :? :1
-- :doc retrieve a onu_state record according onu_id and batch_id
SELECT state, rx_power, in_bps, out_bps, in_bw, out_bw, date_format(upd_time, '%Y-%m-%d %H:%i:%s') upd_tm
  FROM onu_states
 WHERE onu_id = :onu_id
   AND batch_id = :batch_id

-- :name get-state-by-id :? :1
-- :doc retrieve an onu_state record by id
SELECT * FROM onu_states
 WHERE id = :id

-- :name upd-state :! :n
-- :doc update state according to id
UPDATE onu_states
   SET in_bw = :in_bw,
       out_bw = :out_bw,
       in_bps = :in_bps,
       out_bps = :out_bps,
       rx_power = :rx_power,
       state = :state
 WHERE id = :id

-- :name onu-states :? :*
-- :doc retrieve all states record of a specific onu
SELECT a.state, a.rx_power, a.in_bps, a.out_bps, a.in_bw, a.out_bw,
       date_format(a.upd_time, '%Y-%m-%d %H:%i:%s') upd_tm
  FROM onu_states a
 WHERE a.onu_id = :onu_id
 LIMIT 20

-- :name pon-states :? :*
-- :doc retrieve all states record of a specific olt_id and pon
SELECT a.onu_id, a.state, a.rx_power, a.in_bps, a.out_bps, a.in_bw, a.out_bw,
       date_format(a.upd_time, '%Y-%m-%d %H:%i:%s') upd_tm,
       b.pon, b.oid, b.sn, b.name, b.model, b.auth, b.type,
       c.name olt_name, d.name bat_name
  FROM onu_states a, onus b, olts c, batches d
 WHERE a.batch_id = :batch_id
   AND a.olt_id = :olt_id
   AND a.pon = :pon
   AND a.onu_id = b.id AND b.olt_id = c.id AND a.batch_id = d.id
 ORDER BY b.upd_time desc
 LIMIT :s, :l

-- :name batch-states :? :*
-- :doc retrieve all states record of a specific batch
SELECT a.onu_id, a.state, a.rx_power, a.in_bps, a.out_bps, a.in_bw, a.out_bw,
       date_format(a.upd_time, '%Y-%m-%d %H:%i:%s') upd_tm,
       b.olt_id, b.pon, b.oid, b.sn, b.name, b.model, b.auth, b.type,
       c.name olt_name, d.name bat_name
  FROM onu_states a, onus b, olts c, batches d
 WHERE a.batch_id = :batch_id
   AND a.onu_id = b.id AND b.olt_id = c.id AND a.batch_id = d.id
 ORDER BY b.upd_time desc
 LIMIT :s, :l

-- :name batch-states-cnt :? :1
-- :doc retrieve count of batch-states
SELECT count(*) cnt
  FROM onu_states a, onus b, olts c
 WHERE a.batch_id = :batch_id AND a.onu_id = b.id AND b.olt_id = c.id

-- :name search-states :? :*
-- :doc retrieve onu states match search conditions
SELECT a.id, a.onu_id, a.state, a.rx_power, a.in_bps, a.out_bps, a.in_bw, a.out_bw,
       date_format(a.upd_time, '%Y-%m-%d %H:%i:%s') upd_tm,
       b.pon, b.oid, b.sn, b.name, b.model, b.auth, b.type,
       c.name olt_name, d.name bat_name
  FROM onu_states a, onus b, olts c, batches d
 WHERE a.batch_id = :batch_id
   AND a.onu_id = b.id AND b.olt_id = c.id AND a.batch_id = d.id
   AND b.sn like :sn
   AND b.name like :name
   AND b.pon like :pon
   AND a.rx_power BETWEEN :rx_min AND :rx_max
   AND a.in_bps >= :inbps
   AND a.out_bps >= :outbps
   AND a.in_bw >= :inbw
   AND a.state in (:v*:states)
   AND c.id in (:v*:olts)
 LIMIT :s, :l

-- :name search-states-cnt :? :1
-- :doc retrieve count of onu states match search conditions
SELECT count(*) cnt
  FROM onu_states a, onus b, olts c, batches d
 WHERE a.batch_id = :batch_id
   AND a.onu_id = b.id AND b.olt_id = c.id AND a.batch_id = d.id
   AND b.sn like :sn
   AND b.name like :name
   AND b.pon like :pon
   AND a.rx_power BETWEEN :rx_min AND :rx_max
   AND a.in_bps >= :inbps
   AND a.out_bps >= :outbps
   AND a.in_bw >= :inbw
   AND a.state in (:v*:states)
   AND c.id in (:v*:olts)

-- :name compare-states :? :*
-- :doc retrieve onus match batch_id and olts
SELECT a.onu_id, a.state, a.rx_power, a.in_bps, a.out_bps, a.in_bw, a.out_bw,
       date_format(a.upd_time, '%Y-%m-%d %H:%i:%s') upd_tm,
       b.pon, b.oid, b.sn, b.name, c.name olt_name
  FROM onu_states a, onus b, olts c
 WHERE a.batch_id = :batch_id
   AND a.onu_id = b.id AND b.olt_id = c.id
   AND c.id in (:v*:olts)

-- :name state-group-cnt :? :*
-- :doc retrieve count group by state
SELECT state, count(*) cnt
  FROM onu_states
 WHERE batch_id = :batch_id
 GROUP BY state

-- :name batch-group-cnt :? :*
-- :doc retrieve count group by batch_id
SELECT b.name, count(*) cnt
  FROM onu_states a, batches b
 WHERE a.batch_id = b.id
 GROUP BY a.batch_id
 ORDER BY b.id DESC
 LIMIT 6

-- :name cnt-olt-state :? :1
-- :doc get count of records for a given olt_id and state
SELECT count(*) cnt
  FROM onu_states a, onus b
 WHERE a.onu_id = b.id
   AND a.batch_id = (SELECT max(id) FROM batches)
   AND a.state = :state
   AND b.olt_id = :olt_id

-- :name cnt-olt-oths :? :1
-- :doc get count of records for a given olt_id and other states
SELECT count(*) cnt
  FROM onu_states a, onus b
 WHERE a.onu_id = b.id
   AND a.batch_id = (SELECT max(id) FROM batches)
   AND a.state in ("AuthFail","syncMib", "deny", "Logging")
   AND b.olt_id = :olt_id
