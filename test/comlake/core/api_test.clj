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
(def init-dir
  {"help" "QmY5heUM5qgRubMDD1og9fhCPA6QdkMp3QCwd4s7gJsyE7"
   "contact" "QmYCvbfNbCwFR45HiNP45rwJgvatpiW38D961L5qAhUM5Y"
   "about" "QmZTR5bcpQD7cFgTorqxZDYaew1Wqgfbd2ud9QqGPAkK2V"
   "quick-start" "QmdncfsVm2h5Kqq9hPmU7oAVX2zTSVP3L869tgTbPYnsha"
   "readme" "QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB"
   "security-notes" "QmTumTjvcYCAvRRwQ8sDRxh8ezmrcr88YFU7iYNroGGTBZ"})
(def init-dir-cid "QmYwAPJzv5CZsnA625s3Xf2nemtYgPpHdWEz79ojWnPbdG")
(def interjection (resource "test/Interjection"))
(def interjection-cid "QmbwXK2Wg6npoAusr9MkSduuAViS6dxEQBNzqoixanVtj5")
(def combined-dir-cid "QmPao7zTNvuqH2pAVUgquYXgEhqoiTBpdjU7AwgZvsta9r")
(def empty-dir-cid "QmUNLLsPACCz1vLxQVkXqqLX5R1X345qqfHbsf67hvA3Nn")
(def json-body (comp json/read reader :body))

(defn make-url
  "Make URL for given endpoint."
  [endpoint]
  (str "http://localhost:" port endpoint))

(defn http-get
  "Call aleph.http/get with :throw-exceptions? disabled."
  ([url options] (http/get url (assoc options :throw-exceptions? false)))
  ([url] (http-get url {})))

(defn http-post
  "Call aleph.http/get with :throw-exceptions? disabled."
  ([url options] (http/post url (assoc options :throw-exceptions? false)))
  ([url] (http-post url {})))

(deftest post-dir
  (let [response @(http-post (make-url "/dir"))]
    (is (and (= 200 (:status response))
             (= empty-dir-cid (get (json-body response) "cid"))))))

(deftest post-file
  (let [url (make-url "/file")
        headers {:content-length (.length (file interjection))
                 :content-type "text/plain"}]
    (testing "success"
      (with-open [stream (input-stream interjection)]
        (let [response @(http-post url {:headers headers :body stream})]
          (is (and (= 200 (:status response))
                   (= interjection-cid (get (json-body response) "cid")))))))
    (testing "empty data"
      (let [response @(http-post url {:headers (assoc headers
                                                      :content-length 0)})]
        (is (and (= 400 (:status response))
                 (= "empty data" (get (json-body response) "error"))))))))

(deftest post-cp
  (testing "success"
    (let [args {:src interjection-cid :dest init-dir-cid :path "interjection"}
          response @(http-post (make-url "/cp")
                               {:body (json/write-str args)})]
      (is (and (= 200 (:status response))
               (= combined-dir-cid (get (json-body response) "cid"))))))
  (testing "dest not directory"
    (let [args {:src init-dir-cid :dest interjection-cid :path "interjection"}
          response @(http-post (make-url "/cp")
                               {:body (json/write-str args)})]
      (is (and (= 400 (:status response))
               (= "dest is not a directory"
                  (get (json-body response) "error")))))))

(deftest post-add
  (let [url (make-url "/dataset")
        full {"file" interjection-cid
              "description" "Interjection"
              "source" "https://wiki.installgentoo.com"
              "topics" ["Natural language" "copypasta"]
              "language" "English"}
        less (dissoc full "source")]
    (testing "success"
      (let [response @(http-post url {:body (json/write-str full)})]
        (is (= 200 (:status response)))))
    (testing "missing metadata"
      (let [response @(http-post url {:body (json/write-str less)})]
        (is (and (= 400 (:status response))
                 (= {"missing-metadata" ["source"]}
                    (get (json-body response) "error"))))))))

(deftest post-update
  (let [url (make-url "/update")
        full {"parent" "1"
              "source" "https://wiki.installgentoo.com/wiki/Interjection"}
        less (dissoc full "parent")]
    (testing "success"
      (let [response @(http-post url {:body (json/write-str full)})]
        (is (= 200 (:status response)))))
    (testing "orphan"
      (let [response @(http-post url {:body (json/write-str less)})]
        (is (and (= 400 (:status response))
                 (= "missing parent"
                    (get (json-body response) "error"))))))))

(deftest post-find
  (let [url (make-url "/find")
        options {:accept :json
                 :content-type :json
                 :body (json/write-str ["~" ["." "file"] "^$"])}]
    (testing "success"
      (let [response @(http-post url options)]
        (is (and (= 200 (:status response))
                 ;; Obviously file CID cannot be empty
                 (empty? (json-body response))))))
    (testing "malformed query"
      (let [override {:body "{"}
            response @(http-post url (merge options override))]
        (is (and (= 400 (:status response))
                 (= "malformed query"
                    (get (json-body response) "error"))))))))

(deftest get-dir
  (testing "success"
    (let [response @(http-get (make-url (str "/dir/" init-dir-cid)))]
      (is (and (= 200 (:status response))
               (= init-dir (json-body response))))))
  (testing "not directory"
    (let [response @(http-get (make-url (str "/dir/" interjection-cid)))]
      (is (and (= 400 (:status response))
               (= "not a directory" (get (json-body response) "error"))))))
  (testing "not CID"
    (let [response @(http-get (make-url "/dir/this-cid-does-not-exist"))]
      (is (and (= 400 (:status response))
               (= "not a directory" (get (json-body response) "error")))))))

(deftest get-file
  (testing "success"
    (let [response @(http-get (make-url (str "/file/" interjection-cid)))]
      (is (and (= 200 (:status response))
               (= (slurp (:body response))
                  (slurp interjection))))))
  (testing "not found"
    (let [response @(http-get (make-url "/file/this-cid-does-not-exist"))]
      (is (and (= 404 (:status response))
               (= "content not found"
                  (get (json-body response) "error")))))))

(deftest not-found
  (let [response @(http-get (make-url "/this/endpoint/is/unsupported"))]
    (is (and (= 404 (:status response))
             (= "unsupported" (get (json-body response) "error"))))))

(defn test-ns-hook []
  (let [server (-main (str port))]
    (try (not-found)
         (post-file)
         (post-dir)
         (post-cp)
         (post-add)
         (post-update)
         (post-find)
         (get-file)
         (get-dir)
         (finally (.close server)
                  (wait-for-close server)))))
