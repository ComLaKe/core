;;;; IPFS wrapper
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

(ns comlake-core.ipfs
  (:gen-class)
  (:import (io.ipfs.api IPFS NamedStreamable$InputStreamWrapper)))

(def ipfs (IPFS. "/ip4/127.0.0.1/tcp/5001"))

(defn add
  "Add the content of the given stream to IPFS and return the CID."
  [istream]
  (-> (->> istream NamedStreamable$InputStreamWrapper. (.add ipfs))
      (.get 0) .-hash str))
