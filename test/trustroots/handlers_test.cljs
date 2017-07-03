(ns trustroots.handler-test
  (:require
   [trustroots.test-helper :refer [specs expect]]
   [re-frame.core :as re :refer [subscribe dispatch dispatch-sync]]
   [trustroots.subs :as s]
   [trustroots.handlers :as t]
   [trustroots.domain.main :as d]))

; Helpers

(defn init-db[]
  (dispatch-sync [:initialize-db]) )

(defn get-in-db
  [q]
  (let [db (subscribe [:get-db])]
    (get-in @db q)))

(specs
 {
  "Test re-frame handlers"
  ;============================

  {:beforeEach init-db

   "Test helper functions"
   ;----------------------------------------------------

   {
    "Initial-db should be called before each test"
    [:= #(deref (subscribe [:get-db])) d/app-db]

    "Get-in-db with nil params should return the db"
    [:= #(get-in-db nil) d/app-db]

    "Get-in-db with vector param should use get-in form to get part of current app-db ratom"
    [:= #(get-in-db [:auth]) (get-in d/app-db [:auth])]
    }


   "Navigation handlers"
   ;----------------------------------------------------

   {
    "Test that :set-page message updates page correcty"
    [:act (fn []
           (dispatch-sync [:set-page :inbox]))
     :truthy? #(= (get-in-db [:page]) :inbox)]
    }

   "Auhentication tests"
   ;----------------------------------------------------
   {":auth-fail should set error message to :auth :error"
   [:act #(dispatch-sync [:auth-fail])

    :not-nil? #(get-in-db [:auth :error])
    :falsy?   #(get-in-db [:auth :in-progress])
    :nil?     #(get-in-db [:auth :identity])]

    ":auth-success should set :identity value and remove possible errors"
    [:act (fn []
             (dispatch-sync [:auth-fail])
             (dispatch-sync [:auth-success "user"]))

    :nil?         #(get-in-db [:auth :error])
    :falsy?       #(get-in-db [:auth :in-progress])
    :not-nil?     #(get-in-db [:auth :identity])]
    }}})
