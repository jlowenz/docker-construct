(ns arl-docker.util
  (:require [clojure.string :as str]
            [arl-docker.dsl :as dsl]))

(defn hg-latest
  "Run mercurial to get a repository."
  [] nil)

(defn git-latest
  "Run git to get a repository."
  ([cwd url] 
   [(str "git clone " url " " cwd)])
  ([cwd url opts]
   (into (git-latest cwd url) 
         (let [rev (:rev opts)
               branch (:branch opts)]
           (cond rev [(str "pushd " cwd)
                      (str "git checkout tags/" rev)
                      "popd"]
                 branch [(str "pushd " cwd)
                         (str "git checkout " branch)
                         "popd"]
                 :else [])))))

(defn to-def
  [[k v]]
  (str "-D" k "=" v))

(defn gen-defs
  [defs]
  (str/join " " (map to-def defs)))

(defn cmake
  "Run cmake targeting a specific source directory. Creates an internal build directory, calls cmake with the given definitions, runs make install automatically in parallel"
  [src defs]
  [(str "pushd " src)
   (str "mkdir -p build")
   (str "pushd build")
   (str "cmake " (gen-defs defs) " ..")
   (str "make -j $(nproc) install")
   "popd" "popd"])

(defn rm
  [target]
  (str "rm -rf " target))

(defn chain-multiline
  [items]
  (str/join " \\\n\t" items))

(defn install
  [items]
  (dsl/run
    (str "DEBIAN_FRONTEND=noninteractive apt-get -y update")
    "apt-get -y upgrade"
    (str "apt-get -y install " (chain-multiline items))))
