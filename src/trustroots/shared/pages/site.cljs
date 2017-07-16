(ns trustroots.shared.pages.site
  (:require [trustroots.shared.ui :refer [view web-view]]))

(defn site-page []
  (fn []
    [view {:style
           {:flex-direction "column"
            :margin 0
            :flex 1
            :align-items "stretch"}}
     [web-view {:source {:uri "https://www.trustroots.org"}}]]))

