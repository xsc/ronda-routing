(ns ronda.routing.utils
  (:import [java.net URLEncoder]))

(defn ->str
  [v]
  (if (keyword? v)
    (name v)
    (str v)))

(defn stringify-vals
  [m]
  (->> (for [[k v] m]
         [k (->str v)])
       (into {})))

(defn urlencode
  [v]
  (-> (->str v)
      (URLEncoder/encode "UTF-8")
      (.replace "+" "%20")))

