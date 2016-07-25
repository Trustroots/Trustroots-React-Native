(ns trustroots.domain.auth
  (:require [schema.core :as s :include-macros true]))

;; schema of app-db
(def schema { :auth {
                      :identity (s/maybe s/Any)
                      :in-progress s/Bool
                      :error (s/maybe s/Str)
                     }})

;; initial state of app-db frament
(def app-db { :auth { :identity nil
                      :in-progress false
                      :error nil
                     }})

(defn set-user! [db user]
  (assoc-in db [:auth :identity] user))

(defn get-user [db]
  (get-in db   [:auth :identity]))

(defn set-error! [db error]
  (assoc-in db [:auth :error] error))

(defn get-error [db]
  (get-in db   [:auth :error]))

(defn set-in-progress! [db value]
  (assoc-in db [:auth :in-progress] value))

(defn in-progress? [db]
  (get-in db   [:auth :in-progress]))
