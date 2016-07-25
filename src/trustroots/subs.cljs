(ns trustroots.subs
  (:require-macros [reagent.ratom   :refer [reaction]])
  (:require [re-frame.core          :refer [register-sub]]
            [trustroots.domain.auth :as auth]
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
         (println (str "DISPATCH " msg " with args"))
         (apply query-fn (concat [db] (rest args)))))))


;; Generic helpers handlers
;; -------------------------------------------------------------

(register-sub-for
  :get-db
  (fn [db _] @db))


(register-sub-for
  :get-greeting
  (fn [db _] (get @db :greeting)))

;; Navigation handlers
;; -------------------------------------------------------------

(register-sub-for
  :get-page
  (fn [db _]
    (println @db)
    (if (auth/get-user @db)
      (get @db :page)
      "login" )))

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

