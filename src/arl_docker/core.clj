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

(defn ver-to-string [v]
  (cond
    (string? v) v
    (keyword? v) (if (= :latest v) "z:latest" (str v))
    :else (str v)))

;; Need to handle the :version part of the spec i.e. all specs should be in a dictionary keyed by the version...
;; so: {:boost {"1.58.0" (boost-1-58-spec)
;;              "1.63.0" (boost-1-63-spec)}}
;; etc... of course the question is how to handle the default
;; pick the highest version???
(defn comp-to-map [dict comp-ns]
  (require comp-ns)
  (let [items (ns-publics comp-ns)
        spec (deref (get items 'spec))
        spec2 (assoc spec :build (get items 'build))
        init-fn (get items 'init!)
        cname (:name spec)
        ver (or (:version spec) "0.0.1")       
        existing-versions (or (get dict cname)
                              (sorted-map-by
                               (fn [a b] (>= (compare (ver-to-string a)
                                                      (ver-to-string b)) 1))))]
    (log/debug "parsing spec:" spec2)
    (log/debug ((:build spec2)))
    (when init-fn (init-fn)) ;; call the init function if it exists
    (assoc dict cname (assoc existing-versions ver spec2))))

(defn to-adjacency [m [k v]]
  "Convert the components graph into an adjacency list. Need to
  coalesce the dependencies of multiple versions... This is not
  exactly the right way to do it... but not sure how otherwise."
  (log/debug "to-adjacency\n" m k v)
  ;;(let [deps (reduce #(assoc %1 [k %2] (:depends %3)) {} v)])
  (let [deps (into #{} (mapcat :depends (vals v)))
        d (sort deps)]
    (if d (assoc m k d) m)))

(defn comps-to-graph [comps]
  (g/digraph (reduce to-adjacency {} comps)))

(defn get-component-by-dep
  "Get the component from `comps` specified by the `curr` dependency"
  [curr comps]
  (cond
    (keyword? curr) (let [cname curr
                          comp (apply hash-map (first (get comps cname)))]
                      (log/debug "get-component-by-dep" curr comp)
                      [curr comp]) 
    (vector? curr) (let [cname (first curr)
                         cver (second curr)
                         comp (get-in comps [cname cver])]
                     (log/debug "get-component-by-dep" curr {cver comp})
                     [cname {cver comp}])
    :else (assert false "Unknown dependency specification")))

(defn load-components
  "Load the component definitions from the given directory."
  [prefix]
  (let [comp-ns (bns/namespaces-on-classpath :prefix prefix)]
    ;; Load the specification for each namespace, and build two
    ;; data structures: a map from names/keywords to specs, and a
    ;; dependency graph.
    (let [comps (reduce comp-to-map {} comp-ns)
          comp-graph (comps-to-graph comps)]
      {:comps comps
       :graph comp-graph})))

(defn get-depends [vspec] (-> (vals vspec) first :depends))

(defn expand-deps [comps deps]
  (log/info "expand-deps " comps deps)
  (loop [coll #{}
         deps deps]
    (if (empty? deps)
      coll
      (let [d (first deps)
            [d-name d-spec] (get-component-by-dep d comps)
            rec-d (get-depends d-spec)
            norm-dep (if (vector? d) d [d-name (or (:version d-spec) "0.0.1")])
            r (rest deps)]
        (assert d-spec
                (str "Unknown dependency [" d "]: did you create "
                     "the component file?"))
        (recur (conj coll d) (into r rec-d))))))

(defn comps-to-files [comps]
  (or (flatten (filter (comp not nil?) (map :files comps))) []))

(defn select-comps-from-deps [comps deps]
  "Comps is a map of components (see comp-to-map), while deps is a set
  of dependencies, which may refer to just the component key as a
  keyword, or may by a vector with the name and desired version. This
  function must select the specifications for the correct dependency
  components and versions."
  (loop [sel {}
         deps deps]
    (if (nil? deps)
      sel
      (let [curr (first deps)
            [dname dcomp] (get-component-by-dep curr comps)
            rdeps (next deps)]
        (assert (not (contains? sel dname)) "Version conflict")
        (recur (assoc sel curr dcomp) rdeps)))))


(defn build-to-map [comps dict build-ns]
  (require build-ns)
  (let [items (ns-publics build-ns)
        spec (deref (get items 'spec))
        expanded-deps (expand-deps comps (:depends spec))
        bcomps (select-comps-from-deps comps expanded-deps)
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
  (let [comp-order (into (vec (reverse (alg/topsort (:graph build))))
                         (mapv :name (:append-comps build)))
        _ (log/debug comps)
        _ (log/debug comp-order)
        comp-strs (mapv #((:build (-> (get-component-by-dep %1 comps) second vals first)))
                        comp-order)
        _ (log/debug comp-strs)
        full-build (wrap-components comp-strs build)
        _ (log/debug full-build)
        dockerfile (fs/file build-dir "Dockerfile")]
    (jio/make-parents dockerfile)
    (spit dockerfile full-build)
    (println "Copying files...")
    (copy-files build build-dir)))

(defn -main
  [& args]
  (let [comps (load-components "components")
        builds (load-builds (:comps comps) "build")
        build-key (keyword (first args))
        selected-build (get builds build-key)
        out-dir (second args)]
    (make-dockerfile selected-build comps out-dir)))
