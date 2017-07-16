(ns trustroots.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [trustroots.shared.ui :as ui :refer
                [view text image logo-img]]
            [trustroots.android.styles :as s]
            [trustroots.handlers]
            [trustroots.subs]
            [trustroots.shared.pages.inbox :refer [inbox-page]]
            [trustroots.shared.pages.conversation :refer [conversation-page]]
            [trustroots.shared.pages.login :refer [login-page]]))


(def ReactNative (js/require "react-native"))
(set! js/React ReactNative)
(def app-registry (.-AppRegistry ReactNative))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(defn alert [title]
      (.alert (.-Alert ReactNative) title))

(defn root-scene [{navigator :navigator name :name}]
  (fn []
    [ui/view {:flex 1
              :style   (get-in s/styles [:page])}

      ;;      [ui/view {:style (get-in s/styles [:statusbar])}]
      ;;      [ui/toolbar {:title         "Luno"
      ;;                   :icon          "menu"
      ;;                   :actions       [{:icon    "add-circle"
      ;;                                    :onPress (fn [_]
      ;;                                                       (show-add-dialog))}]
      ;;                   :style         (get-in s/styles [:toolbar])
      ;;                   :on-icon-press (fn [_]
      ;;                                 (.openDrawer @drawer))}]
      (case name
        ; ":inbox"        [inbox-page  {:style (get-in s/styles [:pages :main])}]
        ":inbox"        [inbox-page  {:style (get-in s/styles [:pages :main])}]
        ":conversation" [conversation-page  {:style (get-in s/styles [:pages :main])}]
        ":login"        [login-page {:style (get-in s/styles [:pages :login])}])]))


(defn app-root []
  (-> ReactNative
      (.-BackAndroid)
      (.addEventListener "hardwareBackPress" #(do (dispatch [:navigate/back])
                                                  true)))
  [ui/navigator {:initial-route   {:name ":login" :index 0}
                 :style           (get-in s/styles [:app])
                 :configure-scene (fn [_ _]
                                     js/React.Navigator.SceneConfigs.FloatFromBottomAndroid)
                 :render-scene    (fn [route navigator]
                                    (dispatch [:register-service :navigator navigator])
                                    (r/as-element [root-scene {:navigator navigator :name (-> (js->clj route :keywordize-keys true)
                                                                                              (:name))}]))}])



(defn init []
  (dispatch-sync [:initialize-db])
  (dispatch [:initialize-hardware])
  (dispatch [:load-db])
  (dispatch [:register-service :toaster ui/show-toast])
  (.registerComponent app-registry "trustroots" #(r/reactify-component app-root)))
