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

(defn conditional-middleware
  "Apply the given middleware only if the request matches the given predicate."
  [handler p? wrap-fn]
  (let [wrapped-handler (wrap-fn handler)]
    (fn conditional-handler
      [request]
      (if (p? request)
        (wrapped-handler request)
        (handler request)))))

(defn conditional-transform
  "Apply the given transformation function to the request, only if it matches
   the given predicate."
  [handler p? f]
  (fn conditional-transform-handler
    [request]
    (handler
      (if (p? request)
        (f request)
        request))))

(defn- routed-pred
  "Generate predicate that checks whether a given request is being routed
   to one of the given endpoints."
  [endpoints]
  (comp
    (if (sequential? endpoints)
      (set endpoints)
      #{endpoints})
    r/endpoint))

(defn endpoint-middleware
  "Like `conditional-middleware`, applying the given middleware only if the
   request is being routed to one of the given endpoints. (A single
   non-sequential endpoint ID may be given instead of a seq of multiple.)"
  [handler endpoints wrap-fn]
  (conditional-middleware
    handler
    (routed-pred endpoints)
    wrap-fn))

(defn endpoint-transform
  "Like `conditional-transform`, applying the given function to requests
   that are being routed to one of the given endpoints. (A single non-sequential
   endpoint ID may be given instead of a seq of multiple.)"
  [handler endpoints f]
  (conditional-transform
    handler
    (routed-pred endpoints)
    f))
