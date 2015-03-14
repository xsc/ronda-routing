(ns ronda.routing.meta-middleware-test
  (:require [midje.sweet :refer :all]
            [ronda.routing
             [simple :as simple]
             [middleware :refer [wrap-routing]]
             [meta-middleware :as m]
             [middleware-data :as md]]))

(let [d (-> (simple/descriptor
              {:a "/a"
               :b "/b"})
            (md/enable-middlewares
              :a {:m {:v "hello"}}
              :b {:m {:v "bye"}}))
      h (constantly {:status 200})]
  (tabular
    (let [m (fn [handler _ {:keys [v]}]
              (fn [request]
                (assoc (handler request) :body v)))]
      (fact "about metadata-dependent middlewares."
            (let [h' (-> h
                         (m/meta-middleware :m m)
                         (wrap-routing d))]
              (:body (h' {:request-method :get, :uri ?uri})) => ?body)))
    ?uri    ?body
    "/a"    "hello"
    "/b"    "bye"
    "/c"    nil))
