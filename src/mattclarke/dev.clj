(ns mattclarke.dev
  (:require [hawk.core :as hawk]
            [me.raynes.fs :refer [copy]]))

(defn copy-css!
  "Copy css from resources to target"
  []
  (copy "resources/public/css/global.css" "target/public/css/global.css"))

(defn run-when-changed
  "Run the live dev watcher"
  [path]
  (hawk/watch! [{:paths [path]
                 :handler (fn [ctx e]
                            (println "event: " e)
                            (copy-css!))}]))

(run-when-changed "resources/public/")