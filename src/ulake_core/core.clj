(ns ulake-core.core
  (:gen-class)
  (:require [aleph.http :refer [start-server]]
            [ring.middleware.reload :refer [wrap-reload]])
  (:import (com.rethinkdb RethinkDB)
           (io.ipfs.api IPFS NamedStreamable$InputStreamWrapper)))

(def in-dev? (constantly true))
(def ipfs (IPFS. "/ip4/127.0.0.1/tcp/5001"))

(defn ipfs-add
  "Add the content of the given stream to IPFS and return the CID."
  [istream]
  (-> (->> istream NamedStreamable$InputStreamWrapper. (.add ipfs))
      (.get 0) .-hash str))

(defn route [request]
  (case (list (:request-method request) (:uri request))
    ((:post "/echo")) {:status 200
                     :body (ipfs-add (:body request))}
    {:status 400
     :body "unsupported"}))

(defn -main [& args]
  (start-server
    (if (in-dev? args)
      (wrap-reload #'route)
      route)
    {:port 8090}))
