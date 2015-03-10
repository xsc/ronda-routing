(ns ronda.routing.utils
  (:require [clojure.string :as string])
  (:import [java.net URLEncoder URLDecoder]))

(defn ->str
  [v]
  (cond (keyword? v) (name v)
        (sequential? v) (string/join "," (map ->str v))
        :else (str v)))

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

(defn urldecode
  [v]
  (-> (->str v)
      (URLDecoder/decode "UTF-8")))

(defmacro throwf
  [fmt & args]
  `(throw
     (Exception. (format ~fmt ~@args))))
