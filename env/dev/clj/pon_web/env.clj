(ns pon-web.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [pon-web.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[pon-web started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[pon-web has shut down successfully]=-"))
   :middleware wrap-dev})
