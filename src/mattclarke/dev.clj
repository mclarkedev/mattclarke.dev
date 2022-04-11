(ns mattclarke.dev
  (:require [hawk.core :as hawk]
            [me.raynes.fs :refer [copy]]

            [clojure.java.shell :refer [sh]]))

(def dev-config
  {:paths ["src/mattclarke" "resources"]})

(defn copy-css!
  "Copy css from resources to target"
  []
  (copy "resources/public/css/global.css" "target/public/css/global.css"))

(defn get-file-extension [path]
  (second (re-find #"(\.[a-zA-Z0-9]+)$" path)))

(defn run-when-changed
  "Run the live dev watcher"
  [paths]
  (hawk/watch! [{:paths paths
                 :handler (fn [_ e]
                            (let [path (str (e :file))
                                  ext (get-file-extension path)]
                              ;; (println path)
                              (case ext
                                ".css" (copy-css!)
                                ;; ".md" (time (sh "make"))
                                ;; ".clj" (time (sh "make"))
                                (println "No operation. No file matched.")))
                            )}]))

(run-when-changed (dev-config :paths))