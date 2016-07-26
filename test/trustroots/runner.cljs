(js/require "react-native-mock/mock")
(js/console.log  "global.__DEV__ = true")

(js/eval  "GLOBAL.__DEV__ = true")

(ns trustroots.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [trustroots.db-test]
            [trustroots.handler-test]
            ))

(doo-tests
  'trustroots.db-test
  'trustroots.handler-test
)

