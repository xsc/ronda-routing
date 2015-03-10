(ns ronda.routing.href
  (:require [ronda.routing
             [descriptor :as describe]
             [utils :as u]]
            [clojure.string :as string]))

;; ## Generate

(defn- filter-nils
  [values]
  (->> (filter (comp some? val) values)
       (into {})))

(defn- generate-query-string
  [query-params]
  (->> (for [[k v] (sort-by key query-params)]
         (format "%s=%s"
                 (u/urlencode k)
                 (u/urlencode v)))
       (string/join "&")
       (str)))

(defn- join-query-string
  [base-path query-string]
  (cond (zero? (count query-string)) base-path
        (.endsWith ^String base-path "?") (str base-path query-string)
        (.contains ^String base-path "?") (str base-path "&" query-string)
        :else (str base-path "?" query-string)))

(defn href
  "Generate path + query-string for the given route based on the given
   RouteDescriptor."
  [descriptor route-id values]
  (if-let [{:keys [path query-params]} (->> values
                                            (filter-nils)
                                            (u/stringify-vals)
                                            (describe/generate
                                              descriptor
                                              route-id))]
    (->> (generate-query-string query-params)
         (join-query-string path))))

;; ## Match

(defn- parse-query-params
  "Parse the given query string and produce map of keywords
   -> strings."
  [query-string]
  (if query-string
    (->> (for [s (.split ^String query-string "&")
               :let [[k v] (->> (.split ^String s "=" 2)
                                (map u/urldecode))]]
           [(keyword k) v])
         (into {}))
    {}))

(defn parse
  "Parse the given href and produce a map of `:id`, `:path`,
   `:route-params`, `:query-params` and `:params`."
  [descriptor request-method href]
  (let [[path query-string] (.split ^String href "\\?" 2)]
    (if-let [{:keys [route-params] :as r} (describe/match
                                            descriptor
                                            request-method
                                            path)]
      (let [query-params (parse-query-params query-string)]
        (->> {:path path
              :query-params query-params
              :params (merge route-params query-params)}
             (merge r))))))
