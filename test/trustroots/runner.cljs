(ns trustroots.runner
  (:require-macros [latte.core :as m :refer [describe it]])
  (:require [cljs.test :refer-macros [run-tests async deftest is testing use-fixtures]]
            [latte.chai :refer [expect]]
            [trustroots.test-helper :as th]
            [doo.runner :refer-macros [doo-tests]]
            [trustroots.db-test]
            [trustroots.handler-test]
            [trustroots.api-test]
            ;[trustroots.fetch-test]
            ))

(defn set-entry-point!
  "Sets the function to be run when starting the script"
  [f]
  {:pre [(fn? f)]}
  (set! *main-cli-fn* f))

(set-entry-point! (fn [] (println "Run tests")))

