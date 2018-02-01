(ns build.ros-kinetic-base
  (:use arl-docker.util
        arl-docker.dsl))

(def spec {:name :ros-kinetic-base
           :version "1.0"
           :base "nvidia/cuda:8.0-cudnn5-devel-ubuntu16.04"
           :depends #{:mirrors :ros-pcl :cppdev :user-entry :matlab-runtime}})
