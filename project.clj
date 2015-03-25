(defproject ronda/routing "0.2.4"
  :description "Middleware-based Routing Logic."
  :url "https://github.com/xsc/ronda-routing"
  :license {:name "MIT License"
            :url "http://xsc.mit-license.org"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [potemkin "0.3.12"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]
                                  [joda-time "2.7"]]
                   :plugins [[lein-midje "3.1.3"]
                             [codox "0.8.10"]]
                   :codox {:project {:name "ronda/routing"}
                           :src-dir-uri "https://github.com/xsc/ronda-routing/blob/master/"
                           :src-linenum-anchor-prefix "L"
                           :defaults {:doc/format :markdown}}}}
  :aliases {"test" ["midje"]}
  :pedantic? :abort)
