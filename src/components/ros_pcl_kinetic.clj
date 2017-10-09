(ns components.ros-pcl-kinetic
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :ros-pcl
           :version "kinetic"
           :depends #{:ros :python :cppdev}})

(defn build []
  (component
   spec
   (install ["ros-kinetic-pcl-ros"
             "ros-kinetic-pcl-conversions"])))
