(ns trustroots.api-test
  (:require
   [cljs.test :refer-macros [async deftest is testing use-fixtures]]
   [trustroots.api :as t]))



; See here ho fetch should work in react native
; https://developer.mozilla.org/en-US/docs/Web/API/Request
; Request.method   Contains the request's method (GET, POST, etc.)
; Request.url      Contains the URL of the request.
; Request.headers  Contains the associated Headers object of the request.
; Request.context  Contains the context of the request (e.g., audio, image, iframe, etc.)
; Request.referrer Contains the referrer of the request (e.g., client).
; Request.referrerPolicy Contains the referrer policy of the request (e.g., no-referrer).
; Request.mode        Contains the mode of the request (e.g., cors, no-cors, same-origin, navigate.)
; Request.credentials Contains the credentials of the request (e.g., omit, same-origin).
; Request.redirect    Contains the mode for how redirects are handled. It may be one of follow, error, or manual.
; Request.integrity   Contains the subresource integrity value of the request (e.g., sha256-BpfBw7ivV8q2jLiT13fxDYAe2tJllusRSZ273h2nFSE=).
; Request.cache       Contains the cache mode of the request (e.g., default, reload, no-cache).

; In addition:
; body               in this case string body is only supported
; Body.bodyUsed Read only

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

(deftest test-fetch-wrapper
  (testing "t/fetch wrapper passes params correctly to js/fetch and call success callback"
    (async done
         (with-redefs
           [js/fetch (fn [url args]
                       (is (= "http://foo.com" url))
                       (let [actual (js->clj args)
                             expected  {"method"  "GET"
                                        "headers" {"Content-Type" "text/json"}
                                        "body"    "{\"foo\":\"bar\"}"
                                        }
                             attr-to-test ["method" "body" "headers"] ;, if ther are more, it's okey, check only these in
                             ] 
                         (doseq [k attr-to-test]
                           (is (= (get actual k) (get expected k)))))
                       (get-promise-and-resolve  "foo"))]
           (is (not (nil? js/fetch)))
           (t/fetch-json "http://foo.com"
                                      :method "GET"
                                      :headers {"Content-Type" "text/json"}
                                      :mode "no-cors"
                                      :body {:foo "bar"}
                                      :on-error   (fn [err]
                                                    (is false "should not call error")
                                                    (done))
                                      :on-success (fn [data]
                                                   ; (is (= data  {:data "foo" :status 200 :ok true }))
                                                    (done))
                                      )))))



(deftest test-fetch-wrapper-handler
   (testing "check that t/handle-fetch-promise handles network error correctly"
     (async done
         (with-redefs
           [js/fetch (fn [url args]
                       (is (= "http://foo.com" url))
                       (get-promise-and-reject "error"))
            ]

           (is (not (nil? js/fetch)))
           (t/fetch-json "http://foo.com"
                          :on-error (fn [err]
                                      (is (= err
                                             {:data "error", :type :network-error}
                                             ))
                                      (done)))))))


