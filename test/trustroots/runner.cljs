(ns trustroots.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [trustroots.db-test]
              ))

(doo-tests
  'trustroots.db-test
)
