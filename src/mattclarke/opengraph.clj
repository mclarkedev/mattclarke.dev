(ns mattclarke.opengraph
  (:require [clojure.string :as str]
            [net.cgrand.enlive-html :as html]
            [mattclarke.browser :refer [build-screenshots!]]))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn meta-tags [url] (filter #(get-in % [:attrs :property]) (html/select url [:meta])))

(defn og-tags [meta-tags] (filter #(str/starts-with? (get-in % [:attrs :property]) "og:") meta-tags))

(defn og:image [og-tags] (let [og:image-tag (filter #(str/starts-with? (get-in % [:attrs :property]) "og:image") og-tags)]
                (if og:image-tag (get-in (first og:image-tag) [:attrs :content]) "")))

(defn get-og:image
  "Get opengraph image"
  [url]
  (-> url
      fetch-url
      meta-tags
      og-tags
      og:image))

(defn get-og:images
  "Get opengraph image for each in map"
  [coll]
  (map get-og:image coll))

;; (get-og:images ["https://stackoverflow.com/questions/48621712/parse-html-in-enlive-like-in-beautifulsoup", 
;;                 "https://stackoverflow.com/questions/48621712/parse-html-in-enlive-like-in-beautifulsoup"])

;; (get-og:image "https://stackoverflow.com/questions/48621712/parse-html-in-enlive-like-in-beautifulsoup")

(def url "http://127.0.0.1:8000/")

(defn filter-hrefs
  "Get href from tag" 
  [tags]
  (map #(get-in % [:attrs :href]) tags))

(defn filter-external-links
  ""
  [hrefs]
  (filter #(str/starts-with? % "http") hrefs))

(defn get-all-links
  ""
  [url]
  (-> url
      fetch-url
      (html/select [:a])
      flatten
      filter-hrefs
      filter-external-links))

(comment
  (-> url
      fetch-url
      (html/select [:a])
      flatten
      filter-hrefs
      filter-external-links
      distinct
      get-og:images)
  )

  (-> url
      get-all-links
      get-og:images)
  (print "end")

(comment 
  (-> url
    get-all-links
    build-screenshots!))
