(ns ronda.routing.simple
  (:require [ronda.routing.descriptor :refer :all]
            [clojure.set :refer [map-invert]]))

(defn- prepare-routes
  [routes]
  (->> (for [[route-id path] routes]
         [route-id {:path path, :meta {}}])
       (into {})))

(deftype SimpleDescriptor [route-data uri->endpoint]
  RouteDescriptor
  (match [_ _ uri]
    (if-let [id (uri->endpoint uri)]
      {:id id,
       :route-params {}
       :meta (get-in route-data [id :meta] {})}))
  (generate [_ route-id values]
    (if-let [data (get route-data route-id)]
      (assoc data
             :route-params {}
             :query-params values)))
  (update-metadata [_ route-id f]
    (if (contains? route-data route-id)
      (SimpleDescriptor.
        (update-in route-data [route-id :meta] f)
        uri->endpoint)
      (throw
        (IllegalArgumentException.
          (format "no such route: %s" route-id)))))
  (routes [_]
    route-data)

  PrefixableRouteDescriptor
  (prefix-string [_ prefix]
    (let [{:keys [routes uris]}
          (reduce
            (fn [m [route-id {:keys [path] :as data}]]
              (let [path' (str prefix path)]
                (-> m
                    (assoc-in [:uris path'] route-id)
                    (assoc-in [:routes route-id] (assoc data :path path')))))
            {} route-data)]
      (SimpleDescriptor. routes uris)))
  (prefix-route-param [_ _ _]
    (throw
      (UnsupportedOperationException.
        "simple descriptor does not support route params."))))

(defn descriptor
  "Simple RouteDescriptor that only allows matching constant
   URI strings (without support for route params). Input has
   to be a map associating a route ID with the respective
   path:

       (descriptor
         {:articles \"/articles\"
          :top      \"/articles/top\"
          ...})
   "
  [routes]
  {:pre [(every? keyword? (keys routes))
         (every? string? (vals routes))]}
  (SimpleDescriptor.
    (prepare-routes routes)
    (map-invert routes)))
