(ns components.openblas
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :openblas
           :version "latest"
           :depends #{:cppdev}})

(def repo "https://github.com/xianyi/OpenBLAS")
(def libgfortran "/usr/lib/gcc/x86_64-linux-gnu/5")
(def pkgs ["gfortran"
           "libatlas-dev"
           "libatlas-base-dev"
           "libblas-dev"
           "libflann-dev"
           "libgfortran3"
           "libgfortran-4.8-dev"
           "liblapack-dev"])

(defn build []
  (let [src-path "/tmp/openblas"]
    (component
      spec
      (install pkgs)
      (add-ld-path "/opt/OpenBLAS/lib" "openblas.conf")
      (workdir "/tmp")
      (run (git-latest src-path repo)
           (str "pushd " src-path)
           (str "LD_LIBRARY_PATH=" libgfortran ":$LD_LIBRARY_PATH make -j "
                "$(nproc) NO_AFFINITY=1 USE_OPENMP=1")
           "make -j $(nproc) install"
           "popd"
           (rm src-path)))))
