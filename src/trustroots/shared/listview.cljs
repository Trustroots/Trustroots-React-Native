(ns trustroots.shared.listview
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [trustroots.shared.ui :as ui]
            [trustroots.helpers :refer [log info debug]]))

(def ReactNative (js/require "react-native"))

(defn default-row-has-changed [x y]
  (let [row-1 (js->clj x :keywordize-keys true) row-2 (js->clj y :keywordize-keys true)]
    (not= row-1 row-2)))


(defn get-data-source [& {:keys [row-has-changed] :or {row-has-changed default-row-has-changed}}]
  (ReactNative.ListView.DataSource. (clj->js {:rowHasChanged row-has-changed})))

(defn get-row-component [list-item-view]
  (r/reactify-component
   (fn[props]
     (log "row-component" props)
     (let [row (->
                (props :row)
                (js->clj :keywordize-keys true))]
       [list-item-view row]))))

(defn set-rows [data]
  (clj->js data))

(def list-view (r/adapt-react-class (.-ListView ReactNative)))

(defn list-view-with-subscription [row-subscription list-item-view title]
  (let [data-source (get-data-source)]
    (fn []
      [list-view
       {
        :dataSource (.cloneWithRows data-source (clj->js @row-subscription))
        :render-row (fn[row]
                      (r/create-element
                       (get-row-component list-item-view) #js{:row row}))
        :render-section-header (fn [] (r/as-element [ui/h1 title]))
        :enableEmptySections true
        }
       ])))
