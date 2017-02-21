(ns components.boost-1-63
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :boost
           :version "1.63.0"
           :depends #{:cppdev :python}})

(def fname "boost_1_63_0")
(def url "https://sourceforge.net/projects/boost/files/boost/1.63.0/boost_1_63_0.tar.bz2")
(def cwd (str "/tmp/" fname))
(def tarball (str fname ".tar.bz2"))
(def sha "beae2529f759f6b3bf3f4969a19c2e9d6f0c503edcb2de4a61d1428519fcb3b0")

(defn build []
  (component
    spec
    (workdir "/tmp")
    (run (str "wget " url)
         (checksum-sha256 sha tarball)
         (tar-extract-bz tarball)
         (str "pushd " cwd)
         (str "./bootstrap.sh --prefix=/usr/local")
         (str "./b2 -j $(nproc) install")
         "popd"
         (rm tarball cwd))))
