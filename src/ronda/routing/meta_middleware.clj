(ns ronda.routing.meta-middleware
  (:require [ronda.routing
             [request :as r]
             [middleware-data :as md]]))

(defn ^{:added "0.2.1"} meta-middleware
  "If routing metadata is available, builds a (memoized) handler using
   `(wrap-fn handler route-id metadata)` and routes the request to it.

   The middleware data is expected at `[:meta :middlewares k]` of the routing
   metadata attached by `wrap-routing` and the middleware itself is supposed to
   be activated using `enable-middlewares`. Note that even `nil` metadata is
   allowed an will be passed to `wrap-fn`.

   If `wrap-fn` returns `nil`, no middleware will be applied."
  [handler middleware-key wrap-fn]
  (let [wrap (memoize #(wrap-fn handler %1 %2))]
    (fn [request]
      (if (md/middleware-enabled? request middleware-key)
        (let [id (r/endpoint request)
              mt (md/middleware-data request middleware-key)]
          ((wrap id mt) request))
        (handler request)))))
