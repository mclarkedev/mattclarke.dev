(ns dev.mattclarke.core
  (:require [markdown.core :refer [md-to-html-string]]))

(defn build []
  (md-to-html-string
   "# This is a test\nsome code follows\n```clojure\n(defn foo [])\n```"))

(build)
