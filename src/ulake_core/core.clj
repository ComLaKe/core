(ns ulake-core.core
  (:gen-class)
  (:require [ulake-core.grpc])
  (:import (com.rethinkdb RethinkDB)
           (io.grpc ServerBuilder)
           (io.grpc.stub StreamObserver)
           (ulake-core.grpc GreeterServiceImpl)))

(def SERVER_PORT 50051)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (with-open [conn (.connect (.connection RethinkDB/r))]
    (-> (.range RethinkDB/r 10)
        (.coerceTo "array")
        (.run conn)
        println))
  (print "Now listening on port " SERVER_PORT)
  (let [greeter-service (GreeterServiceImpl.)
        server (-> (ServerBuilder/forPort SERVER_PORT)
                   (.addService greeter-service)
                   (.build)
                   (.start))]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. #(if (not (nil? server))
                                  (.shutdown server))))
    (if (not (nil? server))
      (.awaitTermination server))))
