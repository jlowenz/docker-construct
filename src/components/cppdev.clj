(ns components.cppdev
  (:use arl-docker.util
        arl-docker.dsl))

(def spec {:name :cppdev
           :version "1.0"})
(def pkgs ["build-essential"
           "cmake"
           "curl"
           "git"
           "hashalot"
           "mercurial"
           "pkg-config"
           "python"
           "python-dev"
           "wget"])

(defn build []
  (component spec (install pkgs)))