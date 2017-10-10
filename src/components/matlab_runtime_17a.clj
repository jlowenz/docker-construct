(ns components.matlab_runtime_17a
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :matlab-runtime
           :version "17a"
           :depends #{:cppdev :java}})

(def url "http://ssd.mathworks.com/supportfiles/downloads/R2017a/deployment_files/R2017a/installers/glnxa64/MCR_R2017a_glnxa64_installer.zip")
(def fname "MCR_R2017a_glnxa64_installer")
(def zipfile "~/Downloads/mcr.zip")
(def cwd "mlinst")
(def local-path "~/Downloads/mcr.zip")
(def url-path "/matlab-runtime-17a")

(defn init! []
  (download-locally! url local-path)
  (serve! local-path url-path))

(defn build []
  (component
   spec
   (workdir "/tmp")
   (run (wget-local url-path)
     (unzip cwd zipfile)
     (str "pushd " cwd)
     (str "./install -mode silent -agreeToLicense yes ")
     "popd"
     (rm zipfile cwd))))

(defn shutdown! []
  (kill-server!))
