(ns components.leiningen
  (:use arl-docker.dsl
        arl-docker.util)
  (:require [clojure.java.io :as jio]))

(def spec {:name :leiningen
           :version "2.7.1"})

(def target "/usr/local/bin/lein")
;; if the file is small enough, then we can just add it in as a resource
(def file (.getFile (jio/resource "lein")))

(defn build []
  (component
   spec
   (add file target)))
