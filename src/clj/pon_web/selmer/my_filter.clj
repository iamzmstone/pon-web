(ns pon-web.selmer.my-filter
  (:require [selmer.parser :as parser]
            [pon-web.config :refer [env]]
            [selmer.filters :as filters]))

(filters/add-filter! :class-state
                     (fn [x]
                       (if (nil? x)
                         ""
                         (if (= "working" x)
                           "layui-bg-green"
                           "layui-bg-red"))))

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
                         (if (<= x (env :bw-base))
                         "layui-bg-green"
                         "layui-bg-red"))))
