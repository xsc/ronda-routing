(ns ronda.routing
  (:require [potemkin :refer [import-vars]]
            [ronda.routing
             descriptor
             meta-middleware
             routed-middleware
             middleware
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
  [ronda.routing.meta-middleware
   meta-middleware]
  [ronda.routing.routed-middleware
   active-routed-middleware
   enable-middlewares
   disable-middlewares
   routed-middleware]
  [ronda.routing.prefix
   prefix]
  [ronda.routing.request
   routing-data
   endpoint
   href
   match
   path])
