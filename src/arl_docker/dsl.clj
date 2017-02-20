(ns arl-docker.dsl
  (:require [clojure.string :as str]))

;; There is no FROM... (like there is no spoon)

(defn component
  [spec & steps]
  (str "# COMPONENT: " (str (:name spec) " / " (:version spec)) "\n" 
       (str/join "\n" steps)))

(defn chain-cmd
  "Take a collection of cmds, and joins them with `&&`"
  [cmds]
  (str/join " && \\\n\t" cmds))

(defn run
  "Docker RUN <command> form. Accepts any number of cmds, which will
  be concatenated with `&&` to ensure continuity in a single shell
  call. Each command should be a single string; e.g. helper function output works well here."
  [& cmds]
  (str "RUN " (chain-cmd (flatten cmds)) "\n"))

(defn squote [s]
  (str "\"" s "\""))

(defn run-exec
  "Docker RUN exec form; accepts multiple arguments which will be translated thus:
     RUN [arg1, arg2, ..., argN]"
  [executable & args]
  (str "RUN [" (squote executable)       
       (if (not-empty args)
         (str "," (str/join "," (map squote args)) "]")
         "]")) "\n")

(defn cmd
  "Docker CMD instruction. Only one per build (so components are NOT allowed to use it). This is the default *shell* form."
  [cmd & args])

(defn cmd-exec
  "Docker CMD instruction, exec form."
  [& cmd-parts])

(defn cmd-args
  "Docker CMD instruction, default arguments to ENTRYPOINT instruction."
  [& args])

(defn label
  "Docker LABEL instruction, requires a map of key / value pairs to be included in the image."
  [props])

(defn expose
  "Docker EXPOSE instruction. Accepts multiple ports."
  [& ports])

(defn env
  "Docker ENV instruction; requires a map of key / value pairs"
  [envs]
  (str "ENV"))

(defn add
  "Docker ADD instruction: adds files or directories to the container."
  [& args])

(defn copy
  "Docker COPY instruction: preferred over ADD for simple file and directory copy, where auto-unpack and URL download support is not required."
  [& args])

(defn entrypoint-exec
  "Docker ENTRYPOINT instruction: configure a container to run as an executable. This function represents the exec form (preferred)."
  [executable & args])

(defn volume
  "Docker VOLUME instruction: creates a mount point with the specified name and marks is as holding externally mounted volumes from host or other containers."
  [& vols])

(defn user
  "Docker USER instruction: set the username/UID to use for subsequent instructions."
  [name])

(defn workdir
  "Docker WORKDIR instruction: set the working directory."
  [path])

(defn arg
  "Docker ARG instruction: set a variable for use during build-time. Specify `nil` for no default value."
  [key value])

(defn onbuild
  "Docker ONBUILD instruction: add a trigger to the image to be executed at a later time, when the image is used as a base for another build."
  [instr])

(defn stopsignal
  "Docker STOPSIGNAL instruction: sets the system call signal that will be sent to the container to exit."
  [sig])

(defn healthcheck
  "Docker HEALTHCHECK instruction: tells Docker how to test a container to check that it is still working. If cmd is `nil`, then healthcheck is disabled."
  [cmd & options])

(defn shell
  "Docker SHELL instruction: set the default shell for following commands."
  [executable & params])
