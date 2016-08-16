(ns trustroots.db
  (:require
   [trustroots.helpers :refer [log info debug]]))

(def AsyncStorage (.-AsyncStorage (js/require "react-native")))

;; Persist data by using React Native AsyncStore

(defn save! [db on-error]
  (->
   AsyncStorage
   (.setItem "@Trustroots:db" (js/JSON.stringify (clj->js db)))
   (.then (fn [result]
            (log (str "load done" result))
            debug))
   (.catch on-error)))

(defn load [on-success on-error]
  (->
   AsyncStorage
   (.getItem "@Trustroots:db")
   (.then (fn [item]
            (log (str "JSON: " item))
            (->  (js/JSON.parse item)
                 (js->clj :keywordize-keys true))))
   (.then on-success)
   (.catch on-error )))
