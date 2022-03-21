(ns mattclarke.core
  (:require [markdown.core :refer [md-to-html-string-with-meta]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [me.raynes.fs :refer [copy-dir-into]]
            [hiccup.core :refer [html]]
            [mattclarke.utils :refer [remove-ext str=> get-files encode]]
            [mattclarke.head :refer [make-page-head make-index-head]]))

(def build-config
  {:input-md-from "resources/markdown/"
   :input-links-from "resources/links.md"
   :input-assets-from "resources/public/"
   :output-html-to "target/public/"
   :output-assets-to "target/public/"})

(defn make-markdown-data
  "Return helper data for f (a markdown Java File) to be exported as html."
  [f]
  (print f)
  (let [basename (remove-ext (.getName f))
        path (str f)
        html-name (str basename ".html")
        md-with-meta (md-to-html-string-with-meta (slurp path))
        md-meta (md-with-meta :metadata)]
    {:md-name (.getName f)
     :basename basename
     :title (first (md-meta :title))
     :html-name html-name
     :html-write-path (str (build-config :output-html-to) html-name)
     :path path
     :html-body (html [:article (md-with-meta :html)])
     :html-head (make-page-head md-meta)}))

;; (defn make-external-links []
;;   (:html (md-to-html-string-with-meta (slurp (str (io/file (build-config :input-links-from)))))))

(defn get-external-links []
  (-> "./resources/links.txt"
      slurp
      (str/replace #"\d" "")
      str/split-lines))

(defn make-external-links2 []
  (map #(html [:a {:href %} [:img {:src (str "/images/screenshots/" (encode %) ".png")}]]) (get-external-links)))

(defn get-markdown-data
  "Returns markdown data from dir (a directory) of .md files"
  []
  (map make-markdown-data (get-files (build-config :input-md-from))))

;; (def make-tooltip [:span [:img {:src "https://www.scriptol.com/images/apache.png" :width "1s00px"}]
;;                    [:h3 "Link"]])

(defn make-link
  "Make a link from a md data item"
  [md]
  (html [:a.link.tooltip {:href (md :html-name)} (md :title)
        ;;  make-tooltip
         ]))

(defn make-links
  "Make links from md-data"
  [md-data]
  (apply str (map make-link md-data)))

;; (defn make-link-dot
;;   "Make dot"
;;   [md]
;;   (html [:a.link.dot {:href (md :html-name)}]))

;; (defn make-learning-table)

(defn make-row 
  "Make a row"
  [md]
  [:tr 
  ;;  [:td (make-link-dot md)]
   [:td (make-link md)] 
  ;;  [:td (md :html-name)]
   ])

(defn make-table 
  "Make a table"
  [data]
  (html [:table [:tbody (map make-row data)]]))

(defn make-header
  "Make header"
  []
  [:div.header [:a {:href "/"} "Matthew Clarke"]])

(defn make-menu
  "Make menu html from links from our md-data"
  [md-data]
  [:div.menu (make-links md-data)])

(defn make-nav
  "Build navigation from md-data"
  [md-data]
  (html (make-header)
        (make-menu md-data)))

(defn make-index-body
  "Make index page body"
  [md-data]
  (html [:div.writing
         [:h5 "Writing"]
         [:div.index (make-table md-data)]]
        ;; [:div.resources
        ;;  [:h5 "Resources"]
        ;;  [:div (get-external-links)]]
        ))

(defn make-index-page-data 
  "Make index page data from our md-data" 
  [md-data]
  {:html-head (make-index-head)
   :html-body (make-index-body md-data)
   :html-write-path (str (build-config :output-html-to) "index.html")})

(defn make-footer
  "Make index footer"
  [md-data]
  (html [:article (make-index-body md-data)]))

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
    (write-page! md nav (make-footer md-data))
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
   {:index (write-page! (make-index-page-data md-data) (make-header) "") ;; Index only has header
    :md (write-pages! md-data (make-nav md-data))
    :assets (copy-assets!)}))

(defn run!!
  "Run our build process"
  [_]
  (time (build-site! (get-markdown-data))))

(comment
  (time (run!! {:args ""})))
