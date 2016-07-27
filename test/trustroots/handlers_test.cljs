(ns trustroots.handler-test
  (:require
    [cljs.test :refer-macros [async deftest is testing use-fixtures]]
    [re-frame.core :as re :refer [subscribe dispatch dispatch-sync]]
    [trustroots.subs :as s]
    [trustroots.handlers :as t]
    [trustroots.domain.main :as d]))


(defn init-db-fixure [f]
  (dispatch-sync [:initialize-db])
  (f))

(use-fixtures :each  init-db-fixure)

(deftest test-generic-handlers
  (testing "Test that initial-db is called before each test."
    (let [db-1 (subscribe [:get-db])]
      (is (= @db-1 d/app-db)))))

(defn get-in-db [q]
  (let [db (subscribe [:get-db])]
    (get-in @db q)))

(deftest test-generic-handlers-and-test-helpers
  (testing "Test that initial-db is called before each test."
    (let [db-1-atom (subscribe [:get-db])
          db-1 (get-in-db [])
          auth-node (get-in-db [:auth])
          ]
      (is (= @db-1-atom d/app-db))
      (is (= db-1 d/app-db))
      (is (= auth-node (get-in d/app-db [:auth]))))))

(deftest test-naviation-handlers
  (testing "Test that set-page changes app-db correctly"
    (dispatch-sync [:set-page "foo"])
    (= "foo" (get-in-db [:page]))))

(deftest test-authentication-handlers
  (testing ":auth-fail should set error message to :auth :error"

    (is (nil? (get-in-db [:auth :error]))
        "Ensure that error is nil by default")

    (dispatch-sync [:auth-fail])

    (is (not (nil? (get-in-db [:auth :error]))))
    (is (not (get-in-db [:auth :in-progress])))
    (is (nil? (get-in-db [:auth :identity])))
    )

  (testing ":auth-success message"

    (is (nil? (get-in-db [:auth :identity]))
        "Ensure that identity is nil by default")

    (dispatch-sync [:auth-success "foo"])

    (is (nil? (get-in-db [:auth :error])))
    (is (not  (get-in-db [:auth :in-progress])))
    (is (= "foo" (get-in-db [:auth :identity]))))
  (testing ":logout should make [:auth :identity] nil"

    (dispatch-sync [:auth-success "foo"])

    (is (not (nil? (get-in-db [:auth :identity]))
             "Ensure that identity is not nil at this point"))

    (dispatch-sync [:logout])

    (is (nil? (get-in-db [:auth :identity]))
        "After log out it should be nil"))
  )

