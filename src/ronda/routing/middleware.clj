(ns ronda.routing.middleware
  (:require [ronda.routing
             [descriptor :as describe]
             [request :as r]]))

(defn wrap-routing
  "Wrap the given Ring handler with middleware-based routing using the given
   RouteDescriptor. If a route can be found for a request the descriptor,
   route params and endpoint ID are injected into the request map which is then
   passed to the original handler. If no route can be found, the request is passed
   to the handler unaltered."
  [handler descriptor]
  (fn [request]
    (let [request' (r/set-descriptor request descriptor)]
      (if-let [{:keys [route-params] :as data} (describe/match-request descriptor request)]
        (-> request'
            (update-in [:route-params] (fnil merge {}) route-params)
            (update-in [:params] (fnil merge {}) route-params)
            (r/set-routing-data data)
            (handler))
        (handler request')))))

(defn wrap-endpoints
  "Create a Ring handler that intercepts all requests that had an endpoint ID
   determined by `wrap-routing` and routes them to the matching handler in
   the given handlers map. If no match is found, the request is passed to the
   given default handler."
  [default-handler handlers]
  {:pre [(map? handlers)]}
  (fn [request]
    (if-let [endpoint-id (r/endpoint request)]
      (if-let [h (get handlers endpoint-id)]
        (h request)
        (default-handler request))
      (default-handler request))))

(defn wrap-endpoint
  "Create a Ring handler that intercepts all requests that had the given
   endpoint ID determined by `wrap-routing`, delegating them to the given
   handler."
  [default-handler endpoint-id handler]
  (wrap-endpoint default-handler {endpoint-id handler}))

(defn compile-endpoints
  "Create a Ring handler that intercepts all requests that had an endpoint ID
   determined by `wrap-routing` and routes them to the matching handler in the
   given handlers map. If no match is found, `nil` is returned."
  [handlers]
  (wrap-endpoints
    (constantly nil)
    handlers))
