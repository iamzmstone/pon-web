(ns pon-web.bl.core
  (:require
   [pon-web.db.core :as db]
   [clojure.tools.logging :as log]
   [pon-web.etl.olt-c300 :as c300]))

(def Nor-S ["working" "DyingGasp" "LOS" "Offline"])
(def Oth-S ["AuthFail" "syncMib"])

(defn- cnt-state [olt]
  {:id (:id olt)
   :olt (:name olt)
   :cnt-wk (:cnt (db/cnt-olt-state {:olt_id (:id olt) :state "working"}))
   :cnt-dg (:cnt (db/cnt-olt-state {:olt_id (:id olt) :state "DyingGasp"}))
   :cnt-ls (:cnt (db/cnt-olt-state {:olt_id (:id olt) :state "LOS"}))
   :cnt-ol (:cnt (db/cnt-olt-state {:olt_id (:id olt) :state "Offline"}))
   :cnt-ot (:cnt (db/cnt-olt-oths {:olt_id (:id olt)}))})

(defn olt-cnts []
  (let [olts (db/all-olts)]
    (map cnt-state olts)))

(defn onu-conf [state-id]
  (let [onu-state (db/get-state-by-id {:id state-id})
        onu (db/get-onu-by-id {:id (:onu_id onu-state)})
        olt (db/get-olt-by-id {:id (:olt_id onu)})
        {:keys [state conf]} (c300/onu-data olt onu)]
    (do
      (log/info "state:" state-id state)
      (db/upd-state (merge state {:id state-id}))
      (map #(merge {:type %} (% conf)) (keys conf)))))
