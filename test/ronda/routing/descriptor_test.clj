(ns ronda.routing.descriptor-test
  (:require [midje.sweet :refer :all]
            [ronda.routing.descriptor :refer :all]))

;; ## Fixture

(def test-descriptor
  (reify RouteDescriptor
    (match [_ request-method uri]
      (if (= uri "/hello/world")
        {:id :greet
         :route-params {:greeting "hello"
                        :recipient "world"}}))
    (generate [_ route-id {:keys [greeting recipient] :as params}]
      (when (= route-id :greet)
        (assert (and greeting recipient))
        {:path (format "/%s/%s" greeting recipient)
         :route-params (select-keys params [:greeting :recipient])
         :query-params (dissoc params :greeting :recipient)}))
    (routes [_]
      {:greet ["/" :greeting "/" :recipient]})))

;; ## Tests

(tabular
  (fact "about request matching."
        (let [r (->> {:request-method ?method
                      :uri ?uri}
                     (match-request test-descriptor))]
          (:id r) => ?id
          (-> r :route-params :greeting) => ?greeting
          (-> r :route-params :recipient) => ?recipient))
  ?method ?uri              ?id      ?greeting      ?recipient
  :get    "/hello/world"    :greet   "hello"        "world"
  :post   "/hello/world"    :greet   "hello"        "world"
  :post   "/hello/you"      falsey   falsey         falsey)

(let [d (prefix-descriptor test-descriptor "/app")]
  (tabular
    (fact "about a prefixed descriptor matching."
          (let [r (->> {:request-method ?method
                        :uri ?uri}
                       (match-request d))]
            (:id r) => ?id
            (-> r :route-params :greeting) => ?greeting
            (-> r :route-params :recipient) => ?recipient))
    ?method ?uri               ?id      ?greeting      ?recipient
    :get    "/app/hello/world" :greet   "hello"        "world"
    :post   "/app/hello/world" :greet   "hello"        "world"
    :post   "/hello/world"     falsey   falsey         falsey)
  (fact "about prefixed descriptor generation."
        (let [{:keys [path route-params]} (generate
                                            d
                                            :greet
                                            {:greeting "cheerio"
                                             :recipient "miss-sophie"})]
          path => "/app/cheerio/miss-sophie"
          route-params => {:greeting "cheerio", :recipient "miss-sophie"}))
  (fact "about prefixed descriptor route lists."
        (routes d) => {:greet ["/app/" :greeting "/" :recipient]}))
