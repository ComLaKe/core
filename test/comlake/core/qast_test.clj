;;;; Query AST parser tests
;;;; Copyright (C) 2014-2017  Zach Tellman
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

(ns comlake.core.qast-test
  "Query AST parser tests."
  (:require [clojure.test :refer [deftest is testing]]
            [comlake.core.qast :refer [qast->fn qast->psql]]))

(def regex-example ["~" "name@domain.com" ".*@(.*)"])
(def maths-example ["&"
                    ["==" ["-" ["+" 2 2] 1] 3]
                    ["<" 3 ["/" 8 2] ["%" ["*" 2 2 3] 7]]
                    [">=" 3000 100]
                    ["!" ["|" ["<=" 420 69] [">" 9 11] ["!=" 8 8]]]])

(deftest psql-gen
  (testing "regular expression"
    (is (= "('name@domain.com' ~ '.*@(.*)')"
           (qast->psql regex-example))))
  (testing "logical intersection"
    (is (= "((topics) && ARRAY['copypasta'])"
           (qast->psql ["&&" ["." ["$"] "topics"] ["copypasta"]]))))
  (testing "quick maths"
    (is (= (str "((((2 + 2) - 1) = 3)"
                " AND (3 < (8 / 2) < (MOD((2 * 2 * 3), 7)))"
                " AND (3000 >= 100)"
                " AND (NOT ((420 <= 69) OR (9 > 11) OR (8 <> 8))))")
           (qast->psql maths-example)))))

(deftest fn-gen
  (testing "regular expression"
    (is ((qast->fn regex-example) {})))
  (testing "quick maths"
    (is ((qast->fn maths-example) {}))))
