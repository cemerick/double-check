;   Copyright (c) Rich Hickey, Reid Draper, and contributors.
;   All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns simple-check.rose-tree-test
  (:require [simple-check.core       :as sc]
            [simple-check.generators :as gen]
            [simple-check.properties :as prop :refer (#+clj for-all)]
            #+clj [simple-check.clojure-test :as ct :refer (defspec)]
            #+clj [clojure.test :refer (is testing deftest test-var)])
  #+cljs (:require-macros [simple-check.clojure-test :refer (defspec)]
                          [simple-check.properties :refer (for-all)]
                          [cemerick.cljs.test :refer (is testing deftest test-var)]))

(defn depth-one-children
  [[root children]]
  (into [] (map gen/rose-root children)))

(defn depth-one-and-two-children
  [[root children]]
  (into []
        (concat (map gen/rose-root children)
                (map gen/rose-root (mapcat gen/rose-children children)))))

(defspec test-collapse-rose
  100
  (for-all [i gen/int]
           (let [tree (#+clj #'gen/int-rose-tree #+cljs gen/int-rose-tree i)]
             (= (depth-one-and-two-children tree)
                (depth-one-children (gen/collapse-rose tree))))))
