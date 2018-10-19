(ns components.vtk-7-1
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :vtk
           :version "7.1.0"
           :depends #{:opengl :boost :python :java}})

(def cwd "/tmp/vtk7.1.0")
(def repo "https://github.com/Kitware/VTK")
(def rev "v7.1.0")

(defn build []
  (component
   spec
   (install "doxygen")
   (run
     (git-latest cwd repo {:rev rev})
     (cmake cwd {"CMAKE_BUILD_TYPE" "RelWithDebInfo"
                 "CMAKE_INSTALL_PREFIX" "/usr/local"
                 "BUILD_DOCUMENTATION" "ON"
                 "VTK_USE_CXX11_FEATURES" "ON"
                 "VTK_WRAP_JAVA" "ON"
                 "VTK_WRAP_PYTHON" "ON"})
     (rm cwd))))
