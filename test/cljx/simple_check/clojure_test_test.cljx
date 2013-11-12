(ns simple-check.clojure-test-test
  (:require [simple-check.generators :as gen]
            [simple-check.properties :as prop]
            simple-check.core
            [simple-check.clojure-test.runtime :as ct]
            #+clj [clojure.test :refer (is test-var *test-out* *report-counters* *initial-report-counters*)]
            #+cljs [cemerick.cljs.test :refer (*report-counters* *initial-report-counters*)]
            #+cljs [cljs.reader :refer (read-string)]
            #+clj [simple-check.clojure-test :refer (defspec)])
  #+cljs (:require-macros [simple-check.clojure-test :refer (defspec)]
                          [cemerick.cljs.test :refer (is test-var deftesthook)]))

(defspec trial-counts 5000
  (prop/for-all* [gen/int] (constantly true)))

#+clj ; no async testing (yet) in cljs
(defspec long-running-spec 1000
  (prop/for-all* [] #(do (Thread/sleep 1) true)))

(defn- vector-elements-are-unique*
  [v]
  (== (count v) (count (distinct v))))

(def ^:private vector-elements-are-unique
  (prop/for-all*
    [(gen/vector gen/int)]
    vector-elements-are-unique*))

(defspec failing-spec vector-elements-are-unique)

(#+clj defn #+cljs deftesthook test-ns-hook
  []
  (is (-> (with-out-str (binding #+clj [*test-out* *out*] #+cljs [] (test-var #'trial-counts)))
          read-string
          (select-keys [:test-var :result :num-tests])
          (= {:test-var 'trial-counts, :result true, :num-tests 5000})))
  
  (binding [ct/*report-trials* true]
    (let [output (with-out-str (test-var #'trial-counts))]
      (is (re-seq #"(?m)^\.{5}$" output))))

  #+clj ; no async testing (yet) in cljs
  (binding [ct/*report-trials* ct/trial-report-periodic
            ct/*trial-report-period* 500]
    (is (re-seq
         #"(Passing trial \d{3} / 1000 for .+\n)+"
         (with-out-str (binding [*test-out* *out*] (test-var #'long-running-spec))))))

  (let [[report-counters stdout]
        (binding [ct/*report-shrinking* true
                  ; need to keep the failure of failing-spec from affecting the
                  ; simple-check test run
                  *report-counters* (#+clj ref #+cljs atom *initial-report-counters*)]
          [*report-counters*
           (with-out-str (binding #+clj [*test-out* *out*] #+cljs [] (test-var #'failing-spec)))])]
    (is (== 1 (:fail @report-counters)))
    (is (re-find
         #"^Shrinking vector-elements-are-unique starting with parameters .+"
         stdout))))
