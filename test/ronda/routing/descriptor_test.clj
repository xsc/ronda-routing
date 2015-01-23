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
    (prefix-string [this prefix]
      (throw (Exception.)))
    (prefix-route-param [this k pattern]
      (throw (Exception.)))
    (routes [_]
      {:greet {:path ["/" :greeting "/" :recipient]
               :methods #{:get}}})))

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
