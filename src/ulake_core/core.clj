(ns ulake-core.core
  (:gen-class)
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.handler :refer [site]]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.reload :refer [wrap-reload]])
  (:import (com.rethinkdb RethinkDB)))

(def in-dev? (constantly true))

(defroutes all-routes
  (POST "/ingest" [] #(.read (:body %))))

(defn -main [& args]
  (let [handler (if (in-dev? args)
                  (wrap-reload (site #'all-routes))
                  (site all-routes))]
    (run-server handler {:port 8090
                         :max-bytes 4294967295})))
