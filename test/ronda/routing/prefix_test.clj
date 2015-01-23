(ns ronda.routing.prefix-test
  (:require [midje.sweet :refer :all]
            [ronda.routing
             [descriptor :as describe]
             [prefix :refer [prefix]]]))

(defrecord SingleDescriptor [sq]
  describe/RouteDescriptor
  (prefix-string [this s]
    (update-in this [:sq] conj s))
  (prefix-route-param [this k p]
    (update-in this [:sq] conj (if p [p k] k)))
  (routes [_]
    {:endpoint {:path (vec sq)
                :methods #{:get}}}))

(let [base-descriptor (->SingleDescriptor '())
      digit #"\d"]
  (tabular
    (fact "about prefix logic."
          (let [r (prefix base-descriptor ?prefix)]
            (-> (describe/routes r) :endpoint :path) => ?route))
    ?prefix                  ?route
    nil                      []
    "/hello/"                ["/hello/"]
    :param                   [:param]
    ["/hello/" :param]       ["/hello/" :param]
    [digit :n]               [[digit :n]]
    ["/a/" [digit :n] "/"]   ["/a/" [digit :n] "/"])
  (fact "about invalid routes."
        (prefix base-descriptor ["/" ["/ab"]])
        => (throws AssertionError)))
