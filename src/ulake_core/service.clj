(ns ulake-core.service
  (:gen-class :name ulake-core.service.GreeterServiceImpl
              :extends io.grpc.examples.helloworld.GreeterGrpc$GreeterImplBase)
  (:import [io.grpc.stub StreamObserver]
           [io.grpc.examples.helloworld HelloReply]))

(defn -sayHello [this req res]
  (let [name (.getName req)]
    (doto res
      (.onNext (-> (HelloReply/newBuilder)
                   (.setMessage (str "Hello, " name))
                   (.build)))
      (.onCompleted))))
