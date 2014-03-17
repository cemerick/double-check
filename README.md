# double-check [![Build Status](https://secure.travis-ci.org/cemerick/double-check.png)](http://travis-ci.org/cemerick/double-check)

__double-check__ is a fork of [@reiddraper](http://github.com/reiddraper)'s
[test.check](https://github.com/clojure/test.check) (née
[simple-check](https://github.com/reiddraper/simple-check)) library, a
property-based testing tool inspired by QuickCheck.  The core idea of
test.check (and thus, __double-check__) is that instead of enumerating
expected input and output for unit tests, you write properties about your
function that should hold true for all inputs. This lets you write concise,
powerful tests.

## Why a fork?

While test.check is dedicated to remaining a Clojure-only API (at least for
now?) __double-check__ is written using
[cljx](http://github.com/keminglabs/cljx), and thus provides an API that is
portable between Clojure and ClojureScript.  _This approach has already
uncovered significant bugs in ClojureScript itself, and can do the same for your
programs._

Please note that this fork:

1. ...always tracks test.check as closely as possible, with the only divergences
being those necessary to ensure an API portable between Clojure and
ClojureScript.
2. ...is not a rewrite.  The move to cljx yields minimal changes compared to the
test.check baseline; 100% of the core logic flows from it.  There's nothing
novel here.
3. ...is not hostile in any way to test.check, @reiddraper, etc.  (It's
actually
[@reiddraper-approved](https://github.com/reiddraper/simple-check/issues/11#issuecomment-31963517)!
:-P) It exists solely to make it possible to apply test.check's testing
approach to ClojureScript libraries and applications, not to supplant or compete
with test.check.  In particular, the core abstractions and generator/shrinking
implementation defined in test.check are considered canonical.  If
test.check eventually provides equivalent functionality for the ClojureScript
side of the house, this project will be shuttered.
4. ...does not make any guarantees about 100% API compatibility with test.check,
though it is based upon and tracks it.  i.e. you should not expect to be able to
move from test.check to double-check (or vice versa) in a Clojure project with
no changes.  Except for the (slightly) different namespaces, no changes are
known to be required right now, but that _may_ change to maximize runtime target
portability.
5. ..._does not_ retain the `clojure.test.check.*` namespace structure.  The
simple rule is, `s/clojure.test.check/cemerick.double-check/g`.  While it would
be convenient to do so (in order to make tracking easier, and to allow users to
share/port examples and usage of each package back and forth with a minimum of
pain), I am personally not comfortable distributing a fork of a Clojure
"contrib" library using the same namespaces.

A word on versioning: `[com.cemerick/double-check]` version numbers will track
test.check version numbers as well, using a suffixed classifier (e.g. 0.1.2
turns into 0.1.2-1) to indicate local changes.  `SNAPSHOT` version numbers will
be the same as test.check's.

## Releases and Dependency Information

### Leiningen

Add this to your `:dependencies`:

```clojure
[com.cemerick/double-check "0.5.6-SNAPSHOT"]
```

...and make sure you add this to your
[cljsbuild](https://github.com/emezeske/lein-cljsbuild) `:compiler` options:

```clojure
:libs [""]
```

(This is temporary workaround will bring in the JavaScript portion of the
[portable random number generator](http://github.com/cemerick/pprng) for your
ClojureScript builds; this will be necessary until
[CLJS-656](http://dev.clojure.org/jira/browse/CLJS-656) is resolved.)

### Maven

```xml
<dependency>
  <groupId>com.cemerick</groupId>
  <artifactId>double-check</artifactId>
  <version>0.5.6-SNAPSHOT</version>
</dependency>
```

## Documentation

  * [Generator writing guide](doc/intro.md)
  * Examples (some of these may refer to test.check):
    * [core.matrix](https://github.com/mikera/core.matrix/blob/c45ee6b551a50a509e668f46a1ae52ade2c52a82/src/test/clojure/clojure/core/matrix/properties.clj)
    * [byte-streams](https://github.com/ztellman/byte-streams/blob/b5f50a20c6237ae4e45046f72367ad658090c591/test/byte_streams_simple_check.clj)
    * [byte-transforms](https://github.com/ztellman/byte-transforms/blob/c5b9613eebac722447593530531b9aa7976a0592/test/byte_transforms_simple_check.clj)
    * [collection-check](https://github.com/ztellman/collection-check)
  * Blog posts and videos (some of these may refer to test.check):
    * [Check your work - 8th Light](http://blog.8thlight.com/connor-mendenhall/2013/10/31/check-your-work.html)
    * [Writing simple-check - Reid Draper](http://reiddraper.com/writing-simple-check/)
    * [Generative testing in Clojure - Youtube](https://www.youtube.com/watch?v=u0TkAw8QqrQ)
    * [Using simple-check with Expectations - Curtis Gagliardi](http://curtis.io/posts/2013-12-28-using-simple-check-with-expectations.html)

## Migrating from simple-check or test.check

In order to migrate from _simple-check_ or _test.check_ to _double-check_, you'll need to do two
things:

* Update project.clj

    In your `project.clj` replace `[reiddraper/simple-check "$VERSION"]` or
    `[org.clojure/test.check "$VERSION"]` with
    `[com.cemerick/double-check "$LATEST_VERSION_HERE"]` (note: your version
    numbers may be different).

* Update namespace declarations

    Update your namespaces: `simple-check.core` becomes `cemerick.double-check`
    (note the dropping of 'core').  For all other things, just replace
    `simple-check` or `clojure.test.check` with `cemerick.double-check`. Let's
    make it easy:

    ```shell
    find test -name '*.clj' -print0 | xargs -0 sed -i.bak \
    -e 's/simple-check.core/cemerick.double-check/' \
    -e 's/simple-check/cemerick.double-check/' \
    -e 's/clojure.test.check/cemerick.double-check/'
    ```

    Review the updates.

## Examples

Let's say we're testing a sort function. We want want to check that that our
sort function is idempotent, that is, applying sort twice should be
equivalent to applying it once: `(= (sort a) (sort (sort a)))`. Let's write a
quick test to make sure this is the case:

```clojure
(ns double-check.demos
  (:require [cemerick.double-check.core :as sc]
            [cemerick.double-check.generators :as gen]
            [cemerick.double-check.properties :as prop]))
; in ClojureScript, the last :require would be replaced with:
; (:require-macros [cemerick.double-check.properties :as prop])

(def sort-idempotent-prop
  (prop/for-all [v (gen/vector gen/int)]
    (= (sort v) (sort (sort v)))))

(tc/quick-check 100 sort-idempotent-prop)
;; => {:result true, :num-tests 100, :seed 1382488326530}
```

In prose, this test reads: for all vectors of integers, `v`, sorting `v` is
equal to sorting `v` twice.

What happens if our test fails? _test.check_ will try and find 'smaller'
input that still fails. This process is called shrinking. Let's see it in
action:

```clojure
(def prop-sorted-first-less-than-last
  (prop/for-all [v (gen/such-that not-empty (gen/vector gen/int))]
    (let [s (sort v)]
      (< (first s) (last s)))))

(tc/quick-check 100 prop-sorted-first-less-than-last)
;; => {:result false, :failing-size 0, :num-tests 1, :fail [[3]],
       :shrunk {:total-nodes-visited 5, :depth 2, :result false,
                :smallest [[0]]}}
```

This test claims that the first element of a sorted vector should be less-than
the last. Of course, this isn't true: the test fails with input `[3]`, which
gets shrunk down to `[0]`, as seen in the output above. As your test functions
require more sophisticated input, shrinking becomes critical to being able
to understand exactly why a random test failed. To see how powerful shrinking
is, let's come up with a contrived example: a function that fails if its
passed a sequence that contains the number 42:

```clojure
(def prop-no-42
  (prop/for-all [v (gen/vector gen/int)]
    (not (some #{42} v))))

(tc/quick-check 100 prop-no-42)
;; => {:result false,
       :failing-size 45,
       :num-tests 46,
       :fail [[10 1 28 40 11 -33 42 -42 39 -13 13 -44 -36 11 27 -42 4 21 -39]],
       :shrunk {:total-nodes-visited 38,
                :depth 18,
                :result false,
                :smallest [[42]]}}
```

We see that the test failed on a rather large vector, as seen in the `:fail`
key. But then _test.check_ was able to shrink the input down to `[42]`, as
seen in the keys `[:shrunk :smallest]`.

To learn more, check out the [documentation](#documentation) links.

### `clojure.test` Integration

The `cemerick.double-check.clojure-test/defspec` macro allows you to succinctly write
properties that run under `clojure.test` (or
[clojurescript.test](http://github.com/cemerick/clojurescript.test), as
appropriate).  For example:

```clojure
(defspec first-element-is-min-after-sorting ;; the name of the test
         100 ;; the number of iterations for test.check to test
         (prop/for-all [v (such-that not-empty (gen/vector gen/int))]
           (= (apply min v)
              (first (sorted v)))))
```

This defines a standard clojure.test / clojurescript.test test, which can be
invoked directly to run only it.  Or, you can run all of the tests in a
namespace or the entire environment with the `test-ns` and `run-all-tests`
utility functions in clojure.test and clojurescript.test.

See more examples in [`core_test.clj`](test/cljx/simple_check/core_test.cljx).

## Release Notes

Release notes for each version are available in
[`CHANGELOG.markdown`](CHANGELOG.markdown). Changes that are specific to
double-check (i.e. that don't flow from upstream changes in test.check) are
noted as such.

## See also...

### Other implementations

- [QC for Haskell](http://hackage.haskell.org/package/QuickCheck)
- [The significantly more advanced QC for
  Erlang](http://www.quviq.com/index.html)

### Papers

- [QuickCheck: A Lightweight Tool for Random Testing of Haskell
  Programs](http://www.eecs.northwestern.edu/~robby/courses/395-495-2009-fall/quick.pdf)

## Contributing

We can not accept pull requests. Please see [CONTRIBUTING.md](CONTRIBUTING.md)
for details.

## License

Copyright © 2013 Reid Draper and other contributors

Distributed under the Eclipse Public License, the same as Clojure.
