(ns ronda.routing.middleware-test
  (:require [midje.sweet :refer :all]
            [ronda.routing
             [descriptor-test :refer [test-descriptor]]
             [middleware :refer :all]
             [request :as rq]]))

(def handlers
  {:greet (fn [{:keys [params] :as request}]
            {:status 200
             :endpoint (rq/endpoint request)
             :descriptor (rq/descriptor request)
             :body (format "%s, %s! (%s)"
                           (clojure.string/capitalize (:greeting params))
                           (clojure.string/capitalize (:recipient params))
                           (->> {:greeting "cheerio"
                                 :recipient "miss-sophie"}
                                (rq/href request :greet)))})})

(fact "about the abstract routing middleware."
      (let [a (atom nil)
            h (wrap-routing
                #(do (reset! a %) nil)
                test-descriptor)
            r (h {:request-method :get
                  :uri "/hello/world"})
            {:keys [request-method uri] :as request} @a]
        r => nil?
        request-method => :get
        uri => "/hello/world"
        (rq/endpoint request) => :greet
        (rq/descriptor request) => test-descriptor))

(let [h (-> (constantly {:status 404})
            (wrap-endpoints handlers)
            (wrap-routing test-descriptor))]
  (fact "about the concrete routing middleware."
        (let [{:keys [status body endpoint descriptor]}
              (h {:request-method :get
                  :uri "/hello/world"})]
          status => 200
          body => "Hello, World! (/cheerio/miss-sophie)"
          endpoint => :greet
          descriptor => test-descriptor)
        (:status
          (h {:request-method :get
              :uri "/hola"})) => 404))
