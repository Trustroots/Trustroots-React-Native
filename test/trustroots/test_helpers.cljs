(ns trustroots.test-helper
  (:require [latte.chai :as c])
  )

(comment 
 Usage example

 (def tests
  {
   "test nested test"
   {
    :after #(println "after")
    :afterEach #(println "afterEach")
    :before #(println "before")
    :beforeEach #(println "beforeEach")

    "tämä testi menee läpi"
    (fn []
      (expect true :to.be.ok)
      )
    "tämä testi failaa"
    (fn []
      (is (= 1 2))
      (expect false :to.be.ok)
      )
    }
   })

)

(def expect c/expect)

(defn apply-if-fn [value-or-fn]
  (if (fn? value-or-fn)
    (apply value-or-fn nil)
    value-or-fn
  ))

(defmulti  apply-fn (fn [type value] type))
(defmethod apply-fn :before         [t f] (js/before f))
(defmethod apply-fn :beforeEach     [t f] (js/beforeEach f))
(defmethod apply-fn :after          [t f] (js/after f))
(defmethod apply-fn :afterEach      [t f] (js/afterEach f))


(defmulti  assert-fn (fn [& tokens]
                       (first tokens)))

(defmethod assert-fn :truthy? [_ v & tokens]
  [#(expect (apply-if-fn v) :to.be.ok)
  tokens]
  )

(defmethod assert-fn :falsy? [_ v & tokens]
  [#(expect (apply-if-fn v) :to.not.be.ok)
   tokens]
  )

(defmethod assert-fn :nil? [_ v & tokens]
  [
   #(expect (apply-if-fn v) :to.be.equal nil)
   tokens
   ])

(defmethod assert-fn :not-nil? [_ v & tokens]
  [#(expect (not (nil? (apply-if-fn v))) :to.be.ok)
  tokens]
  )

(defmethod assert-fn :check [& tokens]
  :pre (vector? tokens)
  (fn []
    (loop [t (rest tokens)]
      (let [[parsed-fn remaining] (apply assert-fn t)]
        (parsed-fn)
        (when-not (empty? remaining)
          (recur remaining))))))

(defmethod assert-fn :act [_ f & tokens]
  :pre (fn? f)
  [f tokens])

(defmethod assert-fn := [_ fst snd & tokens]
  [
  #(let [fst' (apply-if-fn fst)
         snd' (apply-if-fn snd)]
     (expect fst' :to.equal snd'))
   tokens
   ])

(defn with-asserts [check-fns]
   (apply assert-fn (concat [:check] check-fns))
  )

(defn do-asserts [check-fns]
  (apply (with-asserts check-fns) nil)
  )


(defn specs [tests]
  :pre (map? tests)
  (doall (map
           (fn [[key value]]
             (cond
               (keyword? key)  (apply-fn key value)
               (fn?  value)    (js/it key value)
               (vector? value) (js/it key (with-asserts value))
               (map? value)    (js/describe key #(specs value))
               :else           (println "Unexpected node in the test"))
             )
           tests
           )))
