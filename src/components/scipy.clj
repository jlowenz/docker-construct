(ns components.scipy
  (:use arl-docker.dsl
            arl-docker.util))

(def spec {:name :scipy
           :version "0.18.1"
           :description "SciPy: Scientific Library for Python"
           :depends #{:python}})

(defn build []
  (component
    spec
    (pip-install "scipy")))