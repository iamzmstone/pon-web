(ns pon-web.selmer.my-filter
  (:require [selmer.parser :as parser]
            [pon-web.config :refer [env]]
            [selmer.filters :as filters]))

(filters/add-filter! :class-state
                     (fn [x]
                       (if (nil? x)
                         ""
                         (cond
                           (= "working" x) "layui-bg-green"
                           (= "OffLine" x) "layui-bg-orange"
                           :else "layui-bg-red"))))

(filters/add-filter! :class-rx
                     (fn [x]
                       (if (nil? x)
                         ""
                         (if (> x (env :rx-power-base))
                         "layui-bg-green"
                         "layui-bg-red"))))

(filters/add-filter! :class-bw
                     (fn [x]
                       (if (nil? x)
                         ""
                         (cond
                           (<= x (env :bw-low)) "layui-bg-green"
                           (<= x (env :bw-high)) "layui-bg-orange"
                           :else "layui-bg-red"))))
