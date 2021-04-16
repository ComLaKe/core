(defproject ulake-core "0.1.0-SNAPSHOT"
  :description "USTH data lake core"
  :url "https://git.sr.ht/~cnx/ulake-core"
  :license {:name "GNU Affero General Public License v3.0"
            :url "https://www.gnu.org/licenses/agpl-3.0"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [com.google.protobuf/protobuf-java "3.15.8"]
                 [io.netty/netty-codec-http2 "4.1.63.Final"]
                 [io.grpc/grpc-netty "1.37.0"]
                 [io.grpc/grpc-protobuf "1.37.0"]
                 [io.grpc/grpc-stub "1.37.0"]
                 [javax.annotation/javax.annotation-api "1.3.2"]]
  :plugins [[lein-protoc "0.5.0"]]
  :protoc-version "3.15.8"
  :protoc-grpc {:version "1.37.0"}
  :main ^:skip-aot ulake-core.core
  :target-path "target/%s"
  :proto-target-path "target/generated-sources/protobuf"
  :java-source-paths ["target/generated-sources/protobuf"]
  :profiles {:uberjar {:aot :all}})
