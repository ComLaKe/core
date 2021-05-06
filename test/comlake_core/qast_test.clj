;;;; Query AST parser tests
;;;; Copyright (C) 2014-2017  Zach Tellman
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

(ns comlake-core.qast-test
  "Query AST parser tests."
  (:require [clojure.test :refer [deftest is testing]]
            [comlake-core.rethink :refer [parse-qast run]]))

(defn run-qast
  "Run given query AST."
  [ast]
  (run ((parse-qast ast) {"foo" "bar"})))

(deftest operators
  (testing "regular expression"
    (is (->> (run-qast ["~" "name@domain.com" ".*@(.*)"])
             :groups
             (apply :str)
             (= "domain.com"))))
  (testing "quick maths"
    (is (run-qast ["&"
                   ["==" ["-" ["+" 2 2] 1] 3]
                   ["<" 3 ["/" 8 2] ["%" ["*" 2 2 3] 7]]
                   ["!" ["|"
                         ["<=" 420 69]
                         [">=" 9 11]
                         [">" 9 11]
                         ["!=" 8 8]]]])))
  (testing "concatenate"
    (is (= "foobarbaz" (run-qast ["+" "foo" "bar" "baz"])))))
