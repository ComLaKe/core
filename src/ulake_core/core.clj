(ns ulake-core.core
  (:gen-class)
  (:require [ulake-core.service])
  (:import (com.rethinkdb RethinkDB)
           (io.grpc Server ServerBuilder)
           (io.grpc.stub StreamObserver)
           (ulake-core.service GreeterServiceImpl)))

(def SERVER_PORT 50051)

(defn start []
  (let [greeter-service (new GreeterServiceImpl)
        server (-> (ServerBuilder/forPort SERVER_PORT)
                   (.addService greeter-service)
                   (.build)
                   (.start))]
    (-> (Runtime/getRuntime)
        (.addShutdownHook (Thread. #(if (not (nil? server))
                                      (.shutdown server)))))
    (if (not (nil? server))
      (.awaitTermination server))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (with-open [conn (-> (.connection RethinkDB/r) .connect)]
    (-> (.range RethinkDB/r 10) (.coerceTo "array") (.run conn) println))
  (print "Now listening on port " SERVER_PORT)
  (start))
