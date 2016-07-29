; Copied from https://github.com/contentjon/mocha-latte/blob/master/src/latte/core.clj
; The mocha-latte did not work. Possibly the reason was that is vas rather old and not
; updated to newer version of clojurescript

(ns trustroots.mocha
  "Contains the core macros for generating mocha test suites in BDD style")

(def suite-options
  #{:timeout :skip :only})

(def fixture-options 
  #{})

(def body-option?
  (comp #{:timeout} first))

(def element-option?
  (comp #{:skip :only} first))

(defmulti body-option first)

(defmethod body-option :default [[k v]]
  (-> (str "Unrecognized test suite option: " k)
      (IllegalArgumentException.)
      (throw)))

(defmethod body-option :timeout
  [[_ v]]
  `(~'this-as this#
     (.timeout this# ~v)))

(defn- element-option [options sym]
  (if-let [flag (first (filter element-option? options))]
    (-> sym
        (str "." (-> flag first name))
        (symbol))
    sym))

(defn- split-options 
  ([out valid? forms]
    (if (-> forms first keyword?)
      (let [option (take 2 forms)]
        (when-not (-> option first valid?)
          (-> (str "Option " (first option) " is not valid in this context")
              (IllegalArgumentException.)
              (throw)))
        (recur (cons option out)
               valid?
               (drop 2 forms)))
      [out forms]))
  ([valid? forms]
    (split-options '() valid? forms)))

(defmacro suite-element
  [sym description args & forms]
  (let [[options body] (split-options suite-options forms)]
    `(~(element-option options sym)
       (str ~description)
       (fn [~@args]
         ~@(map body-option (filter body-option? options))
         ~@body
         nil))))

(defmacro fixture-element
  [sym args & forms]
  (let [[options body] (split-options fixture-options forms)]
    `(~sym
       ~@options
       (fn [~@args]
         ~@(map body-option (filter body-option? options))
         ~@body
         nil))))

(defmacro describe
  "Starts a new possible nested test suite. Can contain
   arbitrary code. Special clauses in a suite are:
  
   (it description & forms)       : Specifies a test case with one or more assertions
   (before/after & forms)         : Executes code at the beginning or end of the suite
   (beforeEach/afterEach & forms) : Executes code before or after each 'it' clause
  
   Additionally the description can be followed by these options:
   :timeout An integer specifying the maximum running time for the suite"
  [description & rest]
  `(suite-element js/describe ~description [] ~@rest))

(defmacro it
  "Specifies a test case. A test case can contain arbitrary code if the
   code produces an exception this is interpreted as a failing test.
   
   If the args parameter is a vector with one symbol, a function is passed
   to the test case, which can be called to indicate that test execution
   has fnished. This is primarily useful for asynchronous checks.
  
   The test case description can be followed by these options:
   :timeout An integer specifying the maximum running time for the test case"
  [description args & rest]
  `(suite-element js/it ~description ~args ~@rest))

(defmacro before
  "Runs arbitrary code at the beginning of a test suite.
   
   If the args parameter is a vector with one symbol, a function is passed
   to the before clause, which can be called to indicate that code execution
   has fnished. This is primarily useful for asynchronous setup code.
  
   The before clause acceptsthe following options:
   :timeout An integer specifying the maximum running time for the code"
  [args & body]
  `(fixture-element js/before ~args ~@body))

(defmacro beforeEach 
  "Runs arbitrary code before each test case.
   
   If the args parameter is a vector with one symbol, a function is passed
   to the beforeEach clause, which can be called to indicate that code execution
   has fnished. This is primarily useful for asynchronous setup code.
   
   The beforeEach clause acceptsthe following options:
   :timeout An integer specifying the maximum running time for the code"
  [args & body]
  `(fixture-element js/beforeEach ~args ~@body))

(defmacro after 
  "Runs arbitrary code at the end of a test suite.
   
   If the args parameter is a vector with one symbol, a function is passed
   to the after clause, which can be called to indicate that code execution
   has fnished. This is primarily useful for asynchronous tear down code.
  
   The after clause acceptsthe following options:
   :timeout An integer specifying the maximum running time for the code"
  [args & body]
  `(fixture-element js/after ~args ~@body))

(defmacro afterEach
  "Runs arbitrary code after each test case.
   
   If the args parameter is a vector with one symbol, a function is passed
   to the afterEach clause, which can be called to indicate that code execution
   has fnished. This is primarily useful for asynchronous tear down code.
  
   The afterEach clause acceptsthe following options:
   :timeout An integer specifying the maximum running time for the code"
  [args & body]
  `(fixture-element js/afterEach ~args ~@body))
