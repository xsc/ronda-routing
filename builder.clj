(ns ronda.routing.builder
  (:require [ronda.routing
             [middleware :as m]
             [middleware-data :as md]]))

(defn- process-middlewares
  [descriptor route-id data]
  (reduce
    (fn [descriptor [middleware-key value]]
      (if (identical? value false)
         (md/disable-middlewares descriptor route-id [middleware-key])
         (md/enable-middlewares descriptor route-id {middleware-key value})))
    descriptor
    (dissoc data :handler)))

(defn- process-handler
  [handlers route-id {:keys [handler]}]
  {:pre [(ifn? handler)]}
  (assoc handlers route-id handler))

(defn collect
  "Given a map associating a route ID with an endpoint function, generate
   the compiled `:handler`, as well as a `:descriptor` with all desired
   middlewares enabled or disabled. The endpoint functions will have `args`
   passed to it and should produce a map, with `:handler` representing the Ring
   handler function.

   Every other key in the map will be interpreted as a middleware key, with
   value being `false` disabling the respective middleware, every other one
   enabling it. Example endpoint map:

       {:handler  (constantly {:status 200})
        :schema   {:get ...}
        :auth     true
        :tracking false}

   "
  [descriptor endpoints & args]
  (loop [descriptor descriptor
         handlers   {}
         endpoints  endpoints]
    (if-let [[[route-id endpoint-fn] & rst] (seq endpoints)]
      (let [data (apply endpoint-fn args)]
        (recur
          (process-middlewares descriptor route-id data)
          (process-handler handlers route-id data)
          rst))
      {:descriptor descriptor
       :handler    (m/compile-endpoints handlers)})))

(defn build
  "Collect all handlers/middleware data, then  "
  ([collected] (build identity collected))
  ([wrap-fn {:keys [descriptor handler]}]
   (m/wrap-routing
     (wrap-fn handler)
     descriptor))
  ([wrap-fn descriptor endpoints & args]
   (->> (apply collect descriptor endpoints args)
        (build wrap-fn))))

(require '[ronda.routing.simple :as simple])

(let [data (assemble
             (simple/descriptor
               {:articles "/articles"
                :home "/"})
             {:home (constantly
                      {:handler (constantly {:status 200})})})]
  ((build data) {:request-method :get, :uri "/"})
  )
