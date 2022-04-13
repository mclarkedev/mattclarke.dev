(ns mattclarke.browser
  (:require [etaoin.api :as web]
            [clojure.java.io :as io]
            [mattclarke.utils :refer [encode]]))

(def urls ["http://127.0.0.1:8000/" "http://127.0.0.1:8000/finding-art.html"])

(def build-output "target/public/images/screenshots/")

(defn screenshot!
  "Save screenshot for url to save-path"
  [url save-path]
  (let [driver (web/chrome {:size [1080 820]})]
    (doto driver
      (web/go url)
      (web/screenshot save-path)
      (web/quit))
    (println (str "Saved to " save-path)))
  save-path)

(defn cached-screenshot!
  "Visit the link and snap a screenshot"
  [url]
  (let [hash-name (str (encode url) ".png")
        save-path (str build-output hash-name)
        img-exists? (.exists (io/file save-path))]
    (if img-exists?
      (println (str "Cached at " save-path))
      (screenshot! url save-path))
    (str "images/screenshots/" hash-name)))

(defn build-screenshots!
  "Build screenshot images in target from urls"
  [urls]
  (.mkdirs (io/file build-output))
  (println urls)
  (dorun (map cached-screenshot! urls)))

(defn url->screenshot!
  "Takes screenshot of url, returns file path" 
  [url]
  (cached-screenshot! url))

(url->screenshot! "https://www.google.com")

(comment
  (build-screenshots! urls)
  )

;; Media Table ------------------------------------------------------------------------

;; (defn get-article-html "Get all article html text" []
;;   )

(comment
  ;;
  ;; (-> get-html
  ;;     get-media-from-html
  ;;     make-media-table
  ;;     append-el)
  ;;
  ;; (append-el (make-media-table (get-media-from-html (get-html))))
  )