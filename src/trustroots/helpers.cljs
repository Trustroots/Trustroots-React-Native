(ns trustroots.helpers)

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
