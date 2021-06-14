;;;; Entry point
;;;; Copyright (C) 2021  Nguyễn Gia Phong
;;;;
;;;; This file is part of comlake.core.
;;;;
;;;; comlake.core is free software: you can redistribute it and/or modify
;;;; it under the terms of the GNU Affero General Public License version 3
;;;; as published by the Free Software Foundation.
;;;;
;;;; comlake.core is distributed in the hope that it will be useful,
;;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;;; GNU Affero General Public License for more details.
;;;;
;;;; You should have received a copy of the GNU Affero General Public License
;;;; along with comlake.core.  If not, see <https://www.gnu.org/licenses/>.

(ns comlake.core.main
  "Entry point."
  (:gen-class)
  (:require [aleph.http :refer [start-server]]
            [clojure.string :refer [starts-with?]]
            [taoensso.timbre :refer [debug]])
  (:import (comlake.core Configuration
                         HttpHandler)))

(defn route
  "Route HTTP endpoints."
  [request handler]
  (let [method (:request-method request)
        uri (:uri request)]
    (cond
      (and (= method :post) (= uri "/mkdir")) (.mkdir handler)
      (and (= method :post) (= uri "/save")) (.save handler (:body request))
      (and (= method :post) (= uri "/add")) (.add handler (:headers request)
                                                          (:body request))
      (and (= method :post) (= uri "/find")) (.find handler (:body request))
      (and (= method :get)
           (starts-with? uri "/ls/")) (.ls handler (subs uri 4))
      (and (= method :get)
           (starts-with? uri "/get/")) (.get handler (subs uri 5))
      :else (HttpHandler/error "unsupported" 404))))

(defn make-handler
  "Construct a Ring request handler."
  [cfg]
  (let [handler (HttpHandler. cfg)]
    (fn [request]
      ;; java.util.Map.of does not produce clojure map.
      (let [response (reduce (fn [m [k v]] (assoc m k v)) {}
                             (route request handler))]
        (debug request "=>" response)
        response))))

(defn -main
  "Start the HTTP server."
  ([] (-main "8090"))
  ([port & args]
   (let [cfg (Configuration.)]
    (start-server (make-handler cfg) {:port (Integer/parseInt port)}))))
