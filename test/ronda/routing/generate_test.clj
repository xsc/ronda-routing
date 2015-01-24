(ns ronda.routing.generate-test
  (:require [midje.sweet :refer :all]
            [ronda.routing.generate :refer :all]))

(tabular
  (fact "about path generation from bidi-style vectors."
        (generate-by ?path ?values) => ?result)
  ?path                   ?values           ?result
  "/a"                    {}                "/a"
  ["/" :id]               {:id 1}           "/1"
  ["/" :id "-a"]          {:id 1}           "/1-a"
  ["/" [#"\d+" :id] "-a"] {:id 1}           "/1-a")

(tabular
  (fact "about path generation exceptions."
        (generate-by ?path ?values) => (throws Exception ?pattern))
  ?path              ?values     ?pattern
  ["/" :id]          {}          #"missing"
  ["/" [#"\d+" :id]] {:id "abc"} #"not compatible")
