;;;; RethinkDB wrapper
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

(ns comlake-core.rethink
  "RethinkDB wrapper."
  (:require [rethinkdb.query :as r])
  (:import (java.util ArrayList)))

(def table-name "comlake")

(defn clear
  "Clear the table in RethinkDB."
  []
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (when (some #{table-name} (r/run (r/table-list) conn))
      (r/run (r/table-drop table-name) conn))
    (r/run (r/table-create table-name) conn)))

(defn run
  "Run given RethinkDB query."
  [query]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (r/run query conn)))

(defn insert
  "Insert given row to RethinkDB."
  [row]
  (run (r/insert (r/table table-name)
                 (assoc (reduce (fn [m [k v]] (assoc m k v)) {} row)
                        "topics" (vec (get row "topics"))))))

(defn search
  "Filter for rows matching query."
  [query]
  (run (r/filter (r/table table-name) (r/fn [row] (query row)))))

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
   "==" [(ignore-first r/eq) #(> % 1)]
   "!=" [(ignore-first r/ne) #(> % 1)]
   ">" [(ignore-first r/gt) #(> % 1)]
   ">=" [(ignore-first r/ge) #(> % 1)]
   "<" [(ignore-first r/lt) #(> % 1)]
   "<=" [(ignore-first r/le) #(> % 1)]
   "!" [(ignore-first r/not) #(= % 1)]})

(defn parse-qast
  "Parse query AST into ReQL.  Return nil in case of an invalid AST,
  otherwise a function taking a RethinkDB row and returning the ReQL."
  [ast]
  (if (or (vector? ast)
          (= (type ast) ArrayList))
    (when-let [[op pred] (get ops (first ast))]
      (when (pred (dec (count ast)))
        (let [args (map parse-qast (rest ast))]
          (when (every? some? args)
            (fn [row] (apply op row (map #(% row) args)))))))
    (fn [row] ast)))
