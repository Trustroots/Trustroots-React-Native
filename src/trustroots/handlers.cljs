(ns trustroots.handlers
  (:require
    [clojure.walk :refer [keywordize-keys]]
    [re-frame.core :refer [register-handler after dispatch]]
    [schema.core :as   s :include-macros true]
    [trustroots.domain.main :as main :refer [app-db schema]]
    [trustroots.helpers :refer [log info debug]]
    [trustroots.db :as db]
    [trustroots.domain.auth :as auth]
    [trustroots.api :as api]
    ))

;; -- Middleware ------------------------------------------------------------
;; See https://github.com/Day8/re-frame/wiki/Using-Handler-Middleware

(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
    (when-let [problems (s/check a-schema db)]
      (info db)
      (info problems)
      (throw (js/Error. (str "Schema check failed: " problems)))))

(def validate-schema-mw
  (if goog.DEBUG
    (after (partial check-and-throw schema))
    []))

;; Helpers
;; ------------------------------------------------------------------------

(defn register-handler-for [event-name handler-fn]
  "Simplify register handler calls by automatically registring middleware
   and by droping event name from event arguments"
  (register-handler
    event-name
    validate-schema-mw
    (fn [db evt]
      (debug (str "Dispatch " event-name))
      (apply handler-fn (concat [db] (rest evt))))))

;; Generic handlers
;; -------------------------------------------------------------

(register-handler-for
  :initialize-db
  (fn [_ _]
    (info app-db)
    app-db))


(register-handler-for
 :set-service
 (fn [db service value]
   (assoc-in db [:services service] value)))


;; Navigation handlers
;; -------------------------------------------------------------

(register-handler-for
  :set-page
  (fn [db value]
    (assoc-in db [:page] value)))

;; DB handlers
;; -------------------------------------------------------------

(register-handler-for
  :set-db
  (fn [db new-state]
    (if-let [problems (s/check schema new-state)]
      (do
        (log problems)
        db)
      new-state)))


(register-handler-for
 :storage-error
 (fn [db err-event error]
   (log error)
   ; currently do nothing error handling should be here
   db))

(register-handler-for
  :load-db
  (fn [db _]
    (db/load #(dispatch [:set-db %1]) #(dispatch [:storage-error :load-db %1]))
    db))

(register-handler-for
  :save-db
  (fn [db _]
    (db/save! db #(dispatch [:storage-error :save-db %1]))
    db))

;; db handlers
;; -------------------------------------------------------------

(register-handler-for
  :logout
  (fn [db _]
    (dispatch [:save-db])
    (auth/set-user! db nil)))

(register-handler-for
  :login
  (fn [db user-pwd]
    (let [sign-in api/signin]
      (sign-in :user {:username (:user user-pwd) :password (:pwd user-pwd)}
               :on-success (fn [user] (dispatch [:auth-success user] ))
               :on-error
               #(condp = (:type %)
                   :invalid-credentials (dispatch [:auth-fail])
                   :network-error (dispatch [:check-off-line])
                   (dispatch [:unknown-error])))

      (-> db
          (auth/set-in-progress! true)
          (auth/set-user!        nil)
          (auth/set-error!       nil)))))

(register-handler-for
  :auth-fail
  (fn [db error]
    (-> db
        (auth/set-in-progress! false)
        (auth/set-user!        nil)
        (auth/set-error!       "Authentication failed"))))


(register-handler-for
  :auth-success
  (fn [db user]
    (dispatch [:save-db])
    (when (= (:page db) "login")
      (dispatch [:set-page "main"]))
    (-> db
        (auth/set-in-progress! false)
        (auth/set-user!        user)
        (auth/set-error!       nil))))

(register-handler-for
 :set-off-line
 (fn [db mode]
   (assoc db :network-state mode)))

;; get message threads

(register-handler-for
 :messages/fetch
 (fn [db user-pwd]
   (let [get-messages api/messages]
     (get-messages
      :on-success (fn [data]
                    (dispatch [:messages/fetch-success (:data data)] ))
              :on-error
              #(condp = (:type %)
                 :invalid-credentials (dispatch [:logout])
                 :network-error       (do (dispatch [:check-off-line])
                                           (dispatch :messages/fetch-fail))
                 (dispatch [:unknown-error])))

     db)))

(register-handler-for
 :messages/fetch-success
 (fn [db data]
   (log data)

   (assoc db :message/threads data)))

(register-handler-for
 :messages/fetch-fail
 (fn [db user-pwd] db))



(def react-native (js/require "react-native"))

;; Hardware related event listeners
;; ----------------------------------

(defn check-nework-state []
  (-> react-native
      (.-NetInfo)
      (.-isConnected)
      (.fetch)
      (.done #(dispatch [:set-off-line (not %)])))
  )

(register-handler-for
 :initialize-hardware
 (fn [db _]
   ;; register event listeners
   ;; check once
   ;; Currently any way to check NetInfo does not work
   ;; This is left here as warning
   ;(js/setTimeout
   ;    check-nework-state
   ;    15000)
   db))

;; (re-frame.core/dispatch [:set-page "messages"] )
