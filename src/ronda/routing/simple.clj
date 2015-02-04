(ns ronda.routing.simple
  (:require [ronda.routing.descriptor :refer :all]
            [clojure.set :refer [map-invert]]))

(declare descriptor)

(deftype SimpleDescriptor [uri->endpoint endpoint->uri]
  RouteDescriptor
  (match [_ _ uri]
    (if-let [id (uri->endpoint uri)]
      {:id id, :route-params {}}))
  (generate [_ route-id values]
    (if-let [uri (endpoint->uri route-id)]
      {:path uri
       :route-params {}
       :query-params values}))
  (prefix-string [_ prefix]
    (->> (for [[k v] endpoint->uri]
           [k (str prefix v)])
         (into {})
         (descriptor)))
  (prefix-route-param [_ _ _]
    (throw (UnsupportedOperationException.)))
  (routes [_]
    endpoint->uri))

(defn descriptor
  "Simple RouteDescriptor that only allows matching constant
   URI strings (without support for route params). Input has
   to be a map associating a route ID with the respective
   path:

       (descriptor
         {:articles \"/articles\"
          :top      \"/articles/top\"
          ...})
   "
  [routes]
  {:pre [(every? keyword? (keys routes))
         (every? string? (vals routes))]}
  (SimpleDescriptor.
    (map-invert routes)
    routes))
