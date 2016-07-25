(ns trustroots.shared.pages.login
  (:require [reagent.core :as r ]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [trustroots.shared.ui :as ui :refer [view text image h1 p]]
            ))

(defonce fields (r/atom {:user nil :pwd nil}))

(defn error [error]
  (when error [ui/error-text error]))

(defn login-page [{style :style}]
  (let [auth-status (subscribe [:auth-status])]
    (fn []
      [view   {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [image {:source ui/logo-img
               :style  {:width 144 :height 144 :margin-bottom 30}}]
       [h1    "Login to Trustroots"]
       [error (:error @auth-status)]
       [ui/input { :placeholder   "User name"
                   :placeholder-text-color "gray"
                   :return-key-type "next"
                   :on-end-editing    (fn [value]
                                       (swap! fields assoc :user (-> value
                                                                     .-nativeEvent
                                                                     .-text)))
                   :on-submit-editing (fn [value]
                                        (swap! fields assoc :user (-> value
                                                                     .-nativeEvent
                                                                     .-text)))
                   ; For some reason this is in android avd painfully slow solution
                   ;:on-change-text  (fn [value] (swap! fields assoc :user value))
                   ;:value         (:user @fields)
                   :style {:width 200}} ]
       [ui/input { :placeholder   "Password"
                   :return-key-type (if (= (count (:user @fields)) 0) "next" "done")
                   :secure-text-entry true
                   :on-end-editing    (fn [value]
                                        (swap! fields assoc :pwd (-> value
                                                                     .-nativeEvent
                                                                     .-text)))
                   :on-submit-editing (fn [value]
                                        (swap! fields assoc :pwd (-> value
                                                                     .-nativeEvent
                                                                     .-text))
                                        (dispatch [:login @fields]))
                   ;:on-change-text  (fn [value] (swap! fields assoc :pwd value))
                   ;:value           (:pwd @fields)
                   :style           {:width 200}} ]

       [view {:flex-direction "row" }
         ;;[ui/button { :text "Reset"
         ;;             :value "login"
         ;;             :raised true
         ;;             :on-press #(reset! fields {:user nil :pwd nil} )}]
         [ui/button { :text "Login"
                      :value "login"
                      :raised true
                      :on-press #(dispatch [:login @fields])}]]])))
