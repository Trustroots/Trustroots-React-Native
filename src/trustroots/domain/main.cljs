(ns trustroots.domain.main
  (:require
    [schema.core :as s :include-macros true]
    [trustroots.domain.auth :as auth]))



;; schema of app-db
(def schema
  (merge auth/schema
         {
          :services {
                     :toaster (s/maybe s/Any)
                     :navigator (s/maybe s/Any)}

          :off-line s/Bool
          :page     (s/enum :login :inbox :conversation)
          :network-state s/Any
          ;; user inbox from /api/messages
          :message/inbox [s/Any]
          :message/conversation-with
          {
            ;user-id | Messages (from /api/messages/{user-id})
            (s/maybe s/Str) [s/Any]}

          :message/current-conversation (s/maybe s/Str)
          :request-in-progress [s/Keyword]}))


;; initial state of app-db
(def app-db
  (merge auth/app-db
         {
          :services {:toaster   nil
                     :navigator nil}
          :off-line   true
          :page       :login
          :network-state :not-initialized
          :message/inbox []
          :message/conversation-with {}
          :message/current-conversation nil
          :request-in-progress []}))


(defn set-offline! [db state]
  (assoc db :off-line state))
