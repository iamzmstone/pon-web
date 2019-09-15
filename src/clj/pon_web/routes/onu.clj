(ns pon-web.routes.onu
  (:require
    [pon-web.layout :as layout]
    [pon-web.db.core :as db]
    [pon-web.bl.core :as bl]
    [clojure.java.io :as io]
    [dk.ative.docjure.spreadsheet :as spreadsheet]
    [pon-web.middleware :as middleware]
    [pon-web.config :refer [env]]
    [ring.util.http-response :as response]))

(def search-header
  [[:olt_name :bat_name :pon :oid :model :type :auth :sn
    :state :rx_power :in_bps :out_bps :in_bw :out_bw :upd_tm]
   ["OLT" "采集批次" "Pon口" "OnuId" "model" "type" "auth" "SN" "状态" "收光"
    "in_Bps" "out_Bps" "入流量占比" "出流量占比" "采集时间"]])

(def diff-header
  [[:olt_name :pon :oid :sn :state :rx_power :in_bps :out_bps :in_bw :out_bw :upd_tm]
   ["OLT" "Pon口" "OnuId" "SN" "状态" "收光" "in_Bps" "out_Bps"
    "入流量占比" "出流量占比" "采集时间"]])

(defn olt-page [request]
  (layout/render request "olt_list.html"
                 {:olts (db/all-olts)}))

(defn card-page [request]
  (let [olt_id (get-in request [:path-params :id])
        olt (db/get-olt-by-id {:id olt_id})
        cards (db/olt-cards {:olt_id olt_id})]
    (layout/render request "card_list.html" {:cards cards
                                             :olt olt})))
                 
(defn onu-page [request]
  (layout/render request "onu_list.html"))

(defn search-page [request]
  (layout/render request "search.html" {:olts (db/all-olts)}))

(defn compare-page [request]
  (layout/render request "compare.html"
                 {:olts (db/all-olts) :batches (db/done-batches)}))

(defn- values [list]
  (take-nth 2 (rest list)))

(defn- paging [request]
  [(if-let [p (:page (:params request))] (read-string p) 0)
   (if-let [l (:limit (:params request))] (read-string l) 10)])

(defn- search-conds [{:keys [params]}]
  (let [conds (assoc (merge {:states {:1 "NIL"} :olts {:1 "NIL"}} params)
                     :sn (str (:sn params) "%"))]
    (assoc conds :states (vals (:states conds)) :olts (vals (:olts conds)))))

(defn- compare-to [s1 s2]
  (if (or (nil? s1) (nil? s2)
          (or (not (= (:state s1) (:state s2)))
              (> (Math/abs (int (- (:rx_power s1) (:rx_power s2))))
                 (or (get-in env [:diff :rx]) 2))
              (> (Math/abs (- (:in_bw s1) (:in_bw s2)))
                 (or (get-in env [:diff :bw]) 50))))
    {:s1 s1 :s2 s2}))

(defn- compare-onu [s bat_id]
  (compare-to s (db/get-state {:batch_id bat_id :onu_id (:onu_id s)})))

(defn comp-rst [bat1 bat2 olts]
  (remove nil? (map #(compare-onu % bat2)
                    (db/compare-states {:batch_id bat1 :olts olts}))))

(defn do-compare [request]
  (-> (response/found "diff-result")
      (assoc :session
             (assoc (:session request)
                    :comps {:bat1 (get-in request [:params :bat1])
                            :bat2 (get-in request [:params :bat2])
                            :olts (vals (get-in request [:params :olts]))}))))

(defn comp-rst-page [request]
  (layout/render request "compare_result.html"
                 {:diff-list (comp-rst (get-in request [:session :comps :bat1])
                                       (get-in request [:session :comps :bat2])
                                       (get-in request [:session :comps :olts]))}))

(defn olt-state [request]
  (let [olt-id (get-in request [:params :olt-id])
        states (clojure.string/split (get-in request [:params :states]) #",")
        bat_id (:id (db/latest-done-batch))
        conds {:batch_id bat_id :sn "%" :rx_min -100 :rx_max 0 :inbps 0
               :outbps 0 :inbw 0 :states states :olts [olt-id] :s 0 :l 10}]
    (-> (response/found "/search-list")
        (assoc :session
               (assoc (:session request) :conds conds)))))

(defn do-search [request]
;  (response/ok {:body (:params request)}))
  (-> (response/found "/search-list")
      (assoc :session
             (assoc (:session request) :conds (search-conds request)))))

(defn search-list [request]
  (layout/render request "search_list.html"))

(defn search [request]
  (let [[page limit] (paging request)
        bat_id (:id (db/latest-done-batch))
        conds (get-in request [:session :conds])
        onus (db/search-states
              (merge {:batch_id bat_id :s (* (dec page) limit) :l limit}
                     conds))]
    {:body
     {:code 0 :msg "SEO" :count (:cnt (db/search-states-cnt
                                       (merge {:batch_id bat_id} conds)))
      :data onus}}))


(defn- dump-to [data excel-file]
  (let [wb (spreadsheet/create-workbook "onus" data)
        sheet (spreadsheet/select-sheet "onus" wb)
        header-row (first (spreadsheet/row-seq sheet))]
    (spreadsheet/set-row-style!
     header-row (spreadsheet/create-cell-style! wb {:background :yellow
                                                    :font {:bold true}}))
    (spreadsheet/save-workbook! excel-file wb)))

(defn- vals-in-order [m v]
  (reduce #(conj %1 (get m %2)) [] v))

(defn dump-search-rst [request]
  (let [bat_id (:id (db/latest-done-batch))
        conds (get-in request [:session :conds])
        onus (db/search-states
              (merge {:batch_id bat_id :s 0 :l 10000000} conds))
        data (conj
              (map #(vals-in-order % (first search-header)) onus)
              (last search-header))]
    (dump-to data "/tmp/search-result.xls")
    (response/file-response "/tmp/search-result.xls")))

(defn dump-onus [request]
  (let [bat_id (:id (db/latest-done-batch))
        onus (db/batch-states {:batch_id bat_id :s 0 :l 10000000})
        data (conj
              (map #(vals-in-order % (first search-header)) onus)
              (last search-header))]
    (dump-to data "/tmp/onus.xls")
    (response/file-response "/tmp/onus.xls")))

(defn- to-vec [comp-item]
  [(vals-in-order (:s1 comp-item) (first diff-header))
   (vals-in-order (merge (select-keys (:s1 comp-item)
                                      (subvec (first diff-header) 0 4))
                         (:s2 comp-item))
                  (first diff-header))])

(defn dump-diff [request]
  (let [diffs (comp-rst (get-in request [:session :comps :bat1])
                       (get-in request [:session :comps :bat2])
                       (get-in request [:session :comps :olts]))
        data (cons
              (last diff-header)
              (reduce #(conj %1 (first %2) (last %2)) (map to-vec diffs)))]
    (dump-to data "/tmp/diff.xls")
    (response/file-response "/tmp/diff.xls")))

(defn- timestamp [k v]
  (if (= k :upd_time)
    (.toString v)
    v))

(defn onu-list [request]
  (let [[page limit] (paging request)
        bat_id (:id (db/latest-done-batch))
        onus (db/batch-states
              {:batch_id bat_id
               :s (* (dec page) limit) :l limit})]
    {:body
     {:code 0 :msg "HiHiHi" :count (:cnt (db/batch-states-cnt {:batch_id bat_id}))
      :data onus}}))

(defn onu-states [request]
  (let [states (db/onu-states {:onu_id (get-in request [:path-params :id])})]
    (layout/render request "onu_state.html" {:states states})))

(defn onu-conf [request]
  (let [conf (bl/onu-conf (get-in request [:path-params :id]))]
    (layout/render request "onu_conf.html" {:confs conf})))
              
(defn onu-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/olts" {:get olt-page}]
   ["/cards/:id" {:get card-page}]
   ["/onus" {:get onu-page}]
   ["/onu-list" {:get onu-list}]
   ["/search" {:get search}]
   ["/onu-search" {:get search-page}]
   ["/do-search" {:post do-search}]
   ["/search-list" {:get search-list}]
   ["/onu-states/:id" {:get onu-states}]
   ["/onu-conf/:id" {:get onu-conf}]
   ["/dump-search.xlsx" {:get dump-search-rst}]
   ["/dump-onus.xlsx" {:get dump-onus}]
   ["/dump-diff.xlsx" {:get dump-diff}]
   ["/olt-state" {:get olt-state}]
   ["/onu-compare" {:get compare-page}]
   ["/do-compare" {:post do-compare}]
   ["/diff-result" {:get comp-rst-page}]])
