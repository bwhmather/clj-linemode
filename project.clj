(defproject linemode "0.1.0-SNAPSHOT"
  :description "Drivers and templates for thermal printers"
  :url "https://github.com/bwhmather/clj-linemode"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:plugins [[lein-bikeshed "0.2.0"]
                             [lein-kibit "0.0.8"]]}})
