(ns system
  (:require [arl-docker.core :as core]))

;; Adapters to support universal start/stop 

(defn start!
  "Start the system, return the system state"
  []
  (core/system-start!))

(defn stop!
  "Stop the system, stopping and releasing resources"
  []
  (core/system-stop!))
