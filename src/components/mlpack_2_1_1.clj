(ns components.mlpack_2_1_1
  (:use arl-docker.dsl arl-docker.util))

(def spec {:name :mlpack
           :version "2.1.1"
           :depends #{:openblas}})

(defn build []
  (let [repo "https://github.com/mlpack/mlpack.git"
        rev "mlpack-2.1.1"
        src "/tmp/mlpack"]
    (component
      spec
      (workdir "/tmp")
      (run (git-latest repo {:rev rev})
           (cmake src {"CMAKE_INSTALL_PREFIX" "/usr/local"})
           (rm src)))))
