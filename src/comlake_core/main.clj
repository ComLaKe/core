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
            [clojure.string :refer [starts-with?]]
            [comlake-core.rethink :as db]
            [taoensso.timbre :refer [debug]])
  (:import (comlake_core Handler)))

(defn route
  "Route HTTP endpoints."
  [request]
  (let [method (:request-method request)
        uri (:uri request)]
    (cond
      (and (= method :post) (= uri "/add")) (Handler/add (:headers request)
                                                         (:body request))
      (and (= method :post) (= uri "/find")) (Handler/find (:body request))
      (and (= method :get)
           (starts-with? uri "/get/")) (Handler/get (subs uri 5))
      :else (Handler/error "unsupported" 404))))

(defn handler
  "Handle HTTP request."
  [request]
  ;; java.util.Map.of does not produce clojure map.
  (let [response (reduce (fn [m [k v]] (assoc m k v)) {} (route request))]
    (debug request "=>" response)
    response))

(defn -main
  "Start the HTTP server."
  ([] (-main "8090"))
  ([port & args]
   (db/clear)
   (start-server handler {:port (Integer/parseInt port)})))
