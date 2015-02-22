(ns ronda.routing
  (:require [potemkin :refer [import-vars]]
            [ronda.routing meta-middleware middleware prefix request]))

;; ## User-Facing API
;;
;; These are the most commonly used functions exposed by this project.

(import-vars
  [ronda.routing.middleware
   wrap-routing
   wrap-endpoints
   wrap-endpoint
   compile-endpoints
   conditional-middleware
   conditional-transform
   endpoint-middleware
   endpoint-transform]
  [ronda.routing.meta-middleware
   enable-middlewares
   disable-middlewares
   meta-middleware]
  [ronda.routing.prefix
   prefix]
  [ronda.routing.request
   routing-data
   endpoint
   href
   match
   path])
