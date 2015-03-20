(ns ronda.routing.generate
  (:require [ronda.routing.utils :as u]
            [potemkin :refer [defprotocol+]]))

;; ## Helper

(defn- read-keyword
  "Assert existence of value in the given map, as well as compliance with the given
   pattern (if appicable). Returns the value for the requested key."
  [vs k pattern]
  {:pre [(keyword? k)]}
  (let [v (get vs k ::none)]
    (when (= v ::none)
      (u/throwf "value missing for route param: %s" k))
    (when (and pattern (not (re-matches pattern v)))
      (u/throwf "route param %s not compatible with pattern: %s" k pattern))
    v))

;; ## Generator

(defprotocol+ Generateable
  (^:private generate-by* [this values])
  (generate-by [this values]
    "Based on a full, flat, bidi-style path vector and a map of values,
     generate a path. This function is intended to be used for RouteDescriptors
     whose underlying libraries do not explicitly offer path generation."))

(extend-protocol Generateable
  String
  (generate-by* [s _]
    s)
  (generate-by [s _]
    s)

  clojure.lang.Keyword
  (generate-by* [k values]
    (read-keyword values k nil))
  (generate-by [k values]
    (generate-by* k values))

  clojure.lang.IPersistentVector
  (generate-by* [v values]
    (read-keyword values (second v) (first v)))
  (generate-by [path values]
    (let [vs (u/stringify-vals values)]
      (if (sequential? path)
        (reduce
          (fn [s e]
            (str s (generate-by* e vs)))
          "" path)
        (generate-by* path vs))))

  clojure.lang.Sequential
  (generate-by [this values]
    (generate-by (vec this) values)))
