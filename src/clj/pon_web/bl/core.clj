(ns pon-web.bl.core
  (:require
   [pon-web.db.core :as db]))

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
