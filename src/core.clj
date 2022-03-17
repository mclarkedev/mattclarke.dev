(ns dev.mattclarke.core
  (:require [markdown.core :refer [md-to-html-string md-to-html]]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def build-config
  {:input-md-from "resources/markdown/"
   :output-html-to "target/public/"})

(defn remove-ext [s] (first (str/split s #"\.")))

(defn get-files
  "Read all file paths in dir (a directory). Return a vector of Java Files"
  [dir]
  (filter #(.isFile %) (file-seq (io/file dir))))

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
                       (str dir (md :html-name)))))

(defn report [md-data]
  (map #(str (:md-name %) " => " (:html-name %)) md-data))

(defn build []
  (let [md-data (get-markdown-data (build-config :input-md-from))]
    (write-md-to-html!
     md-data
     (build-config :output-html-to))
    (report md-data)))

(build)
