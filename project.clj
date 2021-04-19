(defproject ulake-core "0.1.0-SNAPSHOT"
  :description "USTH data lake core"
  :url "https://git.sr.ht/~cnx/ulake-core"
  :license {:name "GNU Affero General Public License v3.0"
            :url "https://www.gnu.org/licenses/agpl-3.0"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [com.rethinkdb/rethinkdb-driver "2.4.4"]
                 [compojure "1.6.2"]
                 [http-kit "2.3.0"]
                 [ring/ring-devel "1.9.2"]
                 [ring/ring-jetty-adapter "1.9.2"]]
  :main ^:skip-aot ulake-core.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
