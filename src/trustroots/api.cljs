(ns trustroots.api
  (:require
    [trustroots.helpers :refer [log info debug]]
    [trustroots.fetch-helper :as f :refer [fetch-json]]
    [re-frame.core :refer [dispatch]]
  ))

; Api call
; ----------------------------------------------------------------------------

(defn signin [& {:keys [user on-success on-error fetch-fn]
                 :or [fetch-fn f/fetch-json]
                 }]
  "Credentials schema is
  {
      username (string): Username or email. ,
      password (string): Password
  }.
  On succesful request return object that contains user data and autehtication cookie."
  (let [fetch (if (nil? fetch-fn) f/fetch-json fetch-fn)]
    (fetch
     :method "POST"
     :endpoint :sign-in
     :body user
     :on-success on-success
     :on-error   (fn [err]
                   (on-error
                    (if (= 400 (:status err))
                      (assoc err :type :invalid-credentials)
                      err))))))


(defn inbox [& {:keys [on-success on-error fetch-fn]
                 :or [fetch-fn f/fetch-json]
                 }]
   "On succesful request return object that contains user data and autehtication cookie."
  (let [fetch (if (nil? fetch-fn) f/fetch-json fetch-fn)]
    (fetch
     :method "GET"
     :endpoint :messages
     :on-success on-success
     :on-error   on-error
     )))

(defn conversation-with [user-id & {:keys [on-success on-error fetch-fn]
                :or [fetch-fn f/fetch-json]
                }]
  "On succesful request return object that contains user data and autehtication cookie."
  (let [fetch (if (nil? fetch-fn) f/fetch-json fetch-fn)]
    (fetch
     :method "GET"
     :endpoint :messages
     :endpoint-path [ user-id ]
     :on-success on-success
     :on-error   on-error
     )))
