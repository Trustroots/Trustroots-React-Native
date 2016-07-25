(ns trustroots.db-test
  (:require [cljs.test :refer-macros [async deftest is testing]]
            [schema.core :as s :include-macros true]
            [trustroots.domain.main :as main]))

(deftest db-test
   (testing "default schema should be valid"
     (is (nil? (s/check main/schema main/app-db)))
    ))
