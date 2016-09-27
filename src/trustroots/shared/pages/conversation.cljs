(ns trustroots.shared.pages.conversation
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [trustroots.shared.ui :as ui :refer
             [view text image logo-img h1]]
            [trustroots.shared.listview :refer
             [list-view-with-subscription]]
            [trustroots.helpers :refer [log info debug to-now]]))

(def html-view (r/adapt-react-class (get (js->clj (js/require "react-native-htmlview")) "default")))

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


(defn list-view-item [row]
  (log row)
  (let [excerpt  (get-in row [:message :excerpt])
        sender    (get-in row [:userFrom :displayName])
        image-url  (get-image-url (:userFrom row))
        message-count  (get-in row [:message :excerpt])
        read (get-in row [:read])
        bg-color (if read "white" "#AAAAFF")
        relative-time (to-now (:updated row))
        ]
  [view {:flex 1 :flex-direction "column"}
   [ui/card {:style {:background-color bg-color}}
    [ui/card-body
     [view {:style
            {:flex 1
             :flex-direction "row"
             :align-items "flex-start"
             ;:justifyContent "space-around"
             :margin 2
             }}
       [view
        [ui/trustroots-avatar image-url]
       ]
      [view {:style {:flex 1
                     :flex-direction "column"
                     :align-items "stretch"
                     :margin-left 10
                     }}
       [text {:style {:font-weight "bold"
                      :font-size 16 }}
        sender]
       [text excerpt ]]
      ]
     [view {:style {:height 20
                    :align-items "flex-end"
                    }}
      [view {:style {:flex 3
                    :flex-direction "row"
                    :align-items "center"
                    ;:background-color "red"
                     }}
      
       [ui/icon {:name "schedule"}]
       [text {:style{:margin-left 5} } relative-time]
       ]
      ]
     ]
    ]
   ]))

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
           [html-view {:value (get-in message [:content])}])
         )
       
       [ui/button { :text "Logout"
                    :value "logout"
                    :raised true
                    :on-press #(dispatch [:logout])}]
      ]))) 



