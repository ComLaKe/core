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
  (:require [rethinkdb.query :as r]))

(def table "comlake")

(defn run
  "Run the given RethinkDB query."
  [query]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (r/run query conn)))

(defn clear
  "Clear the table in RethinkDB."
  [table-name]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (when (some #{table-name} (r/run (r/table-list) conn))
      (r/run (r/table-drop table-name) conn))
    (r/run (r/table-create table-name) conn)))
