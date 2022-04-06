(ns mattclarke.utils
    (:import java.util.Base64)
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn remove-ext [s] (first (str/split s #"\.")))

(defn str=> [from to] (str from " => " to))

(defn get-files
  "Read all file paths in dir (a directory). Return a vector of Java Files"
  [dir]
  (filter #(.isFile %) (file-seq (io/file dir))))

(defn encode [to-encode]
  (String. (.encode (Base64/getEncoder) (.getBytes to-encode))))

(defn get-file-name 
  "Get the end of the path"
  [path]
  (-> path java.io.File. .getName))

(defn get-file-extension [path]
  (second (re-find #"(\.[a-zA-Z0-9]+)$" path)))

;; (defn decode [to-decode]
;;   (String. (.decode (Base64/getDecoder) to-decode)))