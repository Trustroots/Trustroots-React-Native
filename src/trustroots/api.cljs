(ns trustroots.api
  (:require
    [trustroots.helpers :refer [log info debug]]
    [re-frame.core :refer [dispatch]]
  ))

(def baseurl "https://www.trustroots.org/api")
(def url_map
  {
       :signin "auth/signin"
  })

(defn get-url [endpoint]
  (str baseurl (endpoint url_map)))

; Api calls
; -----------------------------------------------------------------------

(defn handle-fetch-promise
  [promise & {:keys [on-success on-error]}]
  (-> promise
      ; network error
      (.catch (fn [err] (on-error {:data err :type :network-error})))

      ; fecth resolves also 400 and 500 errors
      (.then (fn [res]
               (let [ok (.-ok res)
                     status (.-status res)
                     handle-data (fn [data]
                                   (if ok
                                    (on-success {:data data :status status})
                                    (on-error   {:data data :status status :type :http-error})))
                     ]
                 (-> (.json res)
                     (.then handle-data))))))) 


(defn fetch-json [url & {:keys [headers method body on-success on-error]} ]
  "Helper for JSON rest api request"
  (let [default-header {"Content-Type" "text/json"}
        optional-body  (if body
                         {"body" (js/JSON.stringify (clj->js body)) }
                         {})

        args (merge {
                     "method"   method 
                     "headers" (merge default-header headers)}
              optional-body )
        query (js/fetch url (clj->js args))
        ]
    (handle-fetch-promise query :on-error on-error :on-success on-success)))

(defn signin [{user :user
              on-success :on-success
              on-invalid-credentials :on-invalid-credentials }]
  "Credentials schema is
  {
      username (string): Username or email. ,
      password (string): Password
  }.
  On succesful request return object that contains user data and autehtication cookie."
  (let [
         url (get-url :signin)
       ]
    ""
    ))

