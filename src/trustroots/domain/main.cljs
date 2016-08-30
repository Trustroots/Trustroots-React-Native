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
          :network-state s/Any
          :message/threads [s/Any]
          :request-in-progress [s/Keyword]
          }))

;; initial state of app-db
(def app-db
  (merge auth/app-db
         {
          :off-line   true
          :page       "main"
          :greeting   "Hello Clojure in iOS and Android!"
          :network-state :not-initialized
          :message/threads []
          :request-in-progress []
          }))

(defn set-offline! [db state]
  (assoc db :off-line state ))
