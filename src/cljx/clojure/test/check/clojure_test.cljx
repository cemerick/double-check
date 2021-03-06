;   Copyright (c) Rich Hickey, Reid Draper, and contributors.
;   All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns clojure.test.check.clojure-test
  (:require clojure.test.check))

(defn- maybe-var
  [cljs? name]
  (if cljs? name (list 'var name)))

(defmacro defspec
  "Defines a new clojure.test test var that uses `quick-check` to verify
  [property] with the given [args] (should be a sequence of generators),
  [default-times] times by default.  You can call the function defined as [name]
  with no arguments to trigger this test directly (i.e., without starting a
  wider clojure.test run), with a single argument that will override
  [default-times], or with a map containing any of the keys
  [:seed :max-size :num-tests]. "
  {:arglists '([name property] [name num-tests? property] [name options? property])} 
  [name & args]
  (let [entry-sym (gensym (clojure.core/name name))
        test-fn (gensym (str (clojure.core/name name) "-test"))
        property (last args)]
    `(let [options# (clojure.test.check.clojure-test.runtime/process-options ~(first (butlast args)))
           ~entry-sym (fn ~entry-sym
                        ([] (apply ~entry-sym (:num-tests options#) (apply concat options#)))
                        ([times# & opts#]
                           (apply clojure.test.check/quick-check
                             times#
                             (vary-meta ~property assoc :name (str '~property))
                             opts#)))
           ~test-fn (fn [& [test-ctx#]]
                      (~(if (:ns &env) 'cemerick.cljs.test/with-test-ctx 'do)
                       test-ctx#
                       (~(maybe-var (:ns &env) 'clojure.test.check.clojure-test.runtime/assert-check)
                        (assoc (~entry-sym) :test-var (str '~name)))))]
       (do
         ; :declared metadata eliminates clojurescript warnings re: a fn
         ; defined without defn not statically a fn
         (def ~(with-meta name (if (:ns &env)
                                 {:declared true}
                                 {:test test-fn}))
           ~(if (:ns &env)
              `(vary-meta ~entry-sym assoc :test ~test-fn)
              entry-sym))
         ~(when (:ns &env)
            ; not having cljs, cljs.test around as a regular dependency hurts here some :-P
            `(cemerick.cljs.test/register-test! '~(eval 'cljs.analyzer/*cljs-ns*)
               ~(->> [(eval 'cljs.analyzer/*cljs-ns*) name]
                  (map str)
                  (apply symbol))
               ~((eval '(var cemerick.cljs.test/munged-symbol))
                 (eval 'cljs.analyzer/*cljs-ns*) "." name)))
         ~(maybe-var (:ns &env) name)))))

