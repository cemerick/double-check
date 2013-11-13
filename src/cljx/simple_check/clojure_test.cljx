(ns simple-check.clojure-test
  (:require simple-check.core))

(def ^:dynamic *default-test-count* 100)

(defn- maybe-var
  [cljs? name]
  (if cljs? name (list 'var name)))

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
     (let [entry-sym (gensym (clojure.core/name name))
           test-fn (gensym (str (clojure.core/name name) "-test"))]
       `(let [~entry-sym (fn ~entry-sym
                            ([] (~entry-sym (or ~default-times *default-test-count*)))
                            ([times# & {:keys [seed# max-size#] :as quick-check-opts#}]
                               (apply simple-check.core/quick-check
                                      times#
                                      (vary-meta ~property assoc :name (str '~property))
                                      (flatten (seq quick-check-opts#)))))
              ~test-fn (fn [] (~(maybe-var (:ns &env) 'simple-check.clojure-test.runtime/assert-check)
                                (assoc (~entry-sym) :test-var (str '~name))))]
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
                                                 ~((eval '(var cemerick.cljs.test/munged-symbol))
                                                   (eval 'cljs.analyzer/*cljs-ns*) "." name)))
            ~(maybe-var (:ns &env) name))))))

