(ns trustroots.runner
  (:require [trustroots.test-helper :as th]
            [trustroots.db-test]
            [trustroots.handler-test]
            [trustroots.api-test]
            [trustroots.fetch-test]
            ))

(enable-console-print!)

(defn set-entry-point!
  "Sets the function to be run when starting the script"
  [f]
  {:pre [(fn? f)]}
  (set! *main-cli-fn* f))

(set-entry-point! (fn [] (println "Run tests")))

