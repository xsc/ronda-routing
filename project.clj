(defproject ronda/routing "0.2.8-SNAPSHOT"
  :description "Middleware-based Routing Logic."
  :url "https://github.com/xsc/ronda-routing"
  :license {:name "MIT License"
            :url "http://xsc.mit-license.org"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [potemkin "0.4.3"]]
  :profiles {:dev {:dependencies [[midje "1.8.3"]
                                  [org.clojure/math.combinatorics "0.1.1"]
                                  [joda-time "2.9.1"]]
                   :plugins [[lein-midje "3.1.3"]
                             [codox "0.8.11"]]
                   :codox {:project {:name "ronda/routing"}
                           :src-dir-uri "https://github.com/xsc/ronda-routing/blob/master/"
                           :src-linenum-anchor-prefix "L"
                           :defaults {:doc/format :markdown}}}}
  :aliases {"test" ["midje"]}
  :pedantic? :abort)
