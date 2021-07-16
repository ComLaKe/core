;;;; Data extractors
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

(ns comlake.core.extract.data
  "Data extractors."
  (:require [clojure.data.csv :refer [read-csv]]
            [clojure.data.json :as json]))

(defn csv->json
  "Convert tabular data to key-value."
  [csv]
  (let [names (first csv)]
    (map #(zipmap names %) (rest csv))))

(defn extract-data
  "Extract (semi-)structured data matching given predicate."
  [predicate mime reader]
  (when-let [result (case mime
                      "application/json" (filter predicate (json/read reader))
                      "text/csv" (filter predicate
                                         (csv->json (read-csv reader)))
                      nil)]
    (json/write-str result)))
