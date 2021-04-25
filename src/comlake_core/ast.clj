;;;; Query abstract syntax tree parser
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

(ns comlake-core.ast
  (:gen-class)
  (:require [rethinkdb.query :as r]))

(def logic-ops {"&" r/and
                "|" r/or})
(def arith-ops {"==" r/eq
                "!=" r/ne})

(defn parse
  "Parse query into ReQL."
  [row query]
  (assert (map? query))
  (assert (= (count query) 1))
  (let [[k v] (first query)
        logic-op (get logic-ops k)]
    (if logic-op
      (do (assert (vector? v))
          (apply logic-op (map (partial parse row) v)))
      (do (assert (map? v))
          (assert (= (count v) 1))
          (let [arith-op (get arith-ops k)
                [lhs rhs] (first v)]
            (if arith-op
              (arith-op (r/get-field row lhs) rhs)
              (do (assert (= k "~"))
                  (r/match (r/get-field row lhs) rhs))))))))
