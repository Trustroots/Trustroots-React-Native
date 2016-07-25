(ns trustroots.api
  (:require
    [re-frame.core :refer [dispatch]]
    [ajax.core :refer [GET POST]]))

(def baseurl "https://www.trustroots.org/api")
(def url_map
  {
       :signin "auth/signin"
  })

(defn get-url [endpoint]
  (str baseurl (endpoint url_map)))

; Api calls
; -----------------------------------------------------------------------

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
    (POST url {:params user
               :response-format :json
               :format :json
               :handler       (fn [res]
                                (println "SUCCESS POST signin")
                                (.dir js/console res))
               :error-handler (fn [res]
                                (println "FAILED POST signin")
                                (.dir js/console res))
               :keywords? true
              })))

