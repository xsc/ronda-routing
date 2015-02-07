(ns ronda.routing.descriptor
  (:require [potemkin :refer [defprotocol+]]))

;; ## Basic Route Descriptor

(defprotocol+ RouteDescriptor
  "Protocol for Route Descriptors."
  (match [_ request-method uri]
    "Match the given URI, return a map of `:id` (the route ID) and
     `:route-params`, as well as any additional `:meta` data that
     was attached using `update-metadata`.")
  (generate [_ route-id values]
    "Generate path for the given route ID and params (or `nil` if
     the route ID is unknown), returns a map of `:path`, `:route-params`
     (the values used as route params) and `:query-params`, as well as any
     additional `:meta` data that was attached using `update-metadata`.
     Might throw an exception if the values do not match an expected schema.")
  (update-metadata [_ route-id f]
    "Return a new RouteDescriptor that applies the given function to the given
     route's metadata.")
  (routes [_]
    "Return a map associating route IDs with a map of `:path` (the full
     path to the endpoint using bidi's route format) and `:methods`
     (the set of request methods applicable for the endpoint), as well as
     all the additional `:meta` data set using `update-metadata`."))

;; ## Route Descriptor + Prefix

(defprotocol+ PrefixableRouteDescriptor
  "Protocol for RouteDescriptors that can be prefixed with either constant
   strings or an additional route parameter."
  (prefix-string [_ prefix]
    "Return a new RouteDescriptor representing all routes prefixed with
     the given constant string.")
  (prefix-route-param [_ route-param pattern]
    "Return a new RouteDescriptor representing all routes prefixed with
     a string matching the given pattern (if non-nil). When matching a URI
     against the new descriptor, the value of said string will be bound to
     the given key in `:route-params`."))

(extend-protocol PrefixableRouteDescriptor
  Object
  (prefix-string [_ prefix]
    (throw
      (UnsupportedOperationException.
        "descriptor cannot be prefixed.")))
  (prefix-route-param [_ prefix]
    (throw
      (UnsupportedOperationException.
        "descriptor cannot be prefixed."))))

;; ## Derived Functions

(defn match-request
  "Match request against descriptor"
  [descriptor {:keys [request-method uri]}]
  (match descriptor request-method uri))
