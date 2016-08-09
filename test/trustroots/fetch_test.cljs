(ns trustroots.fetch-test
  (:require
   [trustroots.test-helper :refer [specs expect do-asserts should]]
   [trustroots.fetch-helper :as t]))

; See here ho fetch should work in react native
; https://developer.mozilla.org/en-US/docs/Web/API/Request

(def Promise (.-Promise (js/require "bluebird")))

(defn get-promise-and-resolve [data]
  (.resolve Promise
            (clj->js {
                      "status" 200
                      "ok"     true
                      "json"   (fn [] (.resolve Promise (clj->js data)))
                      })))

(defn get-promise-and-reject [data]
    (-> Promise
        (.reject (clj->js data))))

(defn fetch-fake-1 [url args]
  (do-asserts [:= "http://foo.com" url])
  (let [actual (js->clj args)
        expected  {"method"  "GET"
                   "headers" {"Content-Type" "this should override default" "Accept" "application/json"}
                   "body"    "{\"foo\":\"bar\"}"
                   ; without this fetcher wont include cookies into request
                   "credentials" "include"
                  }
                  attr-to-test ["method" "body" "headers" "credentials"]
                ]
                (doseq [k attr-to-test]
                  (should = (get actual k) (get expected k) ))
                (get-promise-and-resolve "foo")))

(defn fetch-fake-network-error [url args]
  (do-asserts [:= "http://foo.com" url])
  (get-promise-and-reject "error"))

(specs "Test fetch-json helper"
      { "t/fetch wrapper passes params correctly to js/fetch and call success callback"
       (fn [done]
           (t/fetch-json :url "http://foo.com"
                         :method "GET"
                         :headers {"Content-Type" "this should override default"}
                         :mode "no-cors"
                         :body {:foo "bar"}
                         :on-error   (fn [err]
                                       (do-asserts [:falsy?  "should not call error"])
                                       (done))
                         :on-success (fn [data]
                                       (do-asserts [:= data  {:data "foo" :status 200 :ok true }])
                                       (done))
                         :fetch-fn fetch-fake-1
                         ))

       "check that t/fetch handles network error correctly"
       (fn [done]
         (t/fetch-json :url "http://foo.com"
                       :on-error (fn [err]
                                   (do-asserts [:= err {:data "error", :type :network-error}])
                                   (done))
                       :fetch-fn fetch-fake-network-error)
         )})


