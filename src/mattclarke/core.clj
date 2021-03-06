(ns mattclarke.core
  (:require [markdown.core :refer [md-to-html-string-with-meta]]
            ;; [clojure.java.io :as io]
            [clojure.string :as str]
            [me.raynes.fs :refer [copy-dir-into]]
            [hiccup.core :refer [html]]
            [net.cgrand.enlive-html :as enlive-html]
            [clojure.data.csv :as csv]
            [clojure.inspector :as inspector]

            [mattclarke.utils :refer [remove-ext str=> get-files]]
            [mattclarke.head :refer [make-page-head make-index-head]]
            ;; [mattclarke.opengraph :refer [url->og:image url->og]]
            [mattclarke.browser :refer [url->screenshot!]]))

(def build-config
  {:input-md-from "resources/markdown/studies/"
   :input-assets-from "resources/public/"
   :output-html-to "target/public/"
   :output-assets-to "target/public/"})

(defn make-video
  "Make an in-line mp4 video from src link"
  [src]
  (html [:div.video
         [:video {:width "100%"
                  :autoplay "true"
                  :loop ""
                  :preload ""
                  :class "video"}
          [:source {:src src :type (str "video/mp4")}]]]))

(defn md-media-transformer [text state]
  (let [img-nodes (enlive-html/select-nodes* (enlive-html/html-snippet text) [:img])
        img? (not-empty img-nodes)
        img-node (first img-nodes)
        img-src (if img? ((img-node :attrs) :src) "nil")
        video? (or (str/includes? img-src ".mp4") (str/includes? img-src "mov"))]
    (if video?
      [(make-video img-src) state]
      [text state])))

(defn node-to-hiccup
  "Return hiccup syntax for an enlive-html node, recurssively through :content."
  [node]
  (let [tag (node :tag)
        attrs (node :attrs)
        content (node :content)]
    [tag
     attrs
     (if (empty? content)
       "" ;; Empty strings deconstruct into hiccup html
       (apply node-to-hiccup content))]))

(defn nodes-to-hiccup
  "Return text for nodes"
  [nodes]
  (map node-to-hiccup nodes))

(defn transform-link [link-node]
  {:tag (link-node :tag)
   :attrs (merge (link-node :attrs)
                 {:target "_blank"
                  :class "link tooltip"})
   :content (link-node :content)})

(defn run-if-map
  "Ruturn the result of (func x) if x is a map, otherwise return x"
  [x func]
  (if (map? x)
    (func x)
    x ;; passthrough
    ))

(defn transform-content-with-links
  "Return in-line content with its in-line tags transformed by transform-link"
  [content]
  (map #(run-if-map % transform-link) content))

(defn md-link-transformer
  [text state]
  (let [nodes (enlive-html/select (enlive-html/html-snippet text) [:p])
        node (first nodes)
        a-nodes (enlive-html/select nodes [:a])
        includes-a? (not-empty a-nodes)]
    (if includes-a?
      [(html [:p (map #(if (string? %)
                         %
                         (if (map? %)
                           [(% :tag)
                            (% :attrs)
                            (% :content)
                            [:span
                             {:class "tooltip-content"}
                             [:img
                              {:src (url->screenshot! ((% :attrs) :href)) :width "300"}]]]
                           "not map"))
                      (transform-content-with-links (node :content)))])
       state]
      [text state] ;; noop
      )))

(defn make-markdown-data
  "Return helper data for f (a markdown Java File) to be exported as html."
  [f]
  (let [basename (remove-ext (.getName f))
        path (str f)
        html-name (str basename ".html")
        md-with-meta (md-to-html-string-with-meta (slurp path)
                                                  :custom-transformers
                                                  [md-media-transformer md-link-transformer])
        md-meta (md-with-meta :metadata)
        md-html (md-with-meta :html)
        md-imgs (enlive-html/select (enlive-html/html-snippet md-html) [[:img (enlive-html/attr? :src)]])  ;; [[:tag match attribute inside :tag]] https://github.com/cgrand/enlive
        md-videos (enlive-html/select (enlive-html/html-snippet md-html) [:video])
        media-hiccup (nodes-to-hiccup (concat md-imgs md-videos))]
    {:md-name (.getName f)
     :basename basename
     :title (first (md-meta :title))
     :html-name html-name
     :html-write-path (str (build-config :output-html-to) html-name)
     :path path
     :html-body (html [:article
                      ;;  [:div.media media-hiccup] ;; Media
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
             [:td {:style "padding-right: 24px"} "???"]
             [:td [:a {:href (nth % 1) :target "_blank"} (nth % 2)]]])
          (get-resources))]]))

(defn make-cv-table
  "Fetch and make resource table from resources csv data"
  []
  (html
   [:table
    [:tbody
     (map #(html
            [:tr
             [:td [:a {:href (nth % 1) :target "_blank"} (nth % 0)]]
             [:td [:a {:href (nth % 1) :target "_blank"} (nth % 1)]]])
          (:body (get-csv "resources/cv.csv")))]]))

(defn make-media-section
  "Make media section form :media-hiccup md-data"
  [md]
  [:a.media-section
   {:href (md :html-name)}
   (md :media-hiccup)])

(defn make-media-table
  "Make media gallery from :media-hiccup md-data"
  [md-media]
  (html [:div.media (map make-media-section md-media)]))

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
   [:a {:href "/"} "???"]
   [:a {:href "/"} "Contact"]])

(defn make-menu
  "Make menu html from links from our md-data"
  [md-data]
  (html [:div.menu
        ;;  [:h5 "??? Writing"]
         (make-links md-data)]))

(defn make-index-body
  "Make index page body"
  [md-data]
  (html
   [:div.writing
    [:h5 "??? Media Gallery"]
    [:div.index (make-media-table md-data)]]
   [:div
    [:h5 "??? Writing"]
    [:div.index (make-table md-data)]]
   [:div
    [:h5 "??? CV"]
    [:div.index (make-cv-table)]]
   [:div
    [:h5 "??? Links and Resources"]
    [:div.index (make-resource-table)]]
   [:div.bio
    [:h5 "??? Bio"]
    [:div
     [:span "Matthew Clarke is a product designer and developer based in Brooklyn, NY. "]
     [:span "He's worked on digital products at Arthur, Datavore Labs, and Splashlight, and in media at Gagosian Gallery, Vice Media, and New Museum of Contemporary Art"]]]))

(defn make-index-page-data
  "Make index page data from our md-data"
  [md-data]
  {:html-head (make-index-head)
   :html-body (make-index-body md-data)
   :html-write-path (str (build-config :output-html-to) "index.html")})

(def today
  (.format (java.text.SimpleDateFormat. "?? MM dd yyyy")
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
                {:onclick "history.back()"} "???"]]
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

(defn template-md
  "Template markdown article using c{:head :header :menu :body :footer}"
  [c]
  (html [:html
         (c :head)
         [:body
          (c :header)
          [:main.centered ;; Centered
           [:div
            (c :menu) ;; Fixed
            (c :body)]
           (c :footer)]]]))

(defn write-page!
  "Writes to :html-write-path and joins page head, body, and nav"
  [md nav footer]
  (let [html (stitch-html (md :html-head) nav (md :html-body) footer)]
    (spit (md :html-write-path) html) html)
  md)

(defn copy-assets!
  "Copy public assets from resource to target"
  []
  (let [from (build-config :input-assets-from)
        to  (build-config :output-assets-to)]
    (copy-dir-into
     from
     to)
    (str=> from to)))

(defn make-index-page
  [md-data]
  (write-page! (make-index-page-data md-data) (make-header) (make-index-footer))) ;; Index only has header

(defn make-md-pages
  "Make a list of {:path :html} data from our markdown data, to be written out"
  [md-data]
  (map #(identity {:path (% :html-write-path)
                   :html (template-md {:head (% :html-head)
                                       :header (make-header)
                                       :menu (make-menu md-data)
                                       :body (% :html-body)
                                       :footer (make-page-footer md-data)})}) md-data))

(defn write-pages!
  "Writes page{:html} => page{:path} for pages"
  [pages]
  (run! #(spit (% :path) (% :html)) pages)
  pages)

(defn run!!
  "Run our build process"
  [_]
  (println "Assets copied: "  (copy-assets!))
  (make-index-page (get-markdown-data))
  (-> (get-markdown-data)
      make-md-pages
      write-pages!))

(comment
  (template-md {:head ""})
  (println (get-markdown-data))
  (inspector/inspect-tree (get-markdown-data))
  (time (run!! {:args ""})))
