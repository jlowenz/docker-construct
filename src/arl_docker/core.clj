(ns arl-docker.core
  (:require [bultitude.core :as bns]
            [taoensso.timbre :as log]
            [loom.graph :as g]
            [loom.alg :as alg]
            [clojure.string :as str]
            [clojure.java.io :as jio]
            [me.raynes.fs :as fs])
  (:use arl-docker.dsl arl-docker.util)
  (:gen-class)
  (:import (java.io File)))

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
            d-spec (get comps d)
            rec-d (:depends d-spec)
            r (rest deps)]
        (assert d-spec
                (str "Unknown dependency [" d "]: did you create "
                     "the component file?"))
        (recur (conj coll d) (into r rec-d))))))

(defn comps-to-files [comps]
  (or (flatten (filter (comp not nil?) (map :files comps))) []))

(defn build-to-map [comps dict build-ns]
  (require build-ns)
  (let [items (ns-publics build-ns)
        spec (deref (get items 'spec))
        expanded-deps (expand-deps comps (:depends spec))
        bcomps (select-keys comps expanded-deps)
        app-comps (mapv #(get comps %1) (:append-components spec))
        files (comps-to-files bcomps)
        afiles (comps-to-files app-comps)]
    (assoc dict (:name spec) (assoc spec :graph (comps-to-graph bcomps)
                                         :append-comps app-comps
                                         :files (flatten [files afiles])))))

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
                  (label (dissoc build :graph :depends :entrypoint
                                 :cmd :base :files :append-comps
                                 :append-components))]))

;(defn convert-entrypoint [comp-specs ep]
;  (cond (string? ep) (entrypoint-exec ep)
;        (coll? ep) (entrypoint-exec (first ep) (rest ep))
;        (keyword? ep) ((:build (ep comp-specs)))))

(defn make-footer [build]
  (let [entrypoint (:entrypoint build)
        cmd (:cmd build)]
    (str/join "\n" (filter (comp not nil?) [entrypoint cmd]))))

(defn wrap-components [comp-str build]
  (let [header (make-header build)
        footer (make-footer build)]
    (str/join "\n" (flatten [header comp-str footer]))))

(defn default-load []
  (let [comp-prefix "components"
        build-prefix "build"
        cs (load-components comp-prefix)
        bs (load-builds (:comps cs) build-prefix)]
    [cs bs]))

(defn file-in-dir [dir file]
  (File. dir (.getName file)))

(defn copy-files
  "Copy referenced files from the build to the build-dir"
  [build build-dir]
  (let [files (map jio/file (:files build))
        out-dir (jio/file build-dir)]
    (doseq [f files]
      (println "Copying" f)
      (if (.isDirectory f)
        (fs/copy-dir f out-dir)
        (fs/copy f (file-in-dir out-dir f))))))

(defn make-dockerfile [build comps build-dir]
  (let [comp-order (into (vec (reverse (alg/topsort (:graph build)))) (mapv :name (:append-comps build)))
        _ (log/debug comp-order)
        comp-strs (mapv #((:build (get comps %1))) comp-order)
        _ (log/debug comp-strs)
        full-build (wrap-components comp-strs build)
        _ (log/debug full-build)
        dockerfile (fs/file build-dir "Dockerfile")]
    (jio/make-parents dockerfile)
    (spit dockerfile full-build)
    (println "Copying files...")
    (copy-files build build-dir)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
