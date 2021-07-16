;;;; Metadata extractors
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

(ns comlake.core.extract.metadata
  "Metadata extractor."
  (:require [clojure.data.csv :refer [read-csv]]
            [clojure.data.json :as json]
            [clojure.java.io :refer [reader]]
            [clojure.string :refer [blank?]]
            [json-schema.infer :as json-schema]))

(def re-number
  "JSON number regular expression."
  #"-?(?:0|[1-9]\d*)(?:\.\d+)?(?:[eE][+-]?\d+)?")

(defn infer-json
  "Infer JSON schema of given content."
  [cid fs]
  (->> cid (.fetch fs) reader json/read
       (json-schema/infer->json {:title cid})))

(defn infer-csv
  "Infer schema of given CSV content."
  [cid fs]
  (let [file (->> cid (.fetch fs) reader read-csv)
        names (first file)
        types (map #(-> {"type" %})
                   (reduce (partial map #(cond ; only consider number and string
                                           (or (= %1 "string") (blank? %2)) %1
                                           (re-matches re-number %2) "number"
                                           :else "string"))
                           (repeat (count names) "number")
                           (rest file)))]
    (json/write-str
      {"$schema" "http://json-schema.org/draft-07/schema#"
       "title" cid
       "type" "array"
       "items" {"type" "object"
                "properties" (zipmap names types)}})))

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
  "Construct an extractor updating given database."
  [fs db]
  ;; TODO: invalidate cache
  (memoize (fn [cid mime]
             (case mime
               ("application/json" "text/csv") (schema cid mime fs db)
               ;; TODO: multimedia metadata
               nil))))
