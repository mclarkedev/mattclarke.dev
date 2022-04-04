(ns mattclarke.core
  (:require [markdown.core :refer [md-to-html-string-with-meta]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [me.raynes.fs :refer [copy-dir-into]]
            [hiccup.core :refer [html]]
            [net.cgrand.enlive-html :as enlive-html]
            [clojure.data.csv :as csv]

            [mattclarke.utils :refer [remove-ext str=> get-files]]
            [mattclarke.head :refer [make-page-head make-index-head]]))

(def build-config
  {:input-md-from "resources/markdown/studies/"
   :input-assets-from "resources/public/"
   :output-html-to "target/public/"
   :output-assets-to "target/public/"})

(defn make-video
  "Make an in-line mp4 video"
  [src]
  (html [:div.video
         [:video {:width "100%"
                  :loop ""
                  :preload ""
                  :class "video"}
          [:source {:src src :type "video/mp4"}]]]))

(defn md-media-transformer [text state]
  (println text)
  (let [img-nodes (enlive-html/select-nodes* (enlive-html/html-snippet text) [:img])
        img? (not-empty img-nodes)
        img-node (first img-nodes)
        img-src (if img? ((img-node :attrs) :src) "nil")
        video? (str/includes? img-src ".mp4")]
    (if video?
      [(make-video img-src) state]
      [text state])))

;; (defn node-to-text
;;   "Return text for node"
;;   [node]
;;   (let [tag (node :tag)
;;         attrs (node :attrs)
;;         content (node :content)]
;;     (html [tag attrs (remove nil?
;;                              (if (empty? content)
;;                                nil
;;                                (apply node-to-text (node :content))))])))

;; (defn nodes-to-text
;;   "Return text for nodes"
;;   [nodes]
;;   (mapv node-to-text nodes))

(defn node-to-hiccup
  "Return hiccup syntax for an enlive-html node"
  [node]
  (let [tag (node :tag)
        attrs (node :attrs)
        content (node :content)]
    [tag
     attrs
     (if (empty? content)
       ""
       (apply node-to-hiccup (node :content)))]))

(defn nodes-to-hiccup
  "Return text for nodes"
  [nodes]
  (map node-to-hiccup nodes))

;; (comment
;;   (def doc (enlive-html/html-snippet "<p><div style='text-align: center'><img src='/images/arthur1.png' width='100p' style='width: 420px;' /></div></p><div class=\"video\"><video autoplay=\"\" class=\"video\" controls=\"true\" loop=\"\" preload=\"\" width=\"100%\"><source src=\"/videos/liminal.mp4\" type=\"video/mp4\" /></video></div></p><p>I started Liminal in 2020 to organize my research topics and daily media consumption. It is currently in development in close collaboration with family and friends. Responsibilities.</p><pre><code># Images\n!&#91;Video&#93;&#40;/videos/liminal.mp4\n&quot;Simon Deny, ‘New Management’ &#40;installation detail view&#41;, 2014&quot;&#41;\n\n</code></pre><p>Liminal is a personal media library for the web. Designed for creatives, researchers, and curious people, it’s mission is to make researching and collecting the web more reliable, secure, and extensible.</p><p>With core feature support for RSS feeds, YouTube media activity, bookmarking, and browser history storage, Liminal provides complete data access to a single timeline of your media consumption––allowing you to track a single topic across a range of sources. Built-in knowledge services, such as a structured data inspector and Wikipedia API, allow you to quickly store and discover commonly known people, places, and things.</p><p>Product concept, mobile interface design, desktop interface design, and cross-platform full-stack development.</p>"))

;;   (def img (enlive-html/select doc [:video]))

;;   (map #(html %) (nodes-to-hiccup img))

;;   (md-to-html-string-with-meta (slurp "resources/markdown/published/liminal.md")
;;                                :custom-transformers [md-media-transformer]))

(defn make-markdown-data
  "Return helper data for f (a markdown Java File) to be exported as html."
  [f]
  (print f)
  (let [basename (remove-ext (.getName f))
        path (str f)
        html-name (str basename ".html")
        md-with-meta (md-to-html-string-with-meta (slurp path) :custom-transformers [md-media-transformer])
        md-meta (md-with-meta :metadata)
        md-html (md-with-meta :html)
        md-imgs (enlive-html/select (enlive-html/html-snippet md-html) [:img])
        md-videos (enlive-html/select (enlive-html/html-snippet md-html) [:video])
        media-hiccup (nodes-to-hiccup (concat md-videos md-imgs))]
    {:md-name (.getName f)
     :basename basename
     :title (first (md-meta :title))
     :html-name html-name
     :html-write-path (str (build-config :output-html-to) html-name)
     :path path
     :html-body (html [:article
                       [:div.media media-hiccup]
                       md-html])
     :html-head (make-page-head md-meta)
     :media-hiccup media-hiccup}))

(defn get-csv
  "Get csv header and body for path"
  [path]
  (let [csv (csv/read-csv (slurp path))
        header (first csv)
        body (rest csv)] {:header header :body body}))

(defn get-resources
  "Get resources"
  []
  (:body (get-csv "resources/resources.csv")))

(defn get-markdown-data
  "Returns markdown data from dir (a directory) of .md files"
  []
  (map make-markdown-data (get-files (build-config :input-md-from))))

(defn make-resource-table
  "Fetch and make resource table from resources csv data"
  []
  (html
   [:table
    [:tbody
     (map #(html
            [:tr
             [:td [:a {:href (nth % 1) :target "_blank"} (nth % 0)]]
             [:td {:style "padding-right: 24px"} "⮥"]
             [:td [:a {:href (nth % 1) :target "_blank"} (nth % 2)]]])
          (get-resources))]]))

(defn make-media-table
  "Make media gallery from :media-hiccup md-data"
  [md-media]
  (html [:div.media (map #(% :media-hiccup) md-media)]))

(defn make-link
  "Make a link from a md data item"
  [md]
  (html [:a.link.tooltip {:href (md :html-name)} (md :title)]))

(defn make-links
  "Make links from md-data"
  [md-data]
  (apply str (map make-link md-data)))

(defn make-row
  "Make a row"
  [md]
  (html [:tr
         [:td (make-link md)]]))

(defn make-table
  "Make a table"
  [data]
  (html [:table [:tbody (map make-row data)]]))

(defn make-header
  "Make header"
  []
  [:header.header
   [:a {:href "/"} "Matt Clarke"]
   [:a {:href "/"} "⚿"]
   [:a {:href "/"} "Contact"]])

(defn make-menu
  "Make menu html from links from our md-data"
  [md-data]
  (html [:div.menu
         [:h5 {:title "Bit-size case studies of product features I've worked on."} "☉ Feature Studies"]
         (make-links md-data)]))

(defn make-nav
  "Build navigation from md-data"
  [md-data]
  (html (make-header)
        (make-menu md-data)))

(defn make-index-body
  "Make index page body"
  [md-data]
  (html
   [:div.writing
    [:h5 "☉ Media Gallery"]
    [:div.index (make-media-table md-data)]]
   [:div
    [:h5 {:title "Bit-size case studies of product features I've worked on."} "☉ Feature Studies"]
    [:div.index (make-table md-data)]]
   [:div
    [:h5 "☉ Links and Resources"]
    [:div.index (make-resource-table)]]
   [:div.bio
    [:h5 "☉ Bio"]
    [:div {:style "max-width: 550px"}
     [:span "Matthew Clarke is a product designer and developer based in Brooklyn, NY. "]
     [:span "Currently, he's building a character lookup tool for designers and developers, called Uni. "]
     [:span "Previously, worked on digital products at Arthur, Datavore Labs, and Splashlight, and in media and the arts at Gagosian Gallery, Vice Media, and New Museum of Contemporary Art"]]]))

(defn make-index-page-data
  "Make index page data from our md-data"
  [md-data]
  {:html-head (make-index-head)
   :html-body (make-index-body md-data)
   :html-write-path (str (build-config :output-html-to) "index.html")})

(def today
  (.format (java.text.SimpleDateFormat. "© MM dd yyyy")
           (new java.util.Date)))

(defn make-index-footer
  "Make index footer"
  []
  (html [:footer
         [:div ""]
         [:div ""]
         [:div today]]))

(defn make-page-footer
  "Make index footer"
  [md-data]
  (html [:article (make-index-body md-data)]
        [:footer 
         [:div [:button
                {:onclick "history.back()"} "←"]]
         [:div ""]
         [:div today]]))

(defn stitch-html
  "Stitch head, nav, and body into main template."
  [head nav body footer]
  (html [:html
         head
         [:body
          nav
          [:main body
           [:div footer]]]]))

(defn write-page!
  "Writes to :html-write-path and joins page head, body, and nav"
  [md nav footer]
  (let [html (stitch-html (md :html-head) nav (md :html-body) footer)]
    (spit (md :html-write-path) html) html))

(defn write-pages!
  "Writes html to dir for each page in our markdown data"
  [md-data nav]
  (doseq [md md-data]
    (write-page! md nav (make-page-footer md-data))
    md)
  md-data)

(defn copy-assets!
  "Copy public assets from resource to target"
  []
  (let [from (build-config :input-assets-from)
        to  (build-config :output-assets-to)]
    (copy-dir-into
     from
     to)
    (str=> from to)))

(defn build-site!
  "Builds the site from our transformed md-data"
  [md-data]
  (.mkdirs (io/file (build-config :output-html-to)))
  (print
   {:index (write-page! (make-index-page-data md-data) (make-header) (make-index-footer)) ;; Index only has header
    :md (write-pages! md-data (make-nav md-data))
    :assets (copy-assets!)}))

(defn run!!
  "Run our build process"
  [_]
  (time (build-site! (get-markdown-data))))

(comment
  (println (get-markdown-data))
  (time (run!! {:args ""})))
