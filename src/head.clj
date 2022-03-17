(ns dev.mattclarke.head
  (:require [hiccup.core :as html]))

(def gc
  "Global Content"
  (let [author "Matthew Clarke"
        desc "Product lead with a background in art, design and engineering."]
    {:write-dir "public"
     :css-path "/css/global.css"
     :site-url "https://matthewclarke.dev"
     :author author
     :title (str author " - 2022")
     :description desc
     :og:image "image.png"
     :favicon.ico "/favicon.ico"
     :favicon.svg "/favicon.svg"
     :apple-touch-icon "/apple-touch-icon.png"}))

(defn index-open-graph []
  [:meta {:property "og:title", :content (gc :title)}]
  [:meta {:property "og:type", :content "website"}]
  [:meta {:property "og:url", :content (gc :site-url)}]
  [:meta {:property "og:description", :content (gc :description)}]
  [:meta {:property "og:image", :content (gc :og:image)}])

(defn global-icon-links []
  [:link {:rel "icon", :href (gc :favicon.ico)}]
  [:link {:rel "icon", :href (gc :favicon.svg), :type "image/svg+xml"}]
  [:link {:rel "apple-touch-icon", :href (gc :apple-touch-icon)}])

(defn global-fonts [] "<style>
@import url('https://fonts.googleapis.com/css2?family=Titillium+Web:wght@300;600&display=swap');
</style>")

(defn global-technical []
  [:meta {:charset "utf-8"}]
  [:meta {:name "viewport", :content "width=device-width, initial-scale=1"}])

(defn make-index-head
  "Make index head from gc, global content"
  []
  (html/html [:head
              (global-fonts)
              (global-technical)
              (global-icon-links)
              [:title (gc :title)]
              [:meta {:name "description", :content (gc :description)}]
              [:meta {:name "author", :content (gc :author)}]
              (index-open-graph)
              [:link {:type "text/css", :href (gc :css-path), :rel "stylesheet"}]
              ]))

(defn make-page-head
  "Make html head from markdown page data."
  [metadata]
  (html/html [:head
              (global-fonts)
              (global-technical)
              (global-icon-links)
              [:title (first (metadata :title))]
            ;;   [:meta {:name "description", :content (gc :description)}]
            ;;   [:meta {:name "author", :content (gc :author)}]
            ;;   (open-graph)
            ;;   [:link {:type "text/css", :href (gc :css-path), :rel "stylesheet"}]
              ]))

;; (make-global-head)