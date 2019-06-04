(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
    [pon-web.config :refer [env]]
    [clojure.spec.alpha :as s]
    [expound.alpha :as expound]
    [mount.core :as mount]
    [pon-web.core :refer [start-app]]
    [pon-web.db.core]
    [conman.core :as conman]
    [luminus-migrations.core :as migrations]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start 
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'pon-web.core/repl-server))

(defn stop 
  "Stops application."
  []
  (mount/stop-except #'pon-web.core/repl-server))

(defn restart 
  "Restarts application."
  []
  (stop)
  (start))

(defn restart-db 
  "Restarts database."
  []
  (mount/stop #'pon-web.db.core/*db*)
  (mount/start #'pon-web.db.core/*db*)
  (binding [*ns* 'pon-web.db.core]
    (conman/bind-connection pon-web.db.core/*db* "sql/queries.sql")))

(defn reset-db 
  "Resets database."
  []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate 
  "Migrates database up for all outstanding migrations."
  []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback 
  "Rollback latest database migration."
  []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration 
  "Create a new up and down migration file with a generated timestamp and `name`."
  [name]
  (migrations/create name (select-keys env [:database-url])))


