(ns trustroots.fetch-helper
  (:require [trustroots.helpers :refer [log info debug]]))

(def baseurl "https://www.trustroots.org/api")
(def url_map
  {:sign-in "auth/signin"
   :messages "messages"
   :signout "auth/signout"})


(defn get-url [endpoint]
  (str baseurl "/" (endpoint url_map)))


(defn- handle-fetch-promise
  [promise & {:keys [on-success on-error parse-json]}]
  (-> promise
      ;; network error
      (.catch (fn [err] (on-error {:data err :type :network-error})))

      ;; fecth resolves also 400 and 500 errors
      (.then (fn [res]
               (if (not (nil? res))
                 (let [ok (.-ok res)
                       status (.-status res)
                       error-type (cond
                                    ok nil
                                    (= status 403) :unauthorized
                                    :else :http-error)
                       handle-data (fn [data]
                                     (if ok
                                       (on-success {:data data :status status :ok ok})
                                       (on-error   {:data data :status status :type error-type})))
                       has-json (if (nil? parse-json) false
                                    parse-json)]

                   (-> (if has-json
                         (-> (.json res)
                             (.then #(js->clj %1 :keywordize-keys true))
                             (.then
                              #(if (vector? %1)
                                 (vec (map clojure.walk/keywordize-keys %1))
                                 %1)))
                         (.text res))
                       (.then #(do (log %) %))
                       (.then handle-data))))))))

(defn fetch-json [& {:keys [url endpoint endpoint-path headers method body on-success on-error fetch-fn]}]
  "Helper for JSON rest api request"
  (let [react-fetch    (if (nil? fetch-fn)
                         js/fetch
                         fetch-fn)
        path           (apply str (map (partial str "/") endpoint-path))
        req-url        (if (nil? endpoint)
                         url
                         ;;(get-url endpoint)
                         (str (get-url endpoint) path))

        default-header {"Content-Type" "application/json" "Accept" "application/json"}
        optional-body  (if body
                         {"body" (js/JSON.stringify (clj->js body))}
                         {})

        args (merge {"method"   method
                     "credentials" "include"
                     "headers" (merge default-header headers)}
                    optional-body)
        query (react-fetch req-url (clj->js args))]


    (log "req-url:" req-url)
    (handle-fetch-promise query :on-error on-error :on-success on-success :parse-json true)))

(defn fetch-no-response [& {:keys [url endpoint endpoint-path headers method body on-success on-error fetch-fn]}]
  "Helper for JSON rest api request"
  (let [react-fetch    (if (nil? fetch-fn)
                         js/fetch
                         fetch-fn)
        path           (apply str (map (partial str "/") endpoint-path))
        req-url        (if (nil? endpoint)
                         url
                         ;;(get-url endpoint)
                         (str (get-url endpoint) path))
        optional-body  (if body
                         {"body" (js/JSON.stringify (clj->js body))}
                         {})

        args (merge {"method"   method
                     "credentials" "include"
                     "headers" headers}
                    optional-body)
        query (react-fetch req-url (clj->js args))]

    (log "fetch-no-response:" method req-url headers)
    (handle-fetch-promise query :on-error on-error :on-success on-success :parse-json false)))
