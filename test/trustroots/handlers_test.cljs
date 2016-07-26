(ns trustroots.handler-test
  (:require
    [cljs.test :refer-macros [async deftest is testing]]
    [re-frame.core :refer [subscribe dispatch]]
    [trustroots.handlers :as t]
    [trustroots.domain.main :as d]))


(defn my-test-fixture [f]
        (f)
)

(deftest test-generic-handlers
  (testing "Test that initial-db is called before each test."
    (let [db-1 (subscribe :get-db)]
          (is (= db-1 d/app-db)))))

