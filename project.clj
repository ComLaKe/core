(defproject ulake-core "0.1.0-SNAPSHOT"
  :description "USTH data lake core"
  :url "https://git.sr.ht/~cnx/ulake-core"
  :license {:name "GNU Affero General Public License v3.0"
            :url "https://www.gnu.org/licenses/agpl-3.0"}
  :repositories [["jitpack" "https://jitpack.io"]]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [aleph "0.4.6"]
                 [com.github.ipfs/java-ipfs-http-client "1.3.3"]
                 [com.rethinkdb/rethinkdb-driver "2.4.4"]
                 [ring/ring-devel "1.9.2"]
                 [ring/ring-jetty-adapter "1.9.2"]]
  :main ^:skip-aot ulake-core.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
