(ns ronda.routing.routed-middleware
  (:require [ronda.routing
             [descriptor :as describe]
             [middleware :as m]
             [request :as r]]))

;; ## Descriptor Metadata

(defn- conj-disj-middleware
  "Update middleware metadata, conjing the given value to `conj-key` and disjing
   it from `disj-key`."
  [descriptor route-id conj-key disj-key k]
  (describe/update-metadata
    descriptor
    route-id
    #(-> %
         (update-in [:middlewares conj-key] (fnil conj #{}) k)
         (update-in [:middlewares disj-key] (fnil disj #{}) k))))

(defn enable-middlewares
  "Enable the given middlewares for the endpoint identified by the given
   route ID."
  [descriptor route-id ks]
  (reduce
    #(conj-disj-middleware %1 route-id :enable :disable %2)
    descriptor ks))

(defn disable-middlewares
  "Disable the given middlewares for the endpoint identified by the given
   route ID."
  [descriptor route-id ks]
  (reduce
    #(conj-disj-middleware %1 route-id :disable :enable %2)
    descriptor ks))

;; ## Metadata-Driven Middlewares

(defn- contains-middleware?
  "Check whether the middleware metadata at the given `k` contains the given
   middleware key value."
  [request k middleware-key]
  (some-> request
          r/routing-data
          (get-in [:meta :middlewares k])
          (contains? middleware-key)))

(defn- generate-predicate
  "Generate predicate for conditional middleware application.

   - if `enabled?` is true, apply as long as it is not explicitly disabled,
   - if `enabled?` is false, apply if it is explicitly enabled.
   "
  [middleware-key enabled?]
  (if enabled?
    #(not (contains-middleware? % :disable middleware-key))
    #(contains-middleware? % :enable middleware-key)))

(defn routed-middleware
  "Generate a handler that applies the middleware represented by `wrap-fn` only
   if it is explicitly enabled/not-disabled for the currently routed endpoint.
   This has to be used below `ronda.routing.middleware/wrap-routing`."
  [handler middleware-key wrap-fn & [{:keys [enabled?] :or {enabled? false}}]]
  (m/conditional-middleware
    handler
    (generate-predicate middleware-key enabled?)
    wrap-fn))