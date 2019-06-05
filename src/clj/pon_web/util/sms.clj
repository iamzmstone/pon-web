(ns pon-web.util.sms
  (:require [clojure.java.jdbc :as jdbc]
            [pon-web.config :refer [env]]))

(def db {:subprotocol "jtds:sqlserver"
         :subname (str "//" (get-in env [:sms :ip]) ":"
                       (get-in env [:sms :port]) "/"
                       (get-in env [:sms :db]))
         :user (get-in env [:sms :user])
         :password (get-in env [:sms :pass])})

(defn sendsms [phone content]
  (jdbc/insert! db :SMSend_khzc_xj
                {:DestTermID phone :MsgContent content}))
