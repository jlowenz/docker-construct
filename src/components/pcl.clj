(ns components.pcl
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :pcl
           :version "1.8.0"
           :depends #{:vtk :boost}})

(def pcl-cwd "/tmp/pcl")
(def pcl-repo "http://github.com/PointCloudLibrary/pcl.git")
(def pcl-rev "pcl-1.8.0")
(def pcl-deps ["libeigen3-dev" "libopenni-sensor-primesense0"
               "libopenni-sensor-primesense-dev" "libopenni-dev"
               "libopenni0" "openni-utils" "libqhull-dev"
               "libqhull7" "libopenni2-dev" "openni2-utils"])

(defn build []
  (component spec 
   (install pcl-deps)
   (run
     (git-latest pcl-cwd pcl-repo {:rev pcl-rev})
     (cmake pcl-cwd {"CMAKE_BUILD_TYPE" "RelWithDebInfo"
                     "CMAKE_INSTALL_PREFIX" "/usr/local"})
     (rm pcl-cwd))))
