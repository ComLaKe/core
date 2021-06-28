;;;; Metadata extractor factory
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

(ns comlake.core.worker.factory
  "Metadata extractor factory."
  (:require [clojure.data.json :as json]
            [clojure.java.io :refer [reader]]
            [clojure.string :refer [blank?]]
            [json-schema.infer :as json-schema]))

(defn infer-json
  "Infer JSON schema of given content."
  [cid fs]
  (->> cid (.fetch fs) reader json/read
       (json-schema/infer->json {:title cid})))

(defn infer-csv
  "Infer schema of given CSV content."
  [cid fs]) ; TODO

(defn schema
  "Return future to schema of given content."
  [cid mime fs db]
  (future
    (let [saved (.getSchema db cid)]
      (if (blank? saved)
        (let [result (case mime
                       "application/json" (infer-json cid fs)
                       "text/csv" (infer-csv cid fs))]
          (.setSchema db cid result) ; run this async?
          result)
        saved))))

(defn metadata-extractor
  [fs db]
  "Construct an extractor updating given database."
  ;; TODO: invalidate cache
  (memoize (fn [cid mime]
             (case mime
               ("application/json" "text/csv") (schema cid mime fs db)
               ;; TODO: multimedia metadata
               nil))))
