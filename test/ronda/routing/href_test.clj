(ns ronda.routing.href-test
  (:require [midje.sweet :refer :all]
            [ronda.routing
             [descriptor-test :refer [test-descriptor]]
             [href :refer :all]]))
(let [prm {:greeting "hello", :recipient "you"}
      gen {:path "/hello/you"
           :route-params prm
           :query-params {}}]
  (tabular
    (fact "about the path + query string generation function."
          (href
            test-descriptor
            :greet
            (merge prm ?data)) => ?href)
    ?data              ?href
    {}                 "/hello/you"
    {:enthusiasm 9001} "/hello/you?enthusiasm=9001"
    {:a 0, :b 1}       "/hello/you?a=0&b=1"
    {:escape :+++}     "/hello/you?escape=%2B%2B%2B"))
