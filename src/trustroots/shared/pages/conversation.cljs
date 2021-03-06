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
        emailHash (:emailHash user)]

   (case (:avatarSource user)
     "local" (str
              "https://www.trustroots.org/modules/users/img/profile/uploads/"
              id
              "/avatar/32.jpg")
     "gravatar" (str "https://www.gravatar.com/avatar/" emailHash)
     nil)))

(defn list-view-item [message]
  (let [is-someone-else  (:is-from-someone-else message)
        is-me (not (:is-from-someone-else message))
        bubble-bg (if is-me "#eee" "#d2ece4")]
    (do
      ^{:key (:_id message)}
      [view
       {
        :style {
                ;:background-color "red"
                :flex -1
                :flex-direction "row"
                :align-items "flex-start"
                ;;:justify-content "flex-start"
                ;;:flex-basis 10
                :margin-top 10}}


       (when is-me [view {:style {:flex 4 :height 0 :background-color "red"}}])
       [view
        {:style
         {:flex 6
          :background-color bubble-bg
          :border-radius 10
          :padding 10}}


        [ui/html-view {:value (get-in message [:content])}]]
       (when is-someone-else [view {:style {:flex 4 :height 0 :background-color "blue"}}])])))





(defn conversation-page [{style :style}]
  (let [messages (subscribe [:current-conversation])
        target-user (subscribe [:get-user-of-current-conversation])
        message-draft (subscribe [:message/get-draft-with])]

    (fn []
      [view {:style {:flex-direction "column"
                     :margin 20
                     :flex 1
                     :justify-content "flex-end"
                     :align-items "stretch"}}
       [list-view-with-subscription messages list-view-item "Conversation"]
       [view {:style
              {
               :height 50
               :background-color "#ddd"
               :align-self "flex-end"
               :flex-direction "row"}}

        ^{:key "msg-field"}
        [ui/input {:style { :flex 1
                            :padding-horizontal 10
                            :align-self "flex-start"}
                   :value @message-draft
                   :on-change (fn [value]
                                (dispatch [:message/update-draft-with
                                           @target-user (-> value
                                                           .-nativeEvent
                                                           .-text)]))
                   :on-submit-editing (fn [value]
                                       (re-frame.core/dispatch
                                        [:message/send-to @target-user]))}]]])))
                  ;  :underlineColorAndroid "transparent"}]]])))
