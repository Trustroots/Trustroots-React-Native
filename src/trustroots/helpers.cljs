(ns trustroots.helpers
  (:refer-clojure :exclude [hash])
  (:require
   [goog.crypt     :as gcrypt]
   [goog.crypt.Sha256 :as Sha256]
   [clojure.string :as str]))

(defn string->bytes [s]
  (gcrypt/stringToUtf8ByteArray s))  ;; must be utf8 byte array

(defn bytes->hex
  "convert bytes to hex"
  [bytes-in]
  (gcrypt/byteArrayToHex bytes-in))

(defn hash-bytes [digester bytes-in]
  (do
    (.update digester bytes-in)
    (.digest digester)))

(defn sha256-bytes->bytes
  "convert bytes to md5 bytes"
  [bytes-in]
  (hash-bytes (goog.crypt.Sha256.) bytes-in))

(defn sha256-string->bytes
  "convert utf8 string to md5 byte array"
  [string]
  (sha256-bytes->bytes (string->bytes string)))

(defn sha256 [string]
  "convert utf8 string to md5 hex string"
  (bytes->hex (sha256-string->bytes string)))

(defonce debug-level (atom :debug))

(defn set-degug! [] (reset! debug-level :debug))

(defn set-info! [] (reset! debug-level :info))

(defn log [& thing]
  "Log to console, if goog.DEBUG is true"
  (when goog.DEBUG
    (apply println thing)
  ;;TODO write to device logs
  ))

(defn get-ref [component key]
  ((js->clj (.-refs component)) key))

(defn info [thing]
  "Log to console, if goog.DEBUG is true and log level is :info"
  (when (and goog.DEBUG
             (= :debug @debug-level)
             (= :info @debug-level))
    (println thing)))

(defn debug [thing]
  "Log to console, if goog.DEBUG is true and log level is :info of :debug"
  (when (and goog.DEBUG
             (or
              (= :debug @debug-level)
              (= :info @debug-level)))
    (println thing)))

(defn dir [thing]
  "console.dir"
  (.dir js/console thing))

(def moment (js/require "moment"))

(defn to-now [timeString]
  (->
   (moment timeString)
   (.fromNow )))

;; returns the keyword of the current route
(defn current-route
  [navigator]
  (-> navigator
    (.getCurrentRoutes)
    (js->clj :keywordize-keys true)
    ; (butlast)
    (last)
    (:name)
    (str/split #":")
    (last)
    (keyword)))
