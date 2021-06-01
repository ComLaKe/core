;;;; Query abstract syntax tree parser
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

(ns comlake-core.db.qast
  "Query abstract syntax tree parser."
  (:require [clojure.data.json :as json]
            [clojure.string :as string]))

(def ops-psql
  "Supported query operators and their predicates for number of operands."
  {"." [#(string/replace (first %) #"^'(.*)'$" "$1") #(= % 1)]
   "~" [#(apply format "%s ~ %s" %) #(= % 2)]
   "+" [#(string/join " + " %) #(> % 0)]
   "-" [#(string/join " - " %) #(> % 0)]
   "*" [#(string/join " * " %) #(> % 0)]
   "/" [#(string/join " / " %) #(> % 0)]
   "%" [#(apply format "MOD(%s, %s)" %) #(= % 2)]
   "==" [#(string/join " = " %) #(> % 1)]
   "!=" [#(string/join " <> " %) #(> % 1)]
   ">" [#(string/join " > " %) #(> % 1)]
   ">=" [#(string/join " >= " %) #(> % 1)]
   "<" [#(string/join " < " %) #(> % 1)]
   "<=" [#(string/join " <= " %) #(> % 1)]
   "&" [#(string/join " AND " %) any?]
   "|" [#(string/join " OR " %) any?]
   "!" [#(apply format "NOT %s" %) #(= % 1)]})

(defn qast-to-psql
  "Parse query AST into PostgreSQL predicate.
  Return nil in case of an invalid AST."
  [ast]
  (cond (vector? ast) (when-let [[op pred] (get ops-psql (first ast))]
                        (when (pred (dec (count ast)))
                          (let [args (map qast-to-psql (rest ast))]
                            (when (every? some? args)
                              (format "(%s)" (op args))))))
        (string? ast) (format "'%s'" ast)
        ;; TODO: map
        :else (json/write-str ast)))

(defn json-to-psql
  "Parse JSON input stream reader into PostgreSQL predicate.
  Return nil in case of an invalid AST."
  [reader]
  (let [ast (try (json/read reader)
                 (catch Exception e nil))]
    (when ast (qast-to-psql ast))))
