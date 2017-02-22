(ns arl-docker.core
  (:require [bultitude.core :as bns]
            [taoensso.timbre :as log]
            [loom.graph :as g])
  (:gen-class))

(defn comp-to-map [dict comp-ns]
  (require comp-ns)
  (let [items (ns-publics comp-ns)
        spec (deref (get items 'spec))
        spec2 (assoc spec :build (get items 'build))]
    (log/debug "parsing spec:" spec2)
    (log/debug ((:build spec2)))
    (assoc dict (:name spec2) spec2)))

(defn to-adjacency [m [k v]]
  (log/info "What?" k (or (:depends v) #{}))
  (let [d (:depends v)]
    (if d (assoc m k d) m)))

(defn comps-to-graph [comps]
  (g/digraph (reduce to-adjacency {} comps)))

(defn load-components
  "Load the component definitions from the given directory."
  [prefix]
  (let [comp-ns (bns/namespaces-on-classpath :prefix prefix)]
    ;; Load the specification for each namespace, and build two
    ;; data structures: a map from names/keywords to specs, and a
    ;; dependency graph.
    (let [comps (reduce comp-to-map {}  comp-ns)
          comp-graph (comps-to-graph comps)]
      {:comps comps
       :graph comp-graph})))

(defn load-builds
  "Load the build definitions from the given directory."
  [dir])


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
