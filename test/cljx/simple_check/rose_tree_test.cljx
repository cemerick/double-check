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
