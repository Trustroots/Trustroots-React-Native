(ns trustroots.db-test
  (:require [trustroots.test-helper :refer [specs]]
            [schema.core :as s]
            [trustroots.domain.main :as main]))

(specs
    { "Test schema and database"
     {

      "Default schema should be valid"
      [:nil? (s/check main/schema main/app-db)]

      }
     }
  )


;(deftest db-test
;   (testing "default schema should be valid"
;     (is (nil? (s/check main/schema main/app-db)))
;    ))


