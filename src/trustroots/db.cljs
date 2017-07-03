(ns trustroots.db
  (:require
   [trustroots.helpers :refer [log info debug sha256]]))

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

(defn generate-key [key-vector]
  (let [key (->
             key-vector
             (sha256)
             (#(str "@Trustroots:cache:" %)))
        ]
        key))

(defn cache! [key-vector obj]
  (let [key (generate-key key-vector)]
    (->
     AsyncStorage
     (.setItem key (js/JSON.stringify (clj->js obj)))
     (.done (fn [result]
              (log (str "cached" result)))))))

(defn cache-load [key-vector on-success on-error]
  (let [key (generate-key key-vector)]
    (->
     AsyncStorage
     (.getItem key)
     (.then (fn [item]
              (log (str "JSON: " item))
              (->  (js/JSON.parse item)
                   (js->clj :keywordize-keys true))))
     (.then on-success)
     (.catch on-error ))))


