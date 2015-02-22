(ns ronda.routing.meta-middleware-test
  (:require [midje.sweet :refer :all]
            [ronda.routing
             [simple :as simple]
             [middleware :refer [wrap-routing]]
             [meta-middleware :as m]]))

(let [d (-> (simple/descriptor
              {:a "/enable"
               :b "/disable"
               :c "/default"})
            (m/enable-middlewares  :a [:m])
            (m/disable-middlewares :b [:m]))
      m #(fn [request]
           (% (assoc request :middleware true)))
      h (fn [request]
          {:status 200,
           :body (format "middleware: %s"
                         (boolean (:middleware request)))})]
  (tabular
    (fact "about middleware enabling."
          (let [h' (-> h
                       (m/meta-middleware :m m)
                       (wrap-routing d))]
            (:body (h' {:request-method :get, :uri ?uri})) => ?body))
    ?uri       ?body
    "/enable"  "middleware: true"
    "/disable" "middleware: false"
    "/default" "middleware: false")
  (tabular
    (fact "about middleware disabling."
          (let [h' (-> h
                       (m/meta-middleware :m m {:enabled? true})
                       (wrap-routing d))]
            (:body (h' {:request-method :get, :uri ?uri})) => ?body))
    ?uri       ?body
    "/enable"  "middleware: true"
    "/disable" "middleware: false"
    "/default" "middleware: true"))
