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
            [clojure.data.json :as json]
            [clojure.java.io :refer [reader]]
            [clojure.string :refer [starts-with?]]
            [comlake-core.ipfs :as ipfs]
            [rethinkdb.query :as r]
            [ring.middleware.reload :refer [wrap-reload]]))

;;; FIXME: prefix should be x-comlake-
(def header-prefix "content-")

(defn ingest
  "Ingest the data and return appropriate response."
  [request]
  ;; FIXME: header should be checked before streaming file to IPFS
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
     :headers {:content-type "text/plain"}
     :body cid}))

(defn parse
  "Parse query into ReQL."
  [row query]
  (assert (map? query))
  (assert (= (count query) 1))
  (let [[k v] (first query)]
    (case k ; FIXME: fallback properly
      ("&" "|") (do (assert (vector? v))
                    (apply (get {"&" r/and "|" r/or} k)
                           (map (partial parse row) v)))
      ("=") (do (assert (map? v))
                (assert (= (count query) 1))
                (let [op (get {"=" r/eq} k)
                      [lhs rhs] (first v)]
                  (op (r/get-field row lhs) rhs))))))

(defn search
  "Return query result as a HTTP response."
  [request]
  (try {:status 200
        :headers {:content-type "application/json"}
        :body (json/write-str
                (with-open [conn (r/connect :host "127.0.0.1"
                                            :port 28015
                                            :db "test")]
                  (-> (r/table "comlake")
                      (r/filter (r/fn [row]
                                  (parse row
                                         (json/read (reader (:body request))))))
                      (r/run conn))))}
       (catch AssertionError e {:status 400
                                :body "malformed query"})))

(defn route
  "Route HTTP endpoints."
  [request]
  (case [(:request-method request) (:uri request)]
    [:post "/ingest"] (ingest request)
    [:post "/filter"] (search request)
    {:status 400
     :headers {:content-type "text/plain"}
     :body "unsupported\n"}))

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
