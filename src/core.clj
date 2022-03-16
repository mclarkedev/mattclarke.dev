(ns dev.mattclarke.core
  (:require [markdown.core :refer [md-to-html-string]]))

(defn build []
  (md-to-html-string
   (slurp "resources/markdown/bio.md")))

(build)
