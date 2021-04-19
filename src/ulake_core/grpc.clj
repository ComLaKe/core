(ns ulake-core.grpc
  (:gen-class :name ulake-core.grpc.GreeterServiceImpl
              :extends io.grpc.examples.helloworld.GreeterGrpc$GreeterImplBase)
  (:import (io.grpc.stub StreamObserver)
           (io.grpc.examples.helloworld HelloReply)))

(defn -sayHello [this request response]
  (let [name (.getName request)]
    (doto response
      (.onNext (-> (HelloReply/newBuilder)
                   (.setMessage (str "Hello, " name))
                   (.build)))
      (.onCompleted))))
