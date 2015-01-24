# ronda-routing

__ronda-routing__ is part of the [ronda](https://github.com/xsc/ronda) library and offers
a middleware-based approach to routing, allowing you to decouple your routing logic from your
handlers.

[![Build Status](https://travis-ci.org/xsc/ronda-routing.svg?branch=master)](https://travis-ci.org/xsc/ronda-routing)
[![endorse](https://api.coderwall.com/xsc/endorsecount.png)](https://coderwall.com/xsc)

## Usage

Don't. But if you insist:

__Leiningen__ ([via Clojars](https://clojars.org/ronda/routing))

[![Clojars Project](http://clojars.org/ronda/routing/latest-version.svg)](http://clojars.org/ronda/routing)

### Route Descriptors

A `RouteDescriptor` is a routing-library independent representation of a series of routes. This project, however,
does not contain any concrete implementations, so you have to explicitly include one, e.g.
[ronda/routing-bidi][bidi-descriptor]:

```clojure
(require '[ronda.routing.bidi :as bidi])

(def routes
  (bidi/descriptor
    ["/" {"articles"        :articles
          ["articles/" :id] :article}]))
```

Implementations exist for:

- __bidi__: [ronda-routing-bidi][bidi-descriptor]

### Middlewares

__`(wrap-routing handler descriptor)`__

The `ronda.routing/wrap-routing` middleware will use a [`RouteDescriptor`](#route-descriptors) to decide on an
endpoint a request should be routed to. The endpoint ID will be injected into the request (accessible via
`ronda.routing/endpoint`) before passing it on to the next middleware/handler.

```clojure
(require '[ronda.routing :as routing])

(def app
  (-> (fn [{:keys [route-params] :as request}]
        {:status 200,
         :body (case (routing/endpoint request)
                 :articles "there are 2 articles."
                 :article  (str "this is article " (:id route-params) "."))})
      (routing/wrap-routing routes)))
```

Calling the resulting handler will inject the endpoint ID into the request which can then be
resolved further down the pipeline (using a simple `case` statement in the above example).

```clojure
(app {:request-method :get, :uri "/articles"})
;; => {:status 200, :body "there are 2 articles!"}

(app {:request-method :get, :uri "/articles/1"})
;; => {:status 200, :body "this is article 1."}
```

__`(wrap-endpoints default-handler handlers)`__

The `ronda.routing/wrap-endpoints` middleware has to be applied downstream of `wrap-routing`
since it relies on the endpoint ID injected into the request. It will match the endpoint ID
against a map of handlers, before passing it to either a matching one or further down the
pipeline.

```clojure
(def app
  (-> (constantly {:status 404, :body "not found."})
      (routing/wrap-endpoints
        {:article
         #(->> % :route-params :id
               (format "this is article %s.")
               (hash-map :status 200 :body))})
      (routing/wrap-routing routes)))
```

Basically, what can be intercepted will be and the rest will pass through unmodified:

```clojure
(app {:request-method :get, :uri "/articles/1"})
;; => {:status 200, :body "this is article 1."}

(app {:request-method :get, :uri "/articles"})
;; => {:status 404, :body "not found."}
```

There is also `wrap-endpoint` (which will add a single handler interception) and `compile-endpoints`
(which will return `nil` if the default path is reached). See the [auto-generated documentation][doc]
for more information.

### Path Matching &amp; Generation

The `wrap-routing` middleware ([see above](#middlewares)) enables the use of two additional features:

- path generation from within a handler using `ronda.routing/href`,
- path matching from within a handler using `ronda.routing/match`.

Both functions use a RouteDescriptor injected into the request map which means that you can reference
(and accept references to) other parts of your application in a way that avoids global state.

```clojure
(defn- article
  [{:keys [route-params uri] :as request}]
  (let [id (-> route-params :id Long/parseLong)]
    {:status 200,
     :data (routing/match request uri)
     :body (->> {:id (inc id)}
                (routing/href request :article)
                (str "next article: "))}))

(def app
  (-> (constantly {:status 404, :body "not found."})
      (routing/wrap-endpoints
        {:article article})
      (routing/wrap-routing routes)))
```

And, go!

```clojure
(app {:request-method :get, :uri "/articles/1"})
;; => {:status 200,
;;     :data {:params {:id "1"},
;;            :query-params {},
;;            :path "/articles/1",
;;            :id :article,
;;            :route-params {:id "1"}},
;;     :body "next article: /articles/2"}
```

## License

Copyright &copy; 2015 Yannick Scherer

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[doc]: https://xsc.github.io/ronda-routing/
[bidi]: https://github.com/juxt/bidi
[bidi-descriptor]: https://github.com/xsc/ronda-routing-bidi
