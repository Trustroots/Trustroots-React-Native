(ns trustroots.api-test
  (:require
   [trustroots.test-helper :refer [specs expect do-asserts]]
   [cljs.test :refer-macros [async deftest is testing use-fixtures]]
   [trustroots.helpers :refer [log]]
   [trustroots.api :as t]))

(specs "Test api endpoint helpers"
      { "Signin"
         (fn [done]
           (let [expected-endpoint :sign-in
                 test-failed-because (fn [description done _]
                                       (do-asserts [:= nil? description])
                                       (done))
                 fetch-fn (fn [& {:keys [endpoint body method on-success] :as all}]
                            (do-asserts [:= (str expected-endpoint) (str endpoint)])
                            (on-success {:data {:user :data} :status 200 :ok true}))
                ]
               (t/signin :user {:username "foo" :password "bar"}

                         :on-success (fn [user](do-asserts [:not-nil? user]) (done))

                         :on-invalid-credentials
                         (partial test-failed-because "It should not hava called on-invalid-credentials for successful response" done)

                         :on-network-error
                         (partial test-failed-because "It should not hava called on-network-error for successful response" done)

                         :fetch-fn fetch-fn
                         ))
           )

        })

