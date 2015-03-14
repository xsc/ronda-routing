(ns ronda.routing.meta-middleware
  (:require [ronda.routing.request :as r]))

(defn meta-middleware
  "If routing metadata is available, builds a (memoized) handler using
   `(wrap-fn handler route-id metadata)` and routes the request to it.

   `metadata-key` can be anything implementing IFn and will be applied to the
   raw routing metadata.

   If `wrap-fn` returns `nil`, no middleware will be applied."
  [handler metadata-key wrap-fn]
  (let [wrap (memoize #(wrap-fn handler %1 %2))]
    (fn [request]
      (let [{:keys [meta id]} (r/routing-data request)
            h (or (some->> meta
                           metadata-key
                           (wrap id))
                  handler)]
        (h request)))))
