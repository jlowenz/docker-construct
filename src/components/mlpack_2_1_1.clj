(ns components.mlpack_2_1_1
  (:use arl-docker.dsl arl-docker.util))

(def spec {:name :mlpack
           :version "2.1.1"
           :depends #{:openblas :boost}})

(defn build []
  (let [repo "https://github.com/mlpack/mlpack.git"
        rev "mlpack-2.1.1"
        src "/tmp/mlpack"]
    (component
      spec
      (workdir "/tmp")
      (install "libarmadillo-dev")

      (run (git-latest src repo {:rev rev})
           (cmake src {"CMAKE_INSTALL_PREFIX" "/usr/local"})
           (rm src)))))
