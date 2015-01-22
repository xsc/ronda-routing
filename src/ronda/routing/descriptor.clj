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
  (routes [_]
    "Return a map associating route IDs with their full path value,
     adhering to bidi's BNF. If `nil` is returned, no information
     can be gathered from this descriptor."))

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

;; ## Descriptor Wrappers

(defn prefix-descriptor
  "Create RouteDescriptor that matches/generates routes with the given
   URI prefix."
  [descriptor ^String prefix]
  {:pre [(string? prefix)]}
  (let [prefix-count (count prefix)]
    (reify RouteDescriptor
      (match [_ request-method uri]
        (if (.startsWith ^String uri prefix)
          (match descriptor request-method (subs uri prefix-count))))
      (generate [_ route-id values]
        (if-let [r (generate descriptor route-id values)]
          (update-in r [:path] #(str prefix %))))
      (routes [_]
        (->> (for [[id spec] (routes descriptor)]
               (->> (if (sequential? spec)
                      (vec
                        (if (string? (first spec))
                          (cons (str prefix (first spec)) (rest spec))
                          (cons prefix spec)))
                      [prefix spec])
                    (vector id)))
             (into {}))))))
