(defproject com.cemerick/double-check "0.6.1-SNAPSHOT"
  :description "A QuickCheck inspired property-based testing library."
  :url "http://github.com/cemerick/double-check"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj" "src/cljx" "target/classes"]
  :test-paths ["target/test-classes"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.cemerick/pprng "0.0.3-SNAPSHOT"]
                 [com.cemerick/clojurescript.test "0.3.1"]]
  :jar-exclusions [#"\.cljx"]
  :codox {:defaults {:doc/format :markdown}}
  :plugins [[codox "0.8.10"]]

  :profiles {:dev {:dependencies [[org.clojure/clojurescript "0.0-2227"]
                                  [codox-md "0.2.0" :exclusions [org.clojure/clojure]]]
                   :plugins [[com.keminglabs/cljx "0.3.3-SNAPSHOT"]
                             [lein-cljsbuild "1.0.3"]
                             [com.cemerick/clojurescript.test "0.3.1"]
                             [com.cemerick/austin "0.1.5-SNAPSHOT"]]
                   :cljx {:builds [{:source-paths ["src/cljx"]
                                    :output-path "target/classes"
                                    :rules :clj}

                                   {:source-paths ["src/cljx"]
                                    :output-path "target/classes"
                                    :rules :cljs}

                                   {:source-paths ["test/cljx"]
                                    :output-path "target/test-classes"
                                    :rules :clj}

                                   {:source-paths ["test/cljx"]
                                    :output-path "target/test-classes"
                                    :rules :cljs}]}

                   :aliases {"cleantest" ["do" "clean," "cljx" "once," "test," "cljsbuild" "test"]
                             "deploy" ["do" "clean," "cljx" "once," "deploy" "clojars"]}

                   :cljsbuild {:test-commands {"phantom" ["phantomjs" :runner "target/testable.js"]
                                               "node" ["node" :node-runner "target/testable.js"]}
                               :builds [{:source-paths ["target/classes" "target/test-classes"]
                                         :compiler {:output-to "target/testable.js"
                                                    ; node doesn't like source maps I guess?
                                                    ;:source-map "target/testable.js.map"
                                                    :optimizations :advanced}}]}}})
