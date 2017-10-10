(ns system
  (:require [arl-docker.core :as core]))

(defn start!
  "Start the system, return the system state"
  []
  (let [[cs bs] (core/default-load)
        comps (:comps cs)
        inits (:core/inits comps)
        shutdowns (:core/shutdowns comps)
        graph (:graph cs)]
    ;; execute the initializers for the components
    (doseq [init-fn inits] (init-fn))
    {:inits inits
     :shutdowns shutdowns
     :comps (dissoc comps :core/inits :core/shutdowns)
     :graph graph
     :builds bs}))

(defn stop!
  "Stop the system, stopping and releasing resources"
  [sys]
  (let [shutdowns (:shutdowns sys)]
    (doseq [shutdown-fn shutdowns] (shutdown-fn))))
