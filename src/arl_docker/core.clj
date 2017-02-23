(ns arl-docker.core
  (:require [bultitude.core :as bns]
            [taoensso.timbre :as log]
            [loom.graph :as g]
            [loom.alg :as alg]
            [clojure.string :as str]
            [clojure.java.io :as jio]
            [me.raynes.fs :as fs])
  (:use arl-docker.dsl arl-docker.util)
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
  (let [d (sort (:depends v))]
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

(defn expand-deps [comps deps]
  (loop [coll #{}
         deps deps]
    (if (empty? deps)
      coll
      (let [d (first deps)
            rec-d (:depends (get comps d))
            r (rest deps)]
        (recur (conj coll d) (into r rec-d))))))

(defn build-to-map [comps dict build-ns]
  (require build-ns)
  (let [items (ns-publics build-ns)
        spec (deref (get items 'spec))
        expanded-deps (expand-deps comps (:depends spec))
        bcomps (select-keys comps expanded-deps)]
    (assoc dict (:name spec) (assoc spec :graph (comps-to-graph bcomps)))))

(defn load-builds
  "Load the build definitions from the given directory."
  [comps prefix]
  (let [build-ns (bns/namespaces-on-classpath :prefix prefix)
        builds (reduce (partial build-to-map comps) {} build-ns)]
    builds))

(defn make-header [build]
  (str/join "\n" [(str "# Docker Build: " (:name build))
                  (str "# This file is auto-generated using docker-construct")
                  (str "FROM " (:base build))
                  (label (dissoc build :graph :depends))]))

(defn make-footer [build]
  (let [entrypoint (:entrypoint build)
        cmd (:cmd build)]
    (str/join "\n" (filter (comp not nil?) [entrypoint cmd]))))

(defn wrap-components [comps build]
  (let [header (make-header build)
        footer (make-footer build)]
    (str/join "\n" (flatten [header comps footer]))))

(defn make-dockerfile [build comps build-dir]
  (let [comp-order (reverse (alg/topsort (:graph build)))
        _ (log/debug comp-order)
        comp-strs (mapv #((:build (get comps %1))) comp-order)
        _ (log/debug comp-strs)
        full-build (wrap-components comp-strs build)
        _ (log/debug full-build)
        dockerfile (fs/file build-dir "Dockerfile")]
    (jio/make-parents dockerfile)
    (spit dockerfile full-build)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
