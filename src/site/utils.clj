(ns site.utils
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn remove-ext [s] (first (str/split s #"\.")))

(defn str=> [from to] (str from " => " to))

(defn get-files
  "Read all file paths in dir (a directory). Return a vector of Java Files"
  [dir]
  (filter #(.isFile %) (file-seq (io/file dir))))
