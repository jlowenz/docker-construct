(ns components.keras
  (:use arl-docker.dsl
        arl-docker.util))


(def spec {:name :keras
           :version "latest"
           :depends #{:python}})

(defn build []
  (component
    spec
    (run (pip-install ))))