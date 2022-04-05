(ns mattclarke.dev
  (:require [hawk.core :as hawk]
            [me.raynes.fs :refer [copy]]
            [mattclarke.core :refer [build-md!]]))

(def dev-config
  {:paths ["src/mattclarke" "resources"]})

(defn copy-css!
  "Copy css from resources to target"
  []
  (copy "resources/public/css/global.css" "target/public/css/global.css"))

(defn file-extension [path]
  (second (re-find #"(\.[a-zA-Z0-9]+)$" path)))

(defn run-when-changed
  "Run the live dev watcher"
  [paths]
  (hawk/watch! [{:paths paths
                 :handler (fn [_ e]
                            (let [path (str (e :file))
                                  ext (file-extension path)]
                              (println path)
                              (case ext
                                ".css" (copy-css!)
                                ".md" (time (build-md!))
                                ".clj" (time (build-md!))
                                (println "No operation. No file matched.")))
                            )}]))

(run-when-changed (dev-config :paths))
