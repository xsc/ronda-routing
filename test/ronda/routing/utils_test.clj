(ns ronda.routing.utils-test
  (:require [midje.sweet :refer :all]
            [ronda.routing.utils :refer :all]))

(tabular
  (fact "about string coercion."
        (->str ?in) => ?out)
  ?in        ?out
  "a"        "a"
  \a         "a"
  1          "1"
  :a         "a"
  'a         "a")

(fact "about string coercion for map values."
      (stringify-vals
        {:a "a", :b :b,
         :c 1, :d 'd}) => {:a "a" :b "b" :c "1" :d "d"})

(tabular
  (fact "about URL encoding."
        (urlencode ?in) => ?out)
  ?in         ?out
  "a"         "a"
  :a          "a"
  "+"         "%2B"
  "hi world"  "hi%20world")
