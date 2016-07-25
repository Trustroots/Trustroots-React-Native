(ns trustroots.domain.main
  (:require
    [schema.core :as s :include-macros true]
    [trustroots.domain.auth :as auth]
  ))


;; schema of app-db
(def schema (merge
    auth/schema {
              :page     s/Str
              :greeting s/Str
    }))

;; initial state of app-db
(def app-db
  (merge auth/app-db
         {:page       "main"
          :greeting   "Hello Clojure in iOS and Android!"}))
