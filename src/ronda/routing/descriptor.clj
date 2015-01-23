(ns ronda.routing.descriptor
  (:require [potemkin :refer [defprotocol+]]))

;; ## Protocol

(defprotocol+ RouteDescriptor
  "Protocol for Route Descriptors."
  (match [_ request-method uri]
    "Match the given URI, return a map of `:id` (the route ID) and
     `:route-params`.")
  (generate [_ route-id values]
    "Generate path for the given route ID and params (or `nil` if
     the route ID is unknown), returns a map of `:path`, `:route-params`
     (the values used as route params) and `:query-params`.
     Might throw an exception if the values do not match an expected schema.")
  (prefix-string [_ prefix]
    "Return a new RouteDescriptor representing all routes prefixed with
     the given constant string.")
  (prefix-route-param [_ route-param pattern]
    "Return a new RouteDescriptor representing all routes prefixed with
     a string matching the given pattern (if non-nil). When matching a URI
     against the new descriptor, the value of said string will be bound to
     the given key in `:route-params`.")
  (routes [_]
    "Return a map associating route IDs with a map of `:path` (the full
     path to the endpoint using bidi's route format) and `:methods`
     (the set of request methods applicable for the endpoint)."))

(extend-protocol RouteDescriptor
  Object
  (match [o request-method uri]
    (if (ifn? o)
      (o request-method uri)))
  (generate [o route-id values]
    (if (fn? o)
      (o route-id values)
      (throw (UnsupportedOperationException.))))
  (routes [_]
    nil)

  nil
  (match [_ _ _] nil)
  (generate [_ _ _ _] nil)
  (routes [_] nil))

;; ## Match

(defn match-request
  "Match request against descriptor"
  [descriptor {:keys [request-method uri]}]
  (match descriptor request-method uri))

