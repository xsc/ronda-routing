(ns ronda.routing.href
  (:require [ronda.routing
             [descriptor :as describe]
             [utils :as u]]
            [clojure.string :as string]))

(defn- generate-query-string
  [query-params]
  (->> (for [[k v] (sort-by key query-params)]
         (format "%s=%s"
                 (u/urlencode k)
                 (u/urlencode v)))
       (string/join "&")
       (str)))

(defn- join-query-string
  [base-path query-string]
  (cond (zero? (count query-string)) base-path
        (.endsWith ^String base-path "?") (str base-path query-string)
        (.contains ^String base-path "?") (str base-path "&" query-string)
        :else (str base-path "?" query-string)))

(defn href
  "Generate path + query-string for the given route based on the given
   RouteDescriptor."
  [descriptor route-id values]
  (if-let [{:keys [path query-params]} (->> (u/stringify-vals values)
                                            (describe/generate
                                              descriptor
                                              route-id))]
    (->> (generate-query-string query-params)
         (join-query-string path))))
