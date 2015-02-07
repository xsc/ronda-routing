(ns ronda.routing.request
  (:require [ronda.routing
             [descriptor :as describe]
             [href :as h]]))

;; ## Access

(defn set-routing-data
  "Set the routing data for the given request."
  [request data]
  {:pre [(map? request)
         (map? data)
         (contains? data :id)]}
  (assoc request :ronda/routing data))

(defn set-descriptor
  "Set the descriptor the request should be routed to."
  [request descriptor]
  (assoc request :ronda/descriptor descriptor))

(defn routing-data
  "Get the routing data for the given request"
  [request]
  (:ronda/routing request))

(defn endpoint
  "Get the endpoint data the request should be routed to."
  [request]
  (some-> (routing-data request) :id))

(defn descriptor
  "Get the descriptor that caused the request to be routed."
  [request]
  (:ronda/descriptor request))

;; ## Helper

(defmacro ^:private with-arglists
  [src-var & body]
  `(let [v# (do ~@body)]
     (alter-meta! v# assoc :arglists (:arglists (meta ~src-var)))
     v#))

;; ## Generator

(defn generate-for
  "Generate map of `:path`, `:route-params`, `:query-params` based on the
   RouteDescriptor stored in the given request."
  ([request route-id]
   (generate-for request route-id {}))
  ([request route-id values]
   (some-> (descriptor request)
           (describe/generate route-id values))))

(with-arglists #'generate-for
  (def path
    "Generate the path component for the given route based on the
     RouteDescriptor stored in the given request."
    (comp :path generate-for)))

(defn href
  "Generate path + query string for the given route based on the RouteDescriptor
   stored in the given request."
  ([request route-id]
   (href request route-id {}))
  ([request route-id values]
   (some-> (descriptor request)
           (h/href route-id values))))

;; ## Matcher

(defn match
  "Parse the given path and create a map of `:id`, `:route-params`,
   `:query-params` and `:params` based on the RouteDescriptor stored
   in the given request."
  ([request path]
   (match request :get path))
  ([request request-method path]
   (some-> (descriptor request)
           (h/parse request-method path))))
