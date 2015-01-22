(ns ronda.routing.request
  (:require [ronda.routing
             [descriptor :as describe]
             [href :as h]]))

;; ## Access

(defn set-endpoint
  "Set the endpoint the request should be routed to."
  [request id]
  (assoc request :ronda/endpoint id))

(defn set-descriptor
  "Set the descriptor the request should be routed to."
  [request descriptor]
  (assoc request :ronda/descriptor descriptor))

(defn endpoint
  "Get the endpoint the request should be routed to."
  [request]
  (:ronda/endpoint request))

(defn descriptor
  "Get the descriptor that caused the request to be routed."
  [request]
  (:ronda/descriptor request))

(defn update-descriptor
  "Update the descriptor stored in the request."
  [request f]
  (update-in request [:ronda/descriptor] f))

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

(defn to
  "Generate path + query string for the given route based on the RouteDescriptor
   stored in the given request."
  ([request route-id]
   (to request route-id {}))
  ([request route-id values]
   (some-> (descriptor request)
           (h/href route-id values))))
