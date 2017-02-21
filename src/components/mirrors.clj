(ns components.mirrors
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :mirrors
           :version "1.0"})

(defn build []
  (component
    spec
    (run "echo \"deb mirror://mirrors.ubuntu.com/mirrors.txt xenial main restricted universe multiverse\" > /etc/apt/sources.list"
         "echo \"deb mirror://mirrors.ubuntu.com/mirrors.txt xenial-updates main restricted universe multiverse\" >> /etc/apt/sources.list"
         "echo \"deb mirror://mirrors.ubuntu.com/mirrors.txt xenial-security main restricted universe multiverse\" >> /etc/apt/sources.list"
         "echo \"deb mirror://mirrors.ubuntu.com/mirrors.txt xenial-proposed main restricted universe multiverse\" >> /etc/apt/sources.list"
         "DEBIAN_FRONTEND=noninteractive apt-get update")
    (shell "/bin/bash" "-c")))