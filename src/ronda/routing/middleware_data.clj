(ns ronda.routing.middleware-data
  (:require [ronda.routing
             [descriptor :as describe]
             [request :as r]]))

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
           (cond-> (some? v) (assoc-in [:middlewares k] v))
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

(defn- route-contains-middleware?
  "Check the raw route data on whether a middleware is contained in the set
   at the given `k`."
  [routing-data k middleware-key]
  (-> routing-data
      (get-in [:meta :middlewares k])
      (contains? middleware-key)))

(defn ^{:added "0.2.7"} route-middleware-enabled?
  "Check the raw route data on whether a middleware is enabled."
  [routing-data middleware-key]
  (route-contains-middleware? routing-data ::enable middleware-key))

(defn ^{:added "0.2.7"} route-middleware-disabled?
  "Check the raw route data on whether a middleware is disabled."
  [routing-data middleware-key]
  (route-contains-middleware? routing-data ::disable middleware-key))

(defn- contains-middleware?
  "Check whether the middleware metadata at the given `k` contains the given
   middleware key value."
  [request k middleware-key]
  (some-> request
          r/routing-data
          (route-contains-middleware? k middleware-key)))

(defn middleware-enabled?
  "Check whether a middleware is active."
  [request middleware-key]
  (contains-middleware? request ::enable middleware-key))

(defn middleware-disabled?
  "Check whether a middleware is inactive."
  [request middleware-key]
  (contains-middleware? request ::disable middleware-key))

(defn middleware-data
  "Get middleware data attached by `enable-middlewares`."
  [request middleware-key]
  (some-> request
          r/routing-data
          (get-in [:meta :middlewares middleware-key])))

(defn ^{:added "0.2.7"} route-middleware-data
  "Get middleware data from the raw route data."
  [routing-data middleware-key]
  (get-in routing-data [:meta :middlewares middleware-key]))
