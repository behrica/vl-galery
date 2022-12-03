(ns convert
  #:nextjournal.clerk{:visibility {:code :hide, :result :hide}}
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [nextjournal.clerk :as clerk]
   [puget.printer :as puget]))

(comment
  (clerk/serve! {:browse true})
  (clerk/clear-cache!)
  :ok)



(def vl-examples (-> (io/as-url  "https://raw.githubusercontent.com/vega/vega-lite/next/site/_data/examples.json")
                     clojure.java.io/reader
                     (json/parse-stream keyword)))



(def vl-infos
  (->> (for  [[k-1 v-1] vl-examples [k-2 v-2] v-1]

         (map (fn [m]
                (assoc m
                       :level-1 k-1
                       :level-2 k-2))
              v-2))
       (flatten)))
       

(defn collect-info [vl-info]
  (let [
        vl-spec (slurp (format  "vl-specs/%s.vl.json" (:name vl-info)))
        edn-spec (puget/pprint-str
                  (json/parse-string vl-spec keyword)
                  {:map-delimiter ""})
        img-file-url (format  "https://github.com/vega/vega-lite/raw/next/examples/compiled/%s.png" (:name vl-info))]
    (assoc vl-info
           :img-file-url img-file-url
           :vl-spec vl-spec
           :edn-spec edn-spec)))
           


(defn info->hiccup [collected-info]
  (def collected-info collected-info)
  [:div
   [:a {:id (format "%s" (:name collected-info))}]
   [:h3  (:title collected-info)]
   [:p (:description collected-info)]
   [:img {:src  (:img-file-url collected-info)}]
   [:a {:href (format  "https://vega.github.io/editor/#/examples/vega-lite/%s" (:name collected-info))}
    "View this example in the online editor"]
   [:h5 "edn"]
   [:div (clerk/code  (:edn-spec collected-info))]])

(defn make-td [vl-info]
  [:td
   [:a {:href (format "#%s" (:name vl-info))}
    (:title vl-info)]
   [:a {:href (format "#%s" (:name vl-info))}
    [
     :img {:src (:img-file-url vl-info)
           :width "100px"
           :height "100px"}]]])
   

;; # vega-lite example Gallery in EDN

^{:nextjournal.clerk/visibility {:code :hide
                                 :result :show}
  :nextjournal.clerk/viewer :html}
(apply vector :table (map
                      (fn [infos]
                        (def infos infos)
                        [:tr {:height "100px"}
                              

                         (make-td (first infos))
                         (make-td (second infos))
                         (make-td (get (vec infos) 2))])

                       
                      (->> vl-infos
                           (remove #(contains? #{"bar_count_minimap" "geo_trellis"}
                                               (:name %)))
                           (map collect-info)
                           (partition-all 3))))
                                     

^{:nextjournal.clerk/visibility {:code :hide
                                 :result :show}
  :nextjournal.clerk/viewer :html}
(apply vector :div (map
                    #(-> % collect-info info->hiccup)

                    vl-infos))
