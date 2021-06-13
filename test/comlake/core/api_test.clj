;;;; HTTP API functional tests
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

(ns comlake.core.api-test
  "HTTP API functional tests."
  (:require [aleph.http :as http]
            [aleph.netty :refer [wait-for-close]]
            [clojure.data.json :as json]
            [clojure.test :refer [deftest is testing]]
            [clojure.java.io :refer [file input-stream reader resource]]
            [comlake.core.main :refer [-main]]))

(def port 42069)
(def interjection (resource "test/Interjection"))
(def interjection-cid "QmbwXK2Wg6npoAusr9MkSduuAViS6dxEQBNzqoixanVtj5")
(def apollo
  {"albums" "QmUh6QSTxDKX5qoNU1GoogbhTveQQV9JMeQjfFVchAtd5Q"
   "README.txt" "QmP8jTG1m9GSDJLCbeWhVSVgEzCPPwXRdCRuJtQ5Tz9Kc9"
   "build_frontend_index.py" "QmRSxRRu9AoJ23bxb2pFeoAUFXMAdki7RZu2T7e6zHRdu6"
   "_Metadata.json" "QmWXShtJXt6Mw3FH7hVCQvR56xPcaEtSj4YFSGjp2QxA4v"
   "apolloarchivr.py" "QmU7gJi6Bz3jrvbuVfB7zzXStLJrTHf6vWh8ZqkCsTGoRC"
   "frontend" "QmeQtZfwuq6aWRarY9P3L9MWhZ6QTonDe9ahWECGBZjyEJ"})
(def apollo-cid "QmSnuWmxptJZdLJpKRarxBMS2Ju2oANVrgbr2xWbie9b2D")
(def json-body (comp json/read reader :body))

(defn make-url
  "Make URL for given endpoint."
  [endpoint]
  (str "http://localhost:" port endpoint))

(defmacro with-server
  "Manage a test server for the code in body."
  [& body]
  `(let [server# (-main (str port))]
     ;; TODO: add mock data
     (try ~@body
          (finally (.close server#)
                   (wait-for-close server#)))))

(defn http-get
  "Call aleph.http/get with :throw-exceptions? disabled."
  ([url options] (http/get url (assoc options :throw-exceptions? false)))
  ([url] (http-get url {})))

(defn http-post
  "Call aleph.http/get with :throw-exceptions? disabled."
  ([url options] (http/post url (assoc options :throw-exceptions? false)))
  ([url] (http-post url {})))

(deftest post-mkdir
  (with-server
    (let [response @(http/post (make-url "/mkdir"))]
      (is (and (= 200 (:status response))
               (= "QmUNLLsPACCz1vLxQVkXqqLX5R1X345qqfHbsf67hvA3Nn"
                  (get (json-body response) "cid")))))))

(deftest post-add
  (let [url (make-url "/add")
        headers {:accept "application/json"
                 :content-length (.length (file interjection))
                 :content-type "text/plain"
                 :x-comlake-name "Interjection"
                 :x-comlake-source "https://wiki.installgentoo.com"
                 :x-comlake-topics ["Natural language" "copypasta"]
                 :x-comlake-language "English"}]
    (with-server
      (testing "success"
        (with-open [stream (input-stream interjection)]
          (let [response @(http/post url {:headers headers :body stream})]
            (is (and (= 200 (:status response))
                     (= "QmbwXK2Wg6npoAusr9MkSduuAViS6dxEQBNzqoixanVtj5"
                        (get (json-body response) "cid")))))))
      (testing "empty data"
        (let [response @(http-post url {:headers (assoc headers
                                                        :content-length 0)})]
          (is (and (= 400 (:status response))
                   (= "empty data" (get (json-body response) "error"))))))
      (testing "missing headers"
        (with-open [stream (input-stream interjection)]
          (let [options {:headers (dissoc headers :x-comlake-source)
                         :body stream}
                response @(http-post url options)]
            (is (and (= 400 (:status response))
                     (= {"missing-metadata" ["source"]}
                        (get (json-body response) "error"))))))))))

(deftest post-find
  (let [url (make-url "/find")
        options {:accept :json
                 :content-type :json
                 :body (json/write-str ["<" ["." "length"] 0])}]
    (with-server
      (testing "success"
        (let [response @(http-post url options)]
          (is (and (= 200 (:status response))
                   ;; Obviously length cannot be negative.
                   (empty? (json-body response))))))
      (testing "malformed query"
        (let [override {:body (json/write-str ["8=D" "foo" "bar"])}
              response @(http-post url (merge options override))]
          (is (and (= 400 (:status response))
                   (= "malformed query"
                      (get (json-body response) "error")))))))))

(deftest get-ls
  (with-server
    (testing "success"
      (let [response @(http-get (make-url (str "/ls/" apollo-cid)))]
        (is (and (= 200 (:status response))
                 (= apollo (json-body response))))))
    (testing "not directory"
      (let [response @(http-get (make-url (str "/ls/" interjection-cid)))]
        (is (and (= 400 (:status response))
                 (= "not a directory" (get (json-body response) "error"))))))
    (testing "not CID"
      (let [response @(http-get (make-url "/ls/this-cid-does-not-exist"))]
        (is (and (= 400 (:status response))
                 (= "not a directory" (get (json-body response) "error"))))))))

(deftest get-get
  (with-server
    (testing "success"
      (let [response @(http-get (make-url (str "/get/" interjection-cid)))]
        (is (and (= 200 (:status response))
                 (= (slurp (:body response))
                    (slurp interjection))))))
    (testing "not found"
      (let [response @(http-get (make-url "/get/this-cid-does-not-exist"))]
        (is (and (= 404 (:status response))
                 (= "content not found"
                    (get (json-body response) "error"))))))))

(deftest not-found
  (with-server
    (let [response @(http-get (make-url "/this/endpoint/is/unsupported"))]
      (is (and (= 404 (:status response))
               (= "unsupported" (get (json-body response) "error")))))))
