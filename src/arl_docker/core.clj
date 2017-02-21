(ns arl-docker.core
  (:require [bultitude.core :as bns]
            [taoensso.timbre :as log])
  (:gen-class))

(defn load-components
  "Load the component definitions from the given directory."
  [prefix]
  (let [comp-ns (bns/namespaces-on-classpath :prefix prefix)]
    ;; Load the specification for each namespace, and build two
    ;; data structures: a map from names/keywords to specs, and a
    ))

(defn load-builds
  "Load the build definitions from the given directory."
  [dir])


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
