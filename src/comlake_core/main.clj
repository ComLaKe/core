;;;; Entry point
;;;; Copyright (C) 2021  Nguyễn Gia Phong
;;;;
;;;; This file is part of comlake-core.
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
            [clojure.set :refer [difference]]
            [clojure.string :refer [join split starts-with?]]
            [comlake-core.ipfs :as fs]
            [comlake-core.rethink :as db]
            [ring.middleware.reload :refer [wrap-reload]]
            [taoensso.timbre :refer [debug]]))

(defn error-response
  "Wrap given error string in a Ring JSON response."
  ([error status] {:status status
                   :headers {:content-type "application/json"}
                   :body (json/write-str {"error" error})})
  ([error] (error-response error 400)))

(def header-prefix "x-comlake-")

(defn ingest
  "Ingest data from the given request and return appropriate response."
  [headers body]
  (let [kv (reduce-kv
             (fn [latest k v]
               (let [add (partial assoc latest)]
                 (case k
                   "content-type" (add "type" v)
                   "x-comlake-type" latest ; disarm footgun
                   "content-length" (add "length" (bigint v))
                   "x-comlake-length" latest ; disarm footgun
                   ;; Should we trim whitespaces?
                   "x-comlake-topics" (add "topics" (split v #"\s*,\s*"))
                   (if (starts-with? k header-prefix)
                       (add (subs k (count header-prefix)) v)
                       latest))))
             {} headers)
        missing (difference #{"length" "type" "name" "source" "topics"}
                            (set (keys kv)))]
    (if (empty? missing)
      (let [cid (fs/add body)] ; TODO: handle exceptions and size mismatch
        (db/insert (assoc kv "cid" cid))
        {:status 200
         :headers {:content-type "application/json"}
         :body (json/write-str {"cid" cid})})
      (error-response {:missing-metadata missing}))))

(defn search
  "Return query result as a HTTP response."
  [raw-ast]
  (if-let [query (db/parse-qast (json/read (reader raw-ast)))]
    {:status 200
     :headers {:content-type "application/json"}
     :body (json/write-str (db/search query))}
    (error-response "malformed query")))

(defn forward
  "Forward content from underlying distributed filesystem as a HTTP response."
  [cid]
  (if-let [body (fs/fetch cid)]
    {:status 200
     :headers {:content-type "application/octet-stream"}
     :body body}
    (error-response "content not found" 404)))

(defn route
  "Route HTTP endpoints."
  [request]
  (let [method (:request-method request)
        uri (:uri request)]
    (cond
      (and (= method :post) (= uri "/add")) (ingest (:headers request)
                                                    (:body request))
      (and (= method :post) (= uri "/find")) (search (:body request))
      (and (= method :get) (starts-with? uri "/get/")) (forward (subs uri 5))
      :else (error-response "unsupported" 404))))

(defn handler
  "Handle HTTP request."
  [request]
  (let [response (route request)]
    (debug request "=>" response)
    response))

(defn -main [& args]
  "Start the HTTP server."
  (db/clear)
  (start-server
    (if (some #{"reload"} args)
      (wrap-reload #'handler)
      handler)
    {:port 8090}))
