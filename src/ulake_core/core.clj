(ns ulake-core.core
  (:gen-class)
  (:require [aleph.http :refer [start-server]]
            [clojure.string :refer [starts-with?]]
            [rethinkdb.query :as r]
            [ring.middleware.reload :refer [wrap-reload]])
  (:import (io.ipfs.api IPFS NamedStreamable$InputStreamWrapper)))

(def in-dev? (constantly true))
(def header-prefix "content-")
(def ipfs (IPFS. "/ip4/127.0.0.1/tcp/5001"))

(defn ipfs-add
  "Add the content of the given stream to IPFS and return the CID."
  [istream]
  (-> (->> istream NamedStreamable$InputStreamWrapper. (.add ipfs))
      (.get 0) .-hash str))

(defn ingest
  "Ingest the data and return appropriate response."
  [request]
  (let [cid (ipfs-add (:body request)) ; TODO: handle exceptions
        object (into {"cid" cid}
                     (for [[k v] (:headers request)
                           :when (starts-with? k header-prefix)]
                       [(subs k (count header-prefix)) v]))]
    (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
      (-> (r/table "ulake")
          (r/insert object)
          (r/run conn)))
    {:status 200
     :body (str object \space cid \newline)}))

(defn route
  "Route HTTP endpoints."
  [request]
  (case (list (:request-method request) (:uri request))
    ((:post "/ingest")) (ingest request)
    {:status 400
     :body "unsupported"}))

(defn -main [& args]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (when (some #{"ulake"} (r/run (r/table-list) conn))
      (r/run (r/table-drop "ulake") conn))
    (r/run (r/table-create "ulake") conn))
  (start-server
    (if (in-dev? args)
      (wrap-reload #'route)
      route)
    {:port 8090}))
