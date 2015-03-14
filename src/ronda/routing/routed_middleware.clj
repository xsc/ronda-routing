(ns ronda.routing.routed-middleware
  (:require [ronda.routing
             [descriptor :as describe]
             [middleware :as m]
             [request :as r]]))

;; ## Descriptor Metadata

(defn- conj-disj-middleware
  "Update middleware metadata, conjing the given value to `conj-key` and disjing
   it from `disj-key`."
  [descriptor route-id conj-key disj-key middleware-spec]
  (let [[k v] (if (sequential? middleware-spec)
                middleware-spec
                [middleware-spec nil])]
    (describe/update-metadata
      descriptor
      route-id
      #(-> %
           (cond-> (some? v) (assoc k v))
           (update-in [:middlewares conj-key] (fnil conj #{}) k)
           (update-in [:middlewares disj-key] (fnil disj #{}) k)))))

(defn- conj-disj-all
  "Update middleware metadata, conjing the given values to `conj-key` and
   disjing them from `disj-key` "
  [descriptor conj-key disj-key [route-id ks]]
  (reduce
    #(conj-disj-middleware %1 route-id conj-key disj-key %2)
    descriptor ks))

(defn- pairs-of
  "Create a seq of pairs, starting with `[a b]`."
  [a b vs]
  (cons [a b] (partition 2 vs)))

(defn enable-middlewares
  "Enable the given middlewares for the endpoint identified by the given
   route ID/middleware key pairs."
  [descriptor route-id ks & more]
  (reduce
    #(conj-disj-all % ::enable ::disable %2)
    descriptor
    (pairs-of route-id ks more)))

(defn disable-middlewares
  "Disable the given middlewares for the endpoint identified by the given
   route ID/middleware key pairs."
  [descriptor route-id ks & more]
  (reduce
    #(conj-disj-all % ::disable ::enable %2)
    descriptor
    (pairs-of route-id ks more)))

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
    #(not (contains-middleware? % ::disable middleware-key))
    #(contains-middleware? % ::enable middleware-key)))

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
