(ns components.boost-1-58
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :boost
           :version "1.58.0"
           :depends #{:cppdev :python}})

(defn build []
  (component
    spec
    (install ["libboost-all-dev"])))
