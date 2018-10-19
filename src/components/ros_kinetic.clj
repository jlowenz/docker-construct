(ns components.ros-kinetic
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :ros
           :version "kinetic"
           :depends #{:cppdev :python}})

(def ros-key "421C365BD9FF1F717815A3895523BAEEB01FA116")

(defn build []
  (component
    spec
    (run (add-apt-source "deb http://packages.ros.org/ros/ubuntu xenial main"
                         "ros-latest.list")
         (str "apt-key adv --keyserver hkp://ha.pool.sks-keyservers.net:80 --recv-key " ros-key))
    (install ["ros-kinetic-ros-base"
              "python-rosinstall"])
    (run "rosdep init")))
