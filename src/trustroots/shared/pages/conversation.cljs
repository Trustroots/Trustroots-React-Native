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
      [view {:style {:flex-direction "column"
                     :margin 20
                     :align-items "stretch"}}
       [ui/text "Conversation"]
       (for [message @messages
             :let [is-someone-else  (:is-from-someone-else message)
                   is-me (not (:is-from-someone-else message))
                   bubble-bg (if is-me "#CCF" "#DDD")]
             ]
         (do
           ^{:key (:_id message)}
           [view
            {
             :style {
                     ; :background-color "red"
                     :flex -1
                     :flex-direction "row"
                     :align-items "flex-start"
                     ;;:justify-content "flex-start"
                     ;;:flex-basis 10
                     :margin-top 10
                     }
             }
            (when is-me [view {:style {:flex 4 :height 0 :background-color "red"}}])
            [view
             {:style
              {:flex 6
               :background-color bubble-bg
               :border-radius 10
               :padding 10
               }
              }
             [ui/html-view {:value (get-in message [:content])}]]
            (when is-someone-else [view {:style {:flex 4 :height 0 :background-color "blue"}}])
            ]
           )
         )         
       [ui/button { :text "Logout"

                    :value "logout"
                    :raised true
                    :on-press #(dispatch [:logout])}]
      ]))) 


