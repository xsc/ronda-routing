(ns ronda.routing.request-test
  (:require [midje.sweet :refer :all]
            [ronda.routing
             [descriptor-test :refer [test-descriptor]]
             [request :refer :all]]))

(fact "about setters/getters."
      (let [data (-> {}
                     (set-routing-data {:id :x, :meta {}})
                     (set-descriptor test-descriptor))]
        (routing-data data) => {:id :x, :meta {}}
        (endpoint data) => :x
        (descriptor data) => test-descriptor))

(let [req (-> {:request-method :get
               :uri "/hello/world"}
              (set-routing-data {:id :greet})
              (set-descriptor test-descriptor))
      prm {:greeting "hello", :recipient "you"}
      gen {:path "/hello/you"
           :route-params prm
           :query-params {}}]
  (fact "about generate function w/o query params."
        (generate-for req :greet) => (throws AssertionError)
        (generate-for req :greet prm) => gen)
  (fact "about generate function w/ query params."
        (generate-for req :greet (assoc prm :enthusiasm 9001))
        => (assoc gen :query-params {:enthusiasm 9001}))
  (fact "about the path generation function."
        (path req :greet prm) => "/hello/you")
  (tabular
    (fact "about the path + query string generation function."
          (href req :greet (merge prm ?data)) => ?href)
    ?data              ?href
    {}                 "/hello/you"
    {:enthusiasm 9001} "/hello/you?enthusiasm=9001"
    {:a 0, :b 1}       "/hello/you?a=0&b=1"
    {:escape :+++}     "/hello/you?escape=%2B%2B%2B"))
