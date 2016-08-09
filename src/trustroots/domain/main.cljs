(ns trustroots.domain.main
  (:require
    [schema.core :as s :include-macros true]
    [trustroots.domain.auth :as auth]
  ))


;; schema of app-db
(def schema
  (merge auth/schema
         {
          :off-line s/Bool
          :page     s/Str
          :greeting s/Str
          }))

;; initial state of app-db
(def app-db
  (merge auth/app-db
         {
          :off-line   true
          :page       "main"
          :greeting   "Hello Clojure in iOS and Android!"
          }))

(defn set-offline! [db state]
  (assoc db :off-line state ))
