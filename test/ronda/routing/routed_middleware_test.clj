(ns ronda.routing.routed-middleware-test
  (:require [midje.sweet :refer :all]
            [ronda.routing
             [simple :as simple]
             [middleware :refer [wrap-routing]]
             [middleware-data :as md]
             [routed-middleware :as m]]))

(let [d (-> (simple/descriptor
              {:a "/enable"
               :b "/disable"
               :c "/default"
               :d "/enable2"})
            (md/enable-middlewares  :a [:m] :d [:m])
            (md/disable-middlewares :b [:m]))
      m #(fn [request]
           (% (assoc request :middleware true)))
      h (fn [request]
          {:status 200,
           :body (format "middleware: %s"
                         (boolean (:middleware request)))})]
  (tabular
    (fact "about middleware enabling."
          (let [h' (-> h
                       (m/routed-middleware :m m)
                       (wrap-routing d))]
            (:body (h' {:request-method :get, :uri ?uri})) => ?body))
    ?uri       ?body
    "/enable"  "middleware: true"
    "/disable" "middleware: false"
    "/default" "middleware: false")
  (tabular
    (fact "about middleware disabling."
          (let [h' (-> h
                       (m/active-routed-middleware :m m)
                       (wrap-routing d))]
            (:body (h' {:request-method :get, :uri ?uri})) => ?body))
    ?uri       ?body
    "/enable"  "middleware: true"
    "/disable" "middleware: false"
    "/default" "middleware: true"))
