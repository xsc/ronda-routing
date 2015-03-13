(defproject ronda/routing "0.2.1-SNAPSHOT"
  :description "Middleware-based Routing Logic."
  :url "https://github.com/xsc/ronda-routing"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [potemkin "0.3.11"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]
                                  [joda-time "2.7"]]
                   :plugins [[lein-midje "3.1.3"]
                             [codox "0.8.10"]]
                   :codox {:project {:name "ronda/routing"}
                           :defaults {:doc/format :markdown}}}}
  :aliases {"test" ["midje"]}
  :pedantic? :abort)
