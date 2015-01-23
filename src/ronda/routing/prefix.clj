(ns ronda.routing.prefix
  (:require [ronda.routing.descriptor :as describe]
            [potemkin :refer [defprotocol+]]))

;; ## Protocol

(defprotocol+ RoutePrefix
  (prefix* [this descriptor]
    "Prefix the given descriptor."))

;; ## Helpers

(defn- route-param-pattern?
  [[a :as v]]
  (and (= (count v) 2)
       (instance? java.util.regex.Pattern a)))

;; ## Implementation

(extend-protocol RoutePrefix
  String
  (prefix* [s descriptor]
    (describe/prefix-string descriptor s))

  clojure.lang.Keyword
  (prefix* [k descriptor]
    (describe/prefix-route-param
      descriptor
      k
      nil))

  clojure.lang.Sequential
  (prefix* [v descriptor]
    (if (route-param-pattern? v)
      (recur [v] descriptor)
      (reduce
        (fn [descriptor prefix]
          (if (sequential? prefix)
            (let [[p k] prefix]
              (assert (route-param-pattern? prefix)
                      (format "not a route-param + pattern: %s" (pr-str prefix)))
              (describe/prefix-route-param descriptor k p))
            (prefix* prefix descriptor)))
        descriptor
        (reverse v))))

  nil
  (prefix* [_ descriptor]
    descriptor))

(defn prefix
  "Create a new RouteDescriptor representing the routes of the given one
   prefixed by a bidi-style route, e.g.

       (prefix d \"/a\")
       (prefix d [\"/\" :locale])
       (prefix d [\"/\" [#\"de|fr|us\" :locale]])

   "
  [descriptor prefix]
  (prefix* prefix descriptor))
