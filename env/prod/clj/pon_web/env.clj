(ns pon-web.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[pon-web started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[pon-web has shut down successfully]=-"))
   :middleware identity})
