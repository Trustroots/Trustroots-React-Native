(ns trustroots.db
  (:require
   [trustroots.helpers :refer [log info debug]]))

(def AsyncStorage (.-AsyncStorage (js/require "react-native")))

;; Persist data by using React Native AsyncStore

(defn save! [db on-error]
  (->
   AsyncStorage
   (.setItem '@Trustroots:db' (clj->js db))
   (.then debug)
   (.catch on-error)))

(defn load [on-success on-error]
  (->
   AsyncStorage
   (.getItem '@Trustroots:db')
   (.then js->clj)
   (.then on-success)
   (.catch on-error )))
