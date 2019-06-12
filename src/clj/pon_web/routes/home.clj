(ns pon-web.routes.home
  (:require
    [pon-web.layout :as layout]
    [pon-web.db.core :as db]
    [clojure.java.io :as io]
    [pon-web.middleware :as middleware]
    [ring.util.http-response :as response]))

(defn home-page [request]
  (let [bat_id (:id (db/latest-done-batch))
        cnts-state (db/state-group-cnt {:batch_id bat_id})
        cnts-batch (db/batch-group-cnt)]
    (layout/render request "home.html"
                   {:states (map #(:state %) cnts-state)
                    :s-cnts (map #(:cnt %) cnts-state)
                    :batches (map #(:name %) cnts-batch)
                    :b-cnts (map #(:cnt %) cnts-batch)})))

(defn about-page [request]
  (layout/render request "about.html"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/about" {:get about-page}]])

