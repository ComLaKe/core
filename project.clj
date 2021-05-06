(defproject comlake-core "0.1.1"
  :description "USTH data lake core"
  :url "https://github.com/ComLake/core"
  :license {:name "GNU Affero General Public License 3.0"
            :url "https://www.gnu.org/licenses/agpl-3.0"}
  :repositories [["jitpack" "https://jitpack.io"]]
  :dependencies [[aleph "0.4.6"]
                 [com.apa512/rethinkdb "1.0.0-SNAPSHOT"]
                 [com.github.ipfs/java-ipfs-http-client "1.3.3"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/data.json "2.2.1"]
                 [ring/ring-devel "1.9.2"]
                 [ring/ring-jetty-adapter "1.9.2"]]
  :main ^:skip-aot comlake-core.main
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-cloverage "1.2.2"]
            [lein-codox "0.10.7"]]
  :codox {:source-uri ~(str "https://github.com/ComLake/core"
                            "/blob/{git-commit}/{filepath}#L{line}")})
