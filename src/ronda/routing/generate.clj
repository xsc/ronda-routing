(ns ronda.routing.generate
  (:require [ronda.routing.utils :as u]))

(defn- read-keyword
  "Assert existence of value in the given map, as well as compliance with the given
   pattern (if appicable). Returns the value for the requested key."
  [vs k pattern]
  {:pre [(keyword? k)]}
  (when (not (contains? vs k))
    (u/throwf "value missing for route param: %s" k))
  (let [v (get vs k)]
    (when (and pattern (not (re-matches pattern v)))
      (u/throwf "route param %s not compatible with pattern: %s" k pattern))
    v))

(defn generate-by
  "Based on a full, flat, bidi-style path vector and a map of values,
   generate a path. This function is intended to be used for RouteDescriptors
   whose underlying libraries do not explicitly offer path generation."
  [path values]
  (let [vs (u/stringify-vals values)]
    (->> (if (sequential? path)
           path
           [path])
         (map
           (fn [v]
             (cond (string? v) v
                   (keyword? v) (read-keyword vs v nil)
                   (vector? v) (read-keyword vs (second v) (first v))
                   :else (u/throwf "not a valid path segment: %s" (pr-str v)))))
         (apply str))))
