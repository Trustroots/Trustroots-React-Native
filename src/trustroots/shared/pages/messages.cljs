(ns trustroots.shared.pages.messages
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [trustroots.shared.ui :as ui :refer
             [view text image logo-img h1]]))

(defn messages-page [{style :style}]
  (let [greeting (subscribe [:get-greeting])]
    (fn []
      [view {:style {:flex-direction "column"
                     :margin 40
                     :align-items "center"}}
      [h1 "Messages"]
      [image {:source ui/logo-img
              :style  {:width 144 :height 144 :margin-bottom 30}}]

      [ui/button { :text "Logout"
                   :value "logout"
                   :raised true
                   :on-press #(dispatch [:logout])}]
      [ui/button { :text "Refresh"
                   :value "refresh"
                   :raised true
                   :on-press #(dispatch [:messages/fetch])}]

      ])))



