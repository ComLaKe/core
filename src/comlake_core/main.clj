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
  "Entry point."
  (:gen-class)
  (:require [aleph.http :refer [start-server]]
            [clojure.data.json :as json]
            [clojure.java.io :refer [reader]]
            [clojure.set :refer [subset?]]
            [clojure.string :refer [split starts-with?]]
            [comlake-core.ipfs :as ipfs]
            [comlake-core.qast :as qast]
            [rethinkdb.query :as r]
            [ring.middleware.reload :refer [wrap-reload]]))

(def header-prefix "x-comlake-")

(defn ingest
  "Ingest data from the given request and return appropriate response."
  [headers body]
  (let [kv (reduce-kv
             (fn [latest k v]
               (let [add (partial assoc latest)]
                 (case k
                   "content-type" (add "type" v)
                   "content-length" (add "length" (bigint v))
                   "x-comlake-length" latest ; disarm footgun
                   ;; Should we trim whitespaces?
                   "x-comlake-topics" (add "topics" (split v #"\s*,\s*"))
                   (if (starts-with? k header-prefix)
                       (add (subs k (count header-prefix)) v)
                       latest))))
             {} headers)]
    (if (subset? #{"length" "type" "name" "source" "topics"} kv)
      (let [cid (ipfs/add body)] ; TODO: handle exceptions
        (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
          (-> (r/table "comlake")
              (r/insert (assoc kv :cid cid))
              (r/run conn)))
        {:status 200
         :headers {:content-type "text/plain"}
         :body cid})
      {:status 400
       :headers {:content-type "text/plain"}
       :body "missing metadata fields\n"})))

(defn search
  "Return query result as a HTTP response."
  [raw-ast]
  ;; FIXME: respond with a 400 upon a malformed query
  (let [parsed (r/fn [row] (qast/parse row (json/read (reader raw-ast))))]
    {:status 200
     :headers {:content-type "application/json"}
     :body (json/write-str
             (with-open [conn (r/connect :host "127.0.0.1"
                                         :port 28015
                                         :db "test")]
               (-> (r/table "comlake")
                   (r/filter parsed)
                   (r/run conn))))}))

(defn route
  "Route HTTP endpoints."
  [request]
  (case [(:request-method request) (:uri request)]
    [:post "/add"] (ingest (:headers request) (:body request))
    [:post "/find"] (search (:body request))
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
