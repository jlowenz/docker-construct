(ns components.opengl
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :opengl
           :version "1.0"
           :description "Attempt to provide support for OpenGL"})

(def pkgs ["xserver-xorg-dev" "libxt-dev" "mesa-utils" "mesa-common-dev" "mesa-utils-extra" "libgl1-mesa-dev" "libglapi-mesa"])

(defn build []
  (component
    spec
    (install pkgs)))
