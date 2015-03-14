(ns ronda.routing
  (:require [potemkin :refer [import-vars]]
            [ronda.routing
             descriptor
             meta-middleware
             routed-middleware
             middleware
             middleware-data
             prefix
             request]))

;; ## User-Facing API
;;
;; These are the most commonly used functions exposed by this project.

(import-vars
  [ronda.routing.descriptor
   assoc-metadata
   merge-metadata
   update-metadata
   routes]
  [ronda.routing.middleware
   wrap-routing
   wrap-endpoints
   wrap-endpoint
   compile-endpoints
   conditional-middleware
   conditional-transform
   endpoint-middleware
   endpoint-transform]
  [ronda.routing.middleware-data
   enable-middlewares
   disable-middlewares]
  [ronda.routing.meta-middleware
   meta-middleware]
  [ronda.routing.routed-middleware
   active-routed-middleware
   routed-middleware]
  [ronda.routing.prefix
   prefix]
  [ronda.routing.request
   routing-data
   endpoint
   href
   match
   path])
