(defproject comlake-core "0.2.0-SNAPSHOT"
  :description "USTH data lake core"
  :url "https://github.com/ComLake/core"
  :license {:name "GNU Affero General Public License 3.0"
            :url "https://www.gnu.org/licenses/agpl-3.0"}
  :repositories [["jitpack" "https://jitpack.io"]]
  :dependencies [[aleph "0.4.6"]
                 [com.apa512/rethinkdb "1.0.0-SNAPSHOT"]
                 [com.github.ComLake/java-ipfs-http-client "1.3.4"]
                 [com.google.code.gson/gson "2.8.6"]
                 [com.mchange/c3p0 "0.9.5.5"]
                 [org.postgresql/postgresql "42.2.20"]
                 [com.taoensso/timbre "5.1.2"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/data.json "2.2.1"]]
  :main ^:skip-aot comlake-core.main
  :java-source-paths ["src"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-codox "0.10.7"]
            [lein-virgil "0.1.9"]]
  :warn-on-reflection false
  :codox {:source-uri ~(str "https://github.com/ComLake/core"
                            "/blob/{git-commit}/{filepath}#L{line}")})
