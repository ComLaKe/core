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

(ns comlake.core.worker.filter
  "Data extractors."
  (:require [clojure.data.csv :refer [read-csv]]
            [clojure.data.json :as json]
            [clojure.java.io :refer [reader]]
            [clojure.string :refer [blank?]]
            [json-schema.infer :as json-schema]))

(defn extract-data
  [predicate mime reader]
  (when-let [result (case mime
                      "application/json" (filter predicate (json/read reader)))]
    (json/write-str result)))
