(defproject trustroots "0.1.0-SNAPSHOT"
            :description "FIXME: write description"
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.8.0"]
                           ;[org.clojure/clojurescript "1.8.51"]
                           [org.clojure/clojurescript "1.9.89"]

                           [reagent "0.6.0-rc" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                           [re-frame  "0.7.0"]
                           [cljs-ajax "0.5.3"]
                           [prismatic/schema "1.1.3"]
                           [lein-doo "0.1.7"]]
            :plugins [[lein-cljsbuild "1.1.3"]
                      [lein-figwheel "0.5.0-6"]]
            :clean-targets ["target/" "index.ios.js" "index.android.js"]
            :aliases {"prod-build" ^{:doc "Recompile code with prod profile."}
                                   ["do" "clean"
                                    ["with-profile" "prod" "cljsbuild" "once" "ios"]
                                    ["with-profile" "prod" "cljsbuild" "once" "android"]]}

            ;; test build
            ;; currently device is not automatically faked and thus testing
            ;; anything having a something to do with device is not possible.
            :hooks [leiningen.cljsbuild]
            :cljsbuild { :test-commands
                                   {"unit" ["node" "run-test.js"]}
                        :builds   {:test {:source-paths ["test" "src"]
                                          :notify-command ["node" "run-test.js"]
                                          :compiler     {
                                                           :output-to     "target/test/test.js"
                                                           :main          "trustroots.runner"
                                                           :target        :nodejs
                                                           :closure-defines {"goog.DEBUG" true
                                                                             "GLOBAL.__DEV__"    true
                                                                             }
                                                           :output-dir    "target/test"
                                                           :optimizations :none}}}}

            ;; build actual app
            :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.0-6"]
                                            [com.cemerick/piggieback "0.2.1"]]
                             :source-paths ["src" "env/dev"]
                             :cljsbuild    {:builds {:ios     {:source-paths ["src" "env/dev"]
                                                               :figwheel     true
                                                               :compiler     {:output-to     "target/ios/not-used.js"
                                                                              :main          "env.ios.main"
                                                                              :output-dir    "target/ios"
                                                                              :optimizations :none}}
                                                     :android {:source-paths ["src" "env/dev"]
                                                               :figwheel     true
                                                               :compiler     {:output-to     "target/android/not-used.js"
                                                                              :main          "env.android.main"
                                                                              :output-dir    "target/android"
                                                                              :optimizations :none}}}}
                             :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
                       :prod {:cljsbuild {:builds {:ios     {:source-paths ["src" "env/prod"]
                                                             :compiler     {:output-to     "index.ios.js"
                                                                            :main          "env.ios.main"
                                                                            :output-dir    "target/ios"
                                                                            :static-fns    true
                                                                            :optimize-constants true
                                                                            :optimizations :simple
                                                                            :closure-defines {"goog.DEBUG" false}}}
                                                   :android {:source-paths ["src" "env/prod"]
                                                             :compiler     {:output-to     "index.android.js"
                                                                            :main          "env.android.main"
                                                                            :output-dir    "target/android"
                                                                            :static-fns    true
                                                                            :optimize-constants true
                                                                            :optimizations :simple
                                                                            :closure-defines {"goog.DEBUG" false}}}}}
                              }})
