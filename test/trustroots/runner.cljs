(ns trustroots.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [trustroots.db-test]
            [trustroots.handler-test]
            [trustroots.api-test]
            ))

(doo-tests
  'trustroots.db-test
  'trustroots.handler-test
  'trustroots.api-test
)



