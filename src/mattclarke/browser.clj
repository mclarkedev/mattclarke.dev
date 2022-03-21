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
    (println (str "Saved to " save-path))))

(defn cached-screenshot!
  "Visit the link and snap a screenshot"
  [url]
  (let [save-path (str build-output (encode url) ".png")
        img-exists? (.exists (io/file save-path))]
    (if img-exists?
      (println (str "Cached at " save-path))
      (screenshot! url save-path))))

(defn build-screenshots!
  "Build screenshot images in target from urls"
  [urls]
  (.mkdirs (io/file build-output))
  (println urls)
  (dorun (map cached-screenshot! urls)))

(comment
  (build-screenshots! urls))