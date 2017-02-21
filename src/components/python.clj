(ns components.python
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :python
           :description "Ensure python is installed with latest pip"
           :version "2.7"})

(def pipurl "https://bootstrap.pypa.io/get-pip.py")

(defn build []
  (component
    spec
    (install ["curl" "python" "python-dev"])
    (run (str "curl " pipurl " | python" ))))