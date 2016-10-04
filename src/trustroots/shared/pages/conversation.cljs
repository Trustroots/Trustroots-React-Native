(ns trustroots.shared.pages.conversation
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
        emailHash (:emailHash user)
        ]
  (case (:avatarSource user)
    "local" (str
             "https://www.trustroots.org/modules/users/img/profile/uploads/"
             id
             "/avatar/32.jpg")
    "gravatar" (str "https://www.gravatar.com/avatar/" emailHash) 
    nil )))

(defn conversation-page [{style :style}]
  (let [messages (subscribe [:current-conversation])]
    (fn []
      (log "conversation" @messages)
      [view {:style { :flex-direction "column"
                      :margin 20
                     :align-items "stretch"}}
       [ui/text "Conversation"]
       (for [message @messages]
         (do 
           ^{:key (:_id message)}
           [view {:style {:background-color "red"}}

            [ui/html-view {:value (get-in message [:content])}]

            ])
         )
       
       [ui/button { :text "Logout"
                    :value "logout"
                    :raised true
                    :on-press #(dispatch [:logout])}]
      ]))) 



