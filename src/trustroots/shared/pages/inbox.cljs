(ns trustroots.shared.pages.inbox
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [trustroots.shared.ui :as ui :refer
             [view text image logo-img h1]]
            [trustroots.shared.listview :refer
             [list-view-with-subscription]]
            [trustroots.helpers :refer [log info debug to-now]]))

(defn get-image-url [user]
  (let [source (:avatarSource user)
        id     (:_id user)
        emailHash (:emailHash user)]

   (case (:avatarSource user)
     "local" (str
              "https://www.trustroots.org/modules/users/img/profile/uploads/"
              id
              "/avatar/32.jpg")
     "gravatar" (str "https://www.gravatar.com/avatar/" emailHash)
     nil)))



(defn list-view-item [row]
  (log row)
  (let [partner-id    (get-in row [:discussion-with :_id])
        partner-name  (get-in row [:discussion-with :displayName])
        image-url     (get-image-url (:discussion-with row))
        excerpt       (get-in row [:message :excerpt])
        message-count (get-in row [:message :excerpt])
        read          (get-in row [:read])
        bg-color      (if read "white" "#AAAAFF")
        relative-time (to-now (:updated row))]


    ^{:key (:_id row)}
     [view {
            ;           :flex 1
            :flex -1
            ;           :flex-direction "row"
            :flex-direction "column"
            :background-color :yellow
            :margin 1}
      [ui/card-with-gravatar
       { :message excerpt
        :name partner-name
        :time relative-time
        :background-colpropsor bg-color
        :avatar-image image-url
        :on-press (fn []
                    (dispatch [:show/conversation-with partner-id]))}]]))



(defn inbox-page [{style :style}]
  (let [messages (subscribe [:inbox/get])]
    (fn []
      [view {:style { :flex-direction "column"
                     :margin 20
                     :flex 1
                     :align-items "stretch"}}
       [list-view-with-subscription messages list-view-item "Messages"]
       [ui/button { :text "Refresh"
                    :value "refresh"
                    :raised true
                    :on-press #(dispatch [:inbox/fetch])}]
       [ui/button { :text "Logout"
                    :value "logout"
                    :raised true
                    :on-press #(dispatch [:auth/logout])}]])))
