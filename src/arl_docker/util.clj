(ns arl-docker.util
  (:require [clojure.string :as str]
            [arl-docker.dsl :as dsl]))

(defn pipe [& cmds]
  (str/join " | " cmds))

(defn echo [& items]
  (str "echo " (apply dsl/squote items)))

(defn checksum-sha256
  "Implement an in-image checksum check"
  [hash fname]
  (pipe (echo hash fname) (str "sha256sum -c -")))

(defn tar-extract-bz
  [fname]
  (str "tar xjf " fname))

(defn hg-latest
  "Run mercurial to get a repository."
  [] nil)

(defn add-ld-path
  "Create a new ld load path file `ld-fname` with the given contents
  `path-to-lib`"
  [path-to-lib ld-fname]
  (dsl/run (str (echo path-to-lib) " > /etc/ld.so.conf.d/" ld-fname)))

(defn add-apt-source
  "Append or create a new apt source file"
  ([src file]
    (add-apt-source src file false))
  ([src file append]
   (let [redir (if append ">>" ">")]
     (str "sh -c 'echo \"" src "\" " redir " /etc/apt/sources.list.d/" file "'"))))

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
  [& targets]
  (str "rm -rf " (str/join " " targets)))



(defn add-ppa
  [ppa]
  (dsl/run "DEBIAN_FRONTEND=noninteractive apt-get -y install software-properties-common"
           (str "add-apt-repository -y ppa:" ppa)))

(defn install
  [& items]
  (dsl/run
    (str "DEBIAN_FRONTEND=noninteractive apt-get -y update")
    "apt-get -y upgrade"
    (str "apt-get -y install " (dsl/chain-multiline (flatten items)))))

(defn pip-install
  "Install a collection of python packages using pip"
  [& items]
  (dsl/run
    (str "pip install -U " (dsl/chain-multiline (flatten items)))))
