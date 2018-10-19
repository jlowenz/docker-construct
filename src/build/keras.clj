(ns build.keras)

(def spec {:name "keras"
           :creator "Jason L. Owens <jason.l.owens.civ@mail.mil>"
           :description "A Keras development environment with the following additional packages: PCL, mlpack"
           :base "nvidia/cuda:8.0-cudnn5-devel-ubuntu16.04"
           :depends #{:keras :pcl :mlpack}})