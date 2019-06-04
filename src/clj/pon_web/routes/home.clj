(ns pon-web.routes.home
  (:require
    [pon-web.layout :as layout]
    [pon-web.db.core :as db]
    [clojure.java.io :as io]
    [pon-web.middleware :as middleware]
    [ring.util.http-response :as response]))

(defn home-page [request]
  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page [request]
  (layout/render request "about.html"))

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
              (> (Math/abs (int (- (:rx_power s1) (:rx_power s2)))) 2)
              (> (Math/abs (- (:in_bw s1) (:in_bw s2))) 50)))
    {:s1 s1 :s2 s2}))

(defn- compare-onu [s bat_id]
  (compare-to s (db/get-state {:batch_id bat_id :onu_id (:onu_id s)})))

(defn comp-rst [bat1 bat2 olts]
  (remove nil? (map #(compare-onu % bat2)
                    (db/compare-states {:batch_id bat1 :olts olts}))))

(defn comp-rst-page [request]
  (layout/render request "compare_result.html"
                 {:diff-list (comp-rst (get-in request [:params :bat1])
                                       (get-in request [:params :bat2])
                                       (vals (get-in request [:params :olts])))}))

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

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get onu-page}]
   ["/onus" {:get onu-page}]
   ["/onu-list" {:get onu-list}]
   ["/search" {:get search}]
   ["/onu-search" {:get search-page}]
   ["/do-search" {:post do-search}]
   ["/search-list" {:get search-list}]
   ["/onu-states/:id" {:get onu-states}]
   ["/onu-compare" {:get compare-page}]
   ["/diff-result" {:post comp-rst-page}]
   ["/about" {:get about-page}]])

