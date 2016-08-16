(ns trustroots.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [trustroots.shared.ui :as ui :refer
                [view text image logo-img]]
            [trustroots.android.styles :as s]
            [trustroots.handlers]
            [trustroots.subs]
            [trustroots.shared.pages.main :refer [main-page]]
            [trustroots.shared.pages.login :refer [login-page]]
            ))

(def ReactNative (js/require "react-native"))
(set! js/React ReactNative)
(def app-registry (.-AppRegistry ReactNative))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(defn alert [title]
      (.alert (.-Alert ReactNative) title))

(defn app-root-old []
  (let [greeting (subscribe [:get-greeting])]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @greeting]
       [image {:source ui/logo-img
               :style  {:width 144 :height 144 :margin-bottom 30}}]
       [ui/button { :text "Add"
                    :value "add"
                    :raised true
                    :on-press #(alert "HELLO!")}]

       ])))


(defn root-scene [{navigator :navigator}]
  (let [tab       (subscribe [:get-page])]
     [ui/view { :flex 1
                :style   (get-in s/styles [:page])
                }
;;      [ui/view {:style (get-in s/styles [:statusbar])}]
;;      [ui/toolbar {:title         "Luno"
;;                   :icon          "menu"
;;                   :actions       [{:icon    "add-circle"
;;                                    :onPress (fn [_]
;;                                                       (show-add-dialog))}]
;;                   :style         (get-in s/styles [:toolbar])
;;                   :on-icon-press (fn [_]
;;                                 (.openDrawer @drawer))}]
      (case @tab
        "main"  [main-page  {:style (get-in s/styles [:pages :main ])} ]
        "login" [login-page {:style (get-in s/styles [:pages :login])} ])]))


(defn app-root []
  [ui/navigator {:initial-route   {:name "main" :index 1}
                 :style           (get-in s/styles [:app])
                 :configure-scene (fn [_ _]
                                     js/React.Navigator.SceneConfigs.FloatFromBottomAndroid)
                 :render-scene    (fn [_ navigator]
                                     (r/as-element [root-scene {:navigator navigator}]))}])



(defn init []
  (dispatch-sync [:initialize-db])
  (dispatch [:initialize-hardware])
  (dispatch [:load-db])
  (.registerComponent app-registry "trustroots" #(r/reactify-component app-root)))
