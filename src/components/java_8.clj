(ns components.java-8
  (:use arl-docker.dsl
        arl-docker.util))

(def spec {:name :java
           :version "8"
           :depends #{:mirrors}})

(def ppa "webupd8team/java")
;(def keyid "7B2C3B0889BF5709A105D03AC2518248EEA14886")
(def pkgs ["oracle-java8-installer"])
(def envs {"JAVA_HOME" "/usr/lib/jvm/java-8-oracle"})

(defn build []
  (component
   spec
   (run "echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections")
   (add-ppa ppa)
   (install pkgs)
   (run "update-java-alternatives -s java-8-oracle")
   (env envs)))
