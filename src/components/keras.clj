(ns components.keras
  (:use arl-docker.dsl
        arl-docker.util))


(def spec {:name :keras
           :version "latest"
           :description "Provides Keras and Tensorflow installed on a scientific python base"
           :depends #{:numpy :scipy :sklearn :skimage}})

(defn build []
  (component
    spec
    (run (pip-install ))))
