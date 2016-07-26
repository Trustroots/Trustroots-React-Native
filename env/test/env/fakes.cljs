(ns env.fakes)
(enable-console-print!)

(js/require "react-native-mock/mock")
(js/eval  "__DEV__ = true")
(println js/global.__DEV__)
