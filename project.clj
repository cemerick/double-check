(defproject reiddraper/simple-check "0.5.4-SNAPSHOT"
  :description "A QuickCheck inspired property-based testing library."
  :url "http://github.com/reiddraper/simple-check"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj" "src/cljx" "target/classes"]
  :test-paths ["target/test-classes"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.cemerick/pprng "0.0.2"]]
  :jar-exclusions [#"\.cljx"]
  :global-vars {*warn-on-reflection* true}
  :codox {:writer codox-md.writer/write-docs}
  :plugins [[codox "0.6.4"]]

  :profiles {:dev {:dependencies [[org.clojure/clojurescript "0.0-2014"]
                                  [codox-md "0.2.0" :exclusions [org.clojure/clojure]]]
                   :plugins [[com.keminglabs/cljx "0.3.2-SNAPSHOT"]
                             [lein-cljsbuild "1.0.0-alpha2"]
                             [com.cemerick/clojurescript.test "0.2.0"]
                             [com.cemerick/austin "0.1.3"]]
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

                   :aliases {"cleantest" ["do" "clean," "cljx" "once," "test," "cljsbuild" "test"]}

                   :cljsbuild {:test-commands {"phantom" ["phantomjs" :runner "target/testable.js"]}
                               :builds [{:source-paths ["target/classes" "target/test-classes"]
                                         :compiler {:output-to "target/testable.js"
                                                    :libs [""]
                                                    :source-map "target/testable.js.map"
                                                    :optimizations :advanced}}]}}})
