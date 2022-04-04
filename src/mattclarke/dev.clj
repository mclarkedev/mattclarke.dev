(ns mattclarke.dev
  (:require [hawk.core :as hawk]
            [me.raynes.fs :refer [copy]]
            [mattclarke.core :refer [run!!]]))

(def dev-config
  {:paths ["src/mattclarke" "resources"]})

(defn copy-css!
  "Copy css from resources to target"
  []
  (copy "resources/public/css/global.css" "target/public/css/global.css"))

(defn run-when-changed
  "Run the live dev watcher"
  [paths]
  (hawk/watch! [{:paths paths
                 :handler (fn [ctx e]
                            (println "event: " e)
                            ;; (copy-css!)
                            (run!! {:args ""})
                            )}]))

(run-when-changed (dev-config :paths))
