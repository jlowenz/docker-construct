(ns components.keras
  (:use arl-docker.dsl
        arl-docker.util))


(def spec {:name :keras
           :version "latest"
           :description "Provides Keras and Tensorflow installed on a scientific python base"
           :depends #{:scipy :sklearn :skimage}})

(def apt-pkgs ["libhdf5-dev"])
(def pip-pkgs ["PyYAML" "h5py" "jupyter" "tensorflow-gpu" "Keras" "elephas"])

(defn build []
  (component
    spec
    (pip-install pip-pkgs)))

