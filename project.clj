(defproject arl_docker "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [bultitude "0.2.8"] ;; same idea as tools.namespace.find
                 [com.taoensso/timbre "4.3.1"]
                 [aysylu/loom "1.0.0"]
                 [me.raynes/fs "1.4.6"]
                 [manifold "0.1.6"]
                 [aleph "0.4.3"]
                 [byte-streams "0.2.3"]]
  :main ^:skip-aot arl-docker.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
