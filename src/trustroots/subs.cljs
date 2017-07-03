(ns trustroots.subs
  (:require-macros [reagent.ratom   :refer [reaction]])
  (:require [re-frame.core          :refer [register-sub]]
            [trustroots.domain.auth :as auth]
            [trustroots.helpers :refer [log info debug]]
            ))

; Helpers
; ---------------------------------------------------------------

(defn register-sub-for [msg query-fn]
  "Simplify call by removing event name from event args and need
   to call reaction separately"
  (register-sub
    msg
    (fn [db args]
      (reaction
         (debug (str "DISPATCH " msg " with args"))
         (apply query-fn (concat [db] (rest args)))))))


;; Generic helpers handlers
;; -------------------------------------------------------------

(register-sub-for
  :get-db
  (fn [db _] @db))


(register-sub-for
  :get-greeting
  (fn [db _] (get @db :greeting)))

;; Messages
;; -------------------------------------------------------------
(register-sub-for
  :inbox/get
  (fn [db _]
    (let [inbox (get @db :message/inbox)
          my-id (:_id (auth/get-user @db))]

      (map
       (fn [thread]
         (let [user-to (:userFrom thread)
               user-from (:userTo thread)]
           (assoc thread :discussion-with
                  (if (= (:_id user-to) my-id)
                    user-from
                    user-to))))
       inbox)
      )))

;; Navigation handlers
;; -------------------------------------------------------------

(register-sub-for
  :get-page
  (fn [db _]
    (if (auth/get-user @db)
      (get @db :page)
      :login)))

;; Authentication/identity subs
;; -------------------------------------------------------------

(register-sub-for
   :auth-status
    (fn [db _]
        (cond
          (auth/get-user @db)       {:in-progress false
                                     :succeed true}
          (auth/in-progress? @db)   {:in-progress true
                                     :succeed false}
          (auth/get-error   @db)    {:in-progress false
                                     :succeed false
                                     :error "Authentication failed"}
          true                      {:in-progress false
                                     :succeed false})))

(register-sub-for
 :current-conversation
 (fn [db _]
   (let [selected-user (get @db :message/current-conversation)]
     (->>
      (get-in @db [:message/conversation-with selected-user])
      (map #(assoc
             %1
             :is-from-someone-else
             (= selected-user
                (get-in %1 [:userFrom :_id])
                               ))))
              )))

(register-sub-for
 :get-user-of-current-conversation
 (fn [db _]
   (get @db :message/current-conversation)))
