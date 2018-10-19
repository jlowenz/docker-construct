(ns components.user-entry
  (:use arl-docker.dsl
        arl-docker.util)
  (:require [clojure.java.io :as jio]))

(def file (.getFile (jio/resource "user_entrypoint.sh")))

(def spec {:name :user-entry
           :version "1.0"
           :description "Add a script to support UID/GID switching, and modify the container entrypoint"
           :files [file]})

(defn build []
  (component
    spec
    (add file "/usr/bin/user_entrypoint.sh")
    (entrypoint-exec "/usr/bin/user_entrypoint.sh")))
