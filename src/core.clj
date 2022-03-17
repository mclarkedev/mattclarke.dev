(ns dev.mattclarke.core
  (:require [markdown.core :refer [md-to-html-string md-to-html]]
            [clojure.java.io :as io]
            [me.raynes.fs :refer [copy-dir-into]]
            [dev.mattclarke.utils :refer [remove-ext str=> get-files]]))

(def build-config
  {:input-md-from "resources/markdown/"
   :input-assets-from "resources/public/"
   :output-html-to "target/public/"
   :output-assets-to "target/public/"})

(defn make-markdown-data
  "Return :markdown-name, :basename, :html-name, :path for f (a Java File)"
  [f]
  (let [basename (remove-ext (.getName f))]
    {:md-name (.getName f)
     :basename basename
     :html-name (str basename ".html")
     :path (str f)}))

(defn get-markdown-data
  "Returns markdown data from dir (a directory) of .md files"
  [dir]
  (map make-markdown-data (get-files dir)))

(defn write-md-to-html!
  "Compile markdown files from our own markdown data to dir (an output directory).
   Creates dir if does not exsist."
  [md-data dir]
  (io/make-parents (str dir "_")) ;; '_' is used to make parents
  (doseq [md md-data] (md-to-html
                       (md :path)
                       (str dir (md :html-name))))
  (map #(str=> (:md-name %) (:html-name %)) md-data))

(defn copy-assets!
  "Copy public assets from resource to target"
  []
  (let [from (build-config :input-assets-from)
        to  (build-config :output-assets-to)]
    (copy-dir-into
     from
     to)
    (str=> from to)))

(defn build []
  {:md     (write-md-to-html!
            (get-markdown-data (build-config :input-md-from))
            (build-config :output-html-to))
   :assets (copy-assets!)})

(build)
