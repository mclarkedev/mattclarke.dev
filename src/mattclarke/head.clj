(ns mattclarke.head
  (:require [hiccup.core :refer [html]]))

(def gc
  "Global Content"
  (let [author "Matt Clarke"
        desc "Product lead with a background in art, design and engineering."]
    {:write-dir "public"
    ;;  :css-path "/css/global.css"
     :site-url "https://mattclarke.dev"
     :author author
     :title author
     :description desc
     :og:image "image.png"
     :favicon.ico "/favicon.ico" ;; TODO: add ico
     :favicon.svg "/favicon.svg" ;; TODO: add svg
     :favicon.png "/favicon.png"
     :apple-touch-icon "/apple-touch-icon.png"}))

(defn index-open-graph []
  (html [:meta {:property "og:title", :content (gc :title)}]
        [:meta {:property "og:type", :content "website"}]
        [:meta {:property "og:url", :content (gc :site-url)}]
        [:meta {:property "og:description", :content (gc :description)}]
        [:meta {:property "og:image", :content (gc :og:image)}]))

(defn global-head
  "Global head for every page"
  []
  "<style>
@import url('https://fonts.googleapis.com/css2?family=IBM+Plex+Serif:wght@400;700&display=swap');
</style>"
  (html [:meta {:charset "utf-8"}]
        [:meta {:name "viewport", :content "width=device-width, initial-scale=1"}]
        [:link {:rel "icon", :href (gc :favicon.png), :type "image/png"}]
        ;; [:link {:rel "icon", :href (gc :favicon.ico)}]
        ;; [:link {:rel "icon", :href (gc :favicon.svg), :type "image/svg+xml"}]
        [:link {:rel "apple-touch-icon", :href (gc :apple-touch-icon)}]
        [:link {:type "text/css", :href "/css/global.css", :rel "stylesheet"}]))

(defn make-index-head
  "Make index head from gc, global content"
  []
  (html [:head
              (global-head)
              [:title (gc :title)]
              [:meta {:name "description", :content (gc :description)}]
              [:meta {:name "author", :content (gc :author)}]
              (index-open-graph)
              [:link {:type "text/css", :href (gc :css-path), :rel "stylesheet"}]
              ]))

(defn make-page-head
  "Make html head from markdown page data."
  [metadata]
  (html [:head
              (global-head)
              [:title (first (metadata :title))]
            ;;   [:meta {:name "description", :content (gc :description)}]
            ;;   [:meta {:name "author", :content (gc :author)}]
            ;;   (open-graph)
            ;;   [:link {:type "text/css", :href (gc :css-path), :rel "stylesheet"}]
              ]))
