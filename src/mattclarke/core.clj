(ns mattclarke.core
  (:require [markdown.core :refer [md-to-html-string-with-meta]]
            [clojure.java.io :as io]
            ;; [clojure.string :as str]
            [me.raynes.fs :refer [copy-dir-into]]
            [hiccup.core :refer [html]]
            [clojure.data.csv :as csv]

            [mattclarke.utils :refer [remove-ext str=> get-files]]
            [mattclarke.head :refer [make-page-head make-index-head]]))

(def build-config
  {:input-md-from "resources/markdown/published/"
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
  (html [:div.menu (make-links md-data)]))

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
    [:h5 "☉ Case Studies"]  
    [:div.index (make-table md-data)]]
   [:div.resources
    [:h5 "☉ Resources"]
    [:div]
    [:div (make-resource-table)]]
   [:div.bio
    [:h5 "☉ Bio"]
    [:div [:p "Matt Clarke is a product designer and developer based in Brooklyn, NY. "]]]
   [:div.writing
    [:h5 "☉ Currently"]
    [:div [:p "Building Uni, a character lookup tool for designers and developers."]]]
   [:div
    [:h5 "☉ Previously"]
    [:div [:p "Arthur, Datavore Labs, Splashlight, Gagosian Gallery, Vice Media, New Museum of Contemporary Art"]]]))

(defn make-index-page-data
  "Make index page data from our md-data"
  [md-data]
  {:html-head (make-index-head)
   :html-body (make-index-body md-data)
   :html-write-path (str (build-config :output-html-to) "index.html")})

(def today (.format (java.text.SimpleDateFormat. "© MM dd yyyy")
                    (new java.util.Date)))

(defn make-index-footer
  "Nake index footer"
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
  (time (run!! {:args ""})))
