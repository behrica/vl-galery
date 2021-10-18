(ns convert
  (:require [cheshire.core :as json]
            [puget.printer :as puget]
            [notespace.api :as note]
            [notespace.kinds :as k]
            [clojure.java.io :as io]))

(comment
  (note/init-with-browser)
  (note/update-config (fn [config] (assoc config :render-src? false)))
  (note/eval-and-realize-this-notespace)
  (note/render-static-html)
  :ok)



(defn copy [uri file]

  (with-open [in  (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(def vl-examples (-> "../vega-lite/site/_data/examples.json"
                     clojure.java.io/reader
                     (json/parse-stream keyword)))

(def vl-infos
  (flatten
   (for  [[k-1 v-1] vl-examples [k-2 v-2] v-1]

     (map (fn [m]
            (assoc m
                   :level-1 k-1
                   :level-2 k-2))
         v-2))))





(defn collect-info [vl-info]
  (let [
        _ (copy (format "../vega-lite/examples/compiled/%s.png" (:name vl-info))
                (note/file-target-path (format  "%s.png" (:name vl-info))))
        vl-spec (slurp (format  "../vega-lite/examples/specs/%s.vl.json" (:name vl-info)))
        edn-spec (puget/pprint-str
                  (json/parse-string vl-spec keyword)
                  {:map-delimiter ""})
        img-file-tag (note/img-file-tag (format "%s.png" (:name vl-info)) {})]

    (assoc vl-info
           :vl-spec vl-spec
           :edn-spec edn-spec
           :img-file-tag img-file-tag)))



(defn info->hiccup [collected-info]
  [:div
   [:h3 (:title collected-info)]

   [:p (:description collected-info)]
   [:div (:img-file-tag collected-info)]
   [:a {:href (format  "https://vega.github.io/editor/#/examples/vega-lite/%s" (:name collected-info))}
    "View this example in the online editor"]
   [:h5 "JSON"]
   [:div (k/wrap :p/code (:vl-spec collected-info))]
   [:h5 "EDN"]
   [:div (k/wrap :p/code (:edn-spec collected-info))]])

^k/hiccup
[:div (map
       #(-> % collect-info info->hiccup)
       ;; take 5
       vl-infos)]
