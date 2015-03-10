# ronda-routing

__ronda-routing__ is part of the [ronda](https://github.com/xsc/ronda) library and offers
a middleware-based approach to routing, allowing you to do several things:

- [decouple your routing logic](#wrap-routing) from your handlers,
- thus, [choose the routing library](#implementations) most suited to your requirements,
- use [conditional middlewares](#conditional-middleware) or middlewares that get
  [triggered by route metadata](#routed-middleware),
- [generate and parse references](#path-matching--generation) to other parts of your application from
  within a handler and without global state.

This isn't yet another routing/matching library. I promise.

[![Build Status](https://travis-ci.org/xsc/ronda-routing.svg?branch=master)](https://travis-ci.org/xsc/ronda-routing)

## Usage

Don't. But if you insist:

__Leiningen__ ([via Clojars](https://clojars.org/ronda/routing))

[![Clojars Project](http://clojars.org/ronda/routing/latest-version.svg)](http://clojars.org/ronda/routing)

Read the [sales pitch](#official-sales-pitch) to see what problem is being solved.

### Middlewares

<a name='wrap-routing'></a>
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

<a name='conditional-middleware'></a>
__`(conditional-middleware handler p? wrap-fn)`__

This middleware will route requests either to the plain `handler` or to
`(wrap-fn handler)`, depending on whether they match the given predicate `p?` or
not. For example, to only decode JSON bodies for the `:article` endpoint:

```clojure
(-> app
    (routing/conditional-middleware
      #(= (routing/endpoint %) :article)
      decode-json-body)
    (routing/wrap-routing routes))
```

There are more variants of this logic (`conditional-transform` to conditionally
apply a function to the request before passing it to the handler,
`endpoint-middleware` and  `endpoint-transform` to have predicate based on
`ronda.routing/endpoint`), all of which can be found in the [auto-generated
documentation][doc].

<a name='routed-middleware'></a>
__`(routed-middleware handler middleware-key wrap-fn [options])`__

This middleware will route requests either to the plain `handler` or to
`(wrap-fn handler)`, depending on request metadata provided by the
[`RouteDescriptor`](#route-descriptors). In particular, you can enable
middlewares per-route using `enable-middlewares` and `disable-middlewares`:

```clojure
(def routes'
  (-> (bidi/descriptor
        ["/" {"articles" :articles
              "api"      :api}])
      (r/disable-middlewares :api [:tracking])
      (r/enable-middlewares  :api [:json])))
```

Middlewares are then instantiated using e.g.:

```clojure
(def app
  (-> handler
      (r/routed-middleware :tracking wrap-tracking {:enabled? true})
      (r/routed-middleware :json     wrap-json)
      (r/wrap-routing routes')))
```

The optional `options` map can contain the key `:enabled?` which, when set to
`true`, will have a middleware be applied unless it is explicitly disabled.

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

Note that the `RouteDescriptor` decides which values are used as query
parameters. The following rules apply when passing values to `href`:

- keywords will be converted to strings.
- `nil` values will be ignored.
- seqs will be concatenated using commas.

If you want different behaviours you have to preprocess the values map.

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

#### Implementations

Routing Library             | `RouteDescriptor`                       | Route Format
----------------------------|-----------------------------------------|-------------
[bidi][bidi]                | [ronda-routing-bidi][bidi-descriptor]   | `["/" {["article/" :id] :article}]`
[clout][clout]  (compojure) | [ronda-routing-clout][clout-descriptor] | `{:article "/article/:id"}`

You can create your own by implementing the [`ronda.routing.descriptor/RouteDescriptor`][route-descriptor] protocol -
and feel free to open a Pull Request to add it to this list!

## Official Sales Pitch

### What We Have

Commonly, routing logic takes a request, analyzes it and directly calls the handler that is
able to generate a response:

```
                +-----------+
                |           | ----> A
  Request ----> |  Routing  |
                |           | ----> B
                +-----------+
```

Let's assume that `A` accepts a POST request with a JSON body while `B` expects some form
parameters. Both can be handled gracefully using middlewares but, as you can see, they are
tightly coupled with the handlers:

```
                                    +--------+
                +-----------+ ----> |  JSON  | ----> A
                |           |       +--------+
  Request ----> |  Routing  |
                |           |       +--------+
                +-----------+ ----> | Params | ----> B
                                    +--------+
```

Adding another JSON-based handler will usually result in something like the following:

```
                                    +--------+
                +-----------+ ----> |  JSON  | ----> A
                |           |       +--------+
  Request ----> |  Routing  |       +--------+
                |           | ----> | Params | ----> B
                +-----------+       +--------+
                     |              +--------+
                     -------------> |  JSON  | ----> C
                                    +--------+
```

Which makes a route correspond to its own little substack of middlewares and handler, resulting in
significant duplication across diverse applications. Alternatively, one could model the stack like
this:

```
                                    +--------+-----------+ ----> A
                +-----------+ ----> |  JSON  | Routing 2 |
                |           |       +--------+-----------+ ----> C
  Request ----> |  Routing  |
                |           |       +--------+
                +-----------+ ----> | Params | ----> B
                                    +--------+
```

This can work well if the subsystems can be easily identified (e.g. all JSON handlers reside under
`/api`) but will fall apart very quickly if the system is more heterogenous. Also, having routing
logic in two ore more different places can make it harder to reason about it in the first place.

### What We Could Have

Instead, `ronda-routing` proposes a more decoupled approach, making routing logic something that
gets injected into the application:

```
                                                   (optional
                +-----------+  Request +          middlewares)
                |  Routing  |  Routing Data   +--------+--------+
  Request ----> |  Middle-  | --------------> |  JSON  | Params |
                |   ware    |                 +--------+--------+
                +-----------+                          |
                     ^                                 v
                     |                          +-------------+
                     v                          |  intercept  |
               +------------+                   +-------------+
               | Descriptor |                     |    |    |
               +------------+                     A    B    C

```

The [`Descriptor`](#route-descriptors) contains the routing logic, basically producing
an identifier that designates the final handler and gets injected into the request.
Follow-up middlewares can then look at that identifier and decide whether they have to
do anything or not.

Multiple paradigms are then possible:

1. Each middleware knows what handlers require it. This means maintaining a list of
   route identifiers per middleware that trigger activation if they are encountered.
2. The route descriptor contains feature-specific data (akin to "feature flags") for
   each route that is read by middlewares, triggering them when necessary.

The second one has immense value when it comes to documentation and is the one preferred
by ronda - but the possibility to use the first approach (or even fall back to a per-handler
middleware stack) remains.

## Contributing

Contributions are very welcome.

1. Clone the repository.
2. Create a branch, make your changes.
3. Make sure tests are passing by running `lein test`.
4. Submit a Github pull request.

## License

Copyright &copy; 2015 Yannick Scherer

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[doc]: https://xsc.github.io/ronda-routing/
[bidi]: https://github.com/juxt/bidi
[clout]: https://github.com/weavejester/clout
[bidi-descriptor]: https://github.com/xsc/ronda-routing-bidi
[clout-descriptor]: https://github.com/xsc/ronda-routing-clout
[route-descriptor]: https://xsc.github.io/ronda-routing/ronda.routing.descriptor.html#var-RouteDescriptor
