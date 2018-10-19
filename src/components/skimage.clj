(ns components.skimage
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :skimage
           :version "0.12.3"
           :description "Image processing routines for SciPy"
           :depends #{:python :scipy}})

(defn build []
  (component
    spec
    (pip-install "scikit-image")))
