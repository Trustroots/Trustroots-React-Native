(ns trustroots.handlers
  (:require [clojure.walk :refer [keywordize-keys]]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler after dispatch dispatch-sync]]
            [schema.core :as   s :include-macros true]
            [trustroots.domain.main :as main :refer [app-db schema]]
            [trustroots.helpers :refer [log info debug current-route]]
            [trustroots.db :as db]
            [trustroots.domain.auth :as auth]
            [trustroots.api :as api]))

;; -- Constants

(def user-pwd-cache-key "user-pwd")
(def db-ignored-keys [:services])

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

(defn add-in-progress-event-for [db event-key] db)

(defn remove-in-progress-event-for [db event-key] db)

(defn keyword-with-suffix [event-key suffix]
  (keyword (namespace event-key) (str (name event-key) suffix)))

(defn register-api-call-handler
  [& {:keys [begin-api-event-key
             api-call
             init-fn
             params-fn
             success-fn
             error-fn
             context-fn]}] ;; optional

  (let [on-success-key (keyword-with-suffix begin-api-event-key "/success")
        on-error-key (keyword-with-suffix begin-api-event-key "/error")]
    (register-handler
     begin-api-event-key
     validate-schema-mw
     (fn [db evt]
       (debug (str "Dispatch " begin-api-event-key))
       (let [init-db (if init-fn
                       (apply init-fn (concat [db] (rest evt)))
                       db)
             context (when context-fn (apply context-fn (concat [init-db] (rest evt))))
             on-api-success (fn [api-response]
                              (dispatch [on-success-key (:data api-response) context]))
             on-api-error (fn [error-response]
                            (dispatch [on-error-key error-response context]))
             partial-api-call (partial api-call
                                       :on-success on-api-success
                                       :on-error on-api-error)
             params  (flatten (vec (apply params-fn (concat [init-db] (rest evt)))))]
         (apply partial-api-call params)
         (add-in-progress-event-for init-db begin-api-event-key))))

    (register-handler
     on-success-key
     validate-schema-mw
     (fn [db evt]
       (-> db
           (remove-in-progress-event-for begin-api-event-key)
           (#(apply success-fn (concat [%1] (rest evt)))))))

    (register-handler
     on-error-key
     validate-schema-mw
     (fn [db evt]
       (-> db
           (remove-in-progress-event-for begin-api-event-key)
           (#(apply error-fn (concat [%1] (rest evt)))))))))


;; Generic handlers
;; -------------------------------------------------------------

(register-handler-for
 :initialize-db
 (fn [_ _]
   (info app-db)
   (db/cache-load user-pwd-cache-key
                  (fn [data] (dispatch [:auth/login data]))
                  identity)
   app-db))


(register-handler-for
 :register-service
 (fn [db service-key service]
   (assoc-in db [:services service-key] service)))


;; Navigation handlers
;; -------------------------------------------------------------

(register-handler-for
 :set-page
 (fn [db value]
   (let [navigator (get-in db [:services :navigator])]
     (.push navigator (clj->js {:index (-> navigator
                                           (.getCurrentRoutes)
                                           (.-length))
                                :name (str value)})))
   (assoc-in db [:page] value)))

(register-handler-for
 :navigate/back
 (fn [db _]
   (let [navigator (get-in db [:services :navigator])]
     (if (< 1 (-> navigator
                  (.getCurrentRoutes)
                  (.-length)))
       (do (.pop navigator)
           (assoc-in db [:page] (current-route navigator)))
       db))))

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
   ;; currently do nothing error handling should be here
   db))

(register-handler-for
 :load-db
 (fn [db _]
   (db/load #(dispatch [:set-db %1]) #(dispatch [:storage-error :load-db %1]))
   db))

(register-handler-for
 :save-db
 (fn [db _]
   (println db)
   (let [db-filtered (apply dissoc db db-ignored-keys)]
     (db/save! db-filtered #(dispatch [:storage-error :save-db %1]))
     db)))

;; db handlers
;; -------------------------------------------------------------

;; (register-handler-for
;;   :logout
;;   (fn [db _]
;;     (api/signout
;;       :on-success (fn []))
;;     (auth/set-user! db nil)
;;     (dispatch [:save-db])
;;     (dispatch [:set-page :login])
;;     (-> app-db
;;       (assoc :services (:services db)))))  ;; we need to preserve the instance of Navigator and Toaster

(register-handler-for
 :login
 (fn [db user-pwd]
   (let [sign-in api/signin]
     (sign-in :user {:username (:user user-pwd) :password (:pwd user-pwd)}
              :on-success (fn [user-res] (dispatch [:auth-success (:data user-res)]))
              :on-error
              #(condp = (:type %)
                 :invalid-credentials (dispatch [:auth-fail])
                 :network-error (dispatch [:set-offline true])
                 (dispatch [:unknown-error])))

     (-> db
         (auth/set-in-progress! true)
         (auth/set-user!        nil)
         (auth/set-error!       nil)))))

(register-api-call-handler
 :begin-api-event-key  :auth/login
 :api-call             api/signin
 :init-fn              (fn [db user]
                         (-> db
                             (auth/set-user!  nil)
                             (auth/set-error! nil)))
 :params-fn            (fn [db user-pwd]
                         {:user {:username (:user user-pwd) :password (:pwd user-pwd)}})
 :context-fn           (fn [db user] user)
 :success-fn           (fn [db user context]
                         (db/cache! user-pwd-cache-key context)
                         (when (= (:page db) :login)
                           (dispatch [:set-page :inbox]))
                         (-> db
                             (auth/set-user! user)))
 :error-fn             (fn [db]
                         (-> db
                             (auth/set-error! "Authentication failed"))))

(register-api-call-handler
 :begin-api-event-key  :auth/logout
 :api-call             api/signout
 :params-fn            (fn [db _] {})
 :success-fn           (fn [db _ _]
                         (dispatch [:save-db])
                         (db/cache! user-pwd-cache-key nil)
                         (dispatch [:set-page :login])
                         (-> app-db
                             (assoc :services (:services db))))  ;; we need to preserve the instance of Navigator and Toaster
 :error-fn             (fn [db]
                         (-> db
                             (auth/set-error! "Logout failed"))))


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
     (dispatch [:set-page :inbox]))
   (-> db
       (auth/set-in-progress! false)
       (auth/set-user!        user)
       (auth/set-error!       nil))))

(register-handler-for
 :set-offline
 (fn [db mode]
   (when-let [toaster (get-in db [:services :toaster])]
     (log toaster)
     (toaster "You are currently offline" 5000))

   (assoc db :network-state mode)))

;; get inbox

(register-handler-for
 :inbox/fetch
 (fn [db user-pwd]
   (let [get-messages api/inbox]
     (get-messages
      :on-success (fn [data]
                    (dispatch [:inbox/fetch-success (:data data)]))
      :on-error
      #(condp = (:type %)
         :invalid-credentials (dispatch [:logout])
         :network-error       (do (dispatch [:set-offline true])
                                  (dispatch :inbox/fetch-fail))
         (dispatch [:unknown-error])))
     db)))

(register-handler-for
 :inbox/fetch-success
 (fn [db data]
   (assoc db :message/inbox data)))

(register-handler-for
 :inbox/fetch-fail
 (fn [db data] db))


;; Get one message thread (=converstion)
;; ----------------------

(register-handler-for
 :conversation/fetch
 (fn [db user-id]
   (let [get-messages (partial api/conversation-with user-id)]
     (get-messages
      :on-success (fn [data]
                    (dispatch [:conversation/fetch-success user-id (:data data)]))
      :on-error
      #(condp = (:type %)
         :invalid-credentials (dispatch [:logout])
         :network-error       (do (dispatch [:set-offline true])
                                  (dispatch [:conversation/fetch-fail user-id]))
         (dispatch [:unknown-error])))

     db)))

(register-handler-for
 :conversation/fetch-success
 (fn [db user-id data]
   (assoc-in db [:message/conversation-with user-id] data)))

(register-handler-for
 :conversation/fetch-fail
 (fn [db user-id] db))


(register-handler-for
 :show/conversation-with
 (fn [db user-id]
   (dispatch [:conversation/fetch user-id])
   (dispatch [:set-page :conversation])
   (assoc db :message/current-conversation user-id)))


;; Send message
;; ------------

(register-handler-for
 :flush
 (fn [db _]
   (reagent/flush)
   db))

(register-handler-for
 :message/update-draft-with
 (fn [db to-user-id content]
   (dispatch [:flush])
   (assoc-in db [:message/draft-with to-user-id] content)))

(register-handler-for
 :message/send-to
 (fn [db to-user-id]
   (let [content (get-in db [:message/draft-with to-user-id])
         send-message (partial api/send-message-to to-user-id content)]
     (send-message
      :on-success (fn [data]
                    (dispatch [:message/send-to-success to-user-id (:data data)]))
      :on-error
      (fn [error]
        (log error)
        (condp = (:type error)
          :invalid-credentials (dispatch [:logout])
          :network-error       (do (dispatch [:set-offline true])
                                   (dispatch [:message/send-to-fail to-user-id content]))
          (dispatch [:unknown-error]))))

     (assoc-in db [:message/draft-with to-user-id] ""))))

(register-handler-for
 :message/send-to-success
 (fn [db to-user-id message]
   (update-in db [:message/conversation-with to-user-id] #(conj %1 message))))

(register-handler-for
 :message/send-to-fail
 (fn [db user-id content] db))


;; Hardware related event listeners
;; ----------------------------------

(def react-native (js/require "react-native"))

(defn check-network-state []
  (-> react-native
      (.-NetInfo)
      (.-isConnected)
      (.fetch)
      (.done #(dispatch [:set-offline (not %)]))))


(register-handler-for
 :initialize-hardware
 (fn [db _]
   ;; register event listeners
   ;; check once
   ;; Currently any way to check NetInfo does not work
   ;; This is left here as warning
   ;;(js/setTimeout
   ;;    check-nework-state
   ;;    15000)
   db))

(comment
  (re-frame.core/dispatch [:set-page :inbox])
  (re-frame.core/dispatch [:inbox/fetch])
  (re-frame.core/subscribe [:inbox/get]))
