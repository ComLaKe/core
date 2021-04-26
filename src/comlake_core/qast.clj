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

(ns comlake-core.qast
  "Query abstract syntax tree parser."
  (:require [rethinkdb.query :as r]))

(defn ignore-first
  "Construct a function ignoring the first argument."
  [func]
  (fn [_ & args] (apply func args)))

(def ops
  "Supported query operators and their predicates for number of operands."
  {"." [r/get-field #(= % 1)]
   "~" [(ignore-first r/match) #(= % 2)]
   "+" [(ignore-first r/add) #(> % 0)]
   "-" [(ignore-first r/sub) #(> % 0)]
   "*" [(ignore-first r/mul) #(> % 0)]
   "/" [(ignore-first r/div) #(> % 0)]
   "%" [(ignore-first r/mod) #(= % 2)]
   "&" [(ignore-first r/and) any?]
   "|" [(ignore-first r/or) any?]
   "==" [(ignore-first r/eq) any?]
   "!=" [(ignore-first r/ne) #(> % 1)]
   ">" [(ignore-first r/gt) #(> % 1)]
   ">=" [(ignore-first r/ge) #(> % 1)]
   "<" [(ignore-first r/lt) #(> % 1)]
   "<=" [(ignore-first r/le) #(> % 1)]
   "!" [(ignore-first r/not) #(= % 1)]})

(defn parse
  "Parse query AST into ReQL.  Return nil in case of an invalid AST,
  otherwise a function taking a RethinkDB row and returning the ReQL."
  [ast]
  (if (vector? ast)
    (when-let [[op pred] (get ops (first ast))]
      (when (pred (dec (count ast)))
        (let [args (map parse (rest ast))]
          (when (every? some? args)
            (fn [row] (apply op row (map #(% row) args)))))))
    (fn [row] ast)))
