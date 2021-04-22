;;;; Entry point
;;;; Copyright (C) 2021  Nguyễn Gia Phong
;;;;
;;;; This file is part of comlake-core
;;;;
;;;; comlake-core is free software: you can redistribute it and/or modify
;;;; it under the terms of the GNU Affero General Public License version 3
;;;; as published by the Free Software Foundation.
;;;;
;;;; comlake-core is distributed in the hope that it will be useful,
;;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;;; GNU Affero General Public License for more details.
;;;;
;;;; You should have received a copy of the GNU Affero General Public License
;;;; along with comlake-core.  If not, see <https://www.gnu.org/licenses/>.

(ns comlake-core.main
  (:gen-class)
  (:require [aleph.http :refer [start-server]]
            [clojure.string :refer [starts-with?]]
            [comlake-core.ipfs :as ipfs]
            [rethinkdb.query :as r]
            [ring.middleware.reload :refer [wrap-reload]]))

;;; FIXME: prefix should be x-comlake-
(def header-prefix "content-")

(defn ingest
  "Ingest the data and return appropriate response."
  [request]
  (let [cid (ipfs/add (:body request)) ; TODO: handle exceptions
        object (into {"cid" cid}
                     (for [[k v] (:headers request)
                           :when (starts-with? k header-prefix)]
                       [(subs k (count header-prefix)) v]))]
    (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
      (-> (r/table "comlake")
          (r/insert object)
          (r/run conn)))
    {:status 200
     :body cid}))

(defn route
  "Route HTTP endpoints."
  [request]
  (case (list (:request-method request) (:uri request))
    ((:post "/ingest")) (ingest request)
    {:status 400
     :body "unsupported"}))

(defn -main [& args]
  ;; TODO: Abstract this away
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (when (some #{"comlake"} (r/run (r/table-list) conn))
      (r/run (r/table-drop "comlake") conn))
    (r/run (r/table-create "comlake") conn))
  (start-server
    (if (some #{"reload"} args)
      (wrap-reload #'route)
      route)
    {:port 8090}))
