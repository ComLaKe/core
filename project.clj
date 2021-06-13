(defproject comlake.core "0.3.0-SNAPSHOT"
  :description "USTH data lake core"
  :url "https://github.com/ComLake/comlake.core"
  :license {:name "GNU Affero General Public License 3.0"
            :url "https://www.gnu.org/licenses/agpl-3.0"}
  :repositories [["jitpack" "https://jitpack.io"]]
  :dependencies [[aleph "0.4.6"]
                 [com.github.ComLake/java-ipfs-http-client "1.3.5"]
                 [com.github.jezza/toml "1.2-java-8"]
                 [com.google.code.gson/gson "2.8.6"]
                 [com.mchange/c3p0 "0.9.5.5"]
                 [com.taoensso/timbre "5.1.2"]
                 [net.harawata/appdirs "1.2.1"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/data.json "2.2.1"]
                 [org.postgresql/postgresql "42.2.20"]]
  :main ^:skip-aot comlake.core.main
  :java-source-paths ["src"]
  :target-path "target/%s"
  :profiles {:repl {:plugins [[cider/cider-nrepl "0.26.0"]
                              [lein-virgil "0.1.9"]]}
             :uberjar {:aot :all}}
  :plugins [[lein-cloverage "1.2.2"]
            [lein-codox "0.10.7"]]
  :warn-on-reflection false
  :codox {:source-uri ~(str "https://github.com/ComLake/comlake.core"
                            "/blob/{git-commit}/{filepath}#L{line}")})
