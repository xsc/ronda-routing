(ns ^:no-doc ronda.routing.routed-middleware
  (:require [ronda.routing
             [descriptor :as describe]
             [middleware :as m]
             [middleware-data :as md]
             [request :as r]]))

(defn- generate-predicate
  "Generate predicate for conditional middleware application.

   - if `enabled?` is true, apply as long as it is not explicitly disabled,
   - if `enabled?` is false, apply if it is explicitly enabled.
   "
  [middleware-key enabled?]
  (if enabled?
    #(not (md/middleware-disabled? % middleware-key))
    #(md/middleware-enabled? % middleware-key)))

(defn- mk-middleware
  [handler middleware-key enabled? wrap-fn args]
  (m/conditional-middleware
    handler
    (generate-predicate middleware-key enabled?)
    #(apply wrap-fn % args)))

(defn routed-middleware
  "Generate a handler that applies the middleware represented by `wrap-fn` only
   if it is explicitly enabled for the currently routed endpoint.
   This has to be used below `ronda.routing.middleware/wrap-routing`."
  [handler middleware-key wrap-fn & args]
  (mk-middleware handler middleware-key false wrap-fn args))

(defn ^{:added "0.2.0"} active-routed-middleware
  "Generate a handler that applies the middleware represented by `wrap-fn`
   unless it is explicitly disabled for the currently routed endpoint.
   This has to be used below `ronda.routing.middleware/wrap-routing`."
  [handler middleware-key wrap-fn & args]
  (mk-middleware handler middleware-key true wrap-fn args))
