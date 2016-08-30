(ns trustroots.shared.pages.messages
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [trustroots.shared.ui :as ui :refer
             [view text image logo-img h1]]
            [trustroots.shared.listview :refer
             [list-view-with-subscription]]
            [trustroots.helpers :refer [log info debug]]))

(defn list-view-item [row]
  (log row)
  [view {:flex 1 :flex-direction "row"}
   [text (get-in row [:message :excerpt])]])

(defn messages-page [{style :style}]
  (let [messages (subscribe [:get-messages])]
    (fn []
      [view {:style { :flex-direction "column"
                      :margin 40
                      :align-items "center"}}
       [list-view-with-subscription messages list-view-item "Messages"]
       [ui/button { :text "Refresh"
                    :value "refresh"
                    :raised true
                    :on-press #(dispatch [:messages/fetch])}]
       [ui/button { :text "Logout"
                    :value "logout"
                    :raised true
                    :on-press #(dispatch [:logout])}]
      ])))



