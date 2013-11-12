(ns simple-check.clojure-test
  (:require simple-check.core))

(def ^:dynamic *default-test-count* 100)

(defmacro defspec
  "Defines a new clojure.test test var that uses `quick-check` to verify
  [property] with the given [args] (should be a sequence of generators),
  [default-times] times by default.  You can call the function defined as [name]
  with no arguments to trigger this test directly (i.e.  without starting a
  wider clojure.test run), or with a single argument that will override
  [default-times]."
  ([name property]
     `(defspec ~name nil ~property))
  ([name default-times property]
     `(do
        (defn ~name 
          ([] (~name (or ~default-times *default-test-count*)))
          ([times# & {:keys [seed# max-size#] :as quick-check-opts#}]
             (apply simple-check.core/quick-check
                    times#
                    (vary-meta ~property assoc :name (str '~property))
                    (flatten (seq quick-check-opts#)))))
        ~(if (:ns &env)
           ; :declared metadata eliminates clojurescript warnings re: a defn no
           ; longer statically a fn
           `(do
              (def ~(with-meta name {:declared true})
               (vary-meta ~name assoc :test
                          (fn [] (simple-check.clojure-test.runtime/assert-check
                                  (assoc (~name) :test-var (str '~name))))))
              ; not having cljs, cljs.test around as a regular dependency hurts here some :-P
              (cemerick.cljs.test/register-test! '~(eval 'cljs.analyzer/*cljs-ns*)
                                               ~((eval '(var cemerick.cljs.test/munged-symbol))
                                                 (eval 'cljs.analyzer/*cljs-ns*) "." name)))
           `(alter-meta! (var ~name) assoc :test
                         (fn [] (#'simple-check.clojure-test.runtime/assert-check
                                 (assoc (~name) :test-var (str '~name)))))))))

