(ns components.sklearn
  (:use arl-docker.util
        arl-docker.dsl))

(def spec {:name :sklearn
           :version "0.18.1"
           :description "A set of Python modules for machine learning and data mining"
           :depends #{:python :scipy}})

(defn build []
  (component
    spec
    (pip-install "scikit-learn==0.18.1")))
