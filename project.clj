(defproject logistan "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [org.xerial/sqlite-jdbc "3.8.7"]
                 [clj-ssh "0.5.11"]
                 [prismatic/schema "0.3.3"]
                 [ragtime/ragtime.core "0.3.7"]
                 [cheshire "5.3.1"]
                 [clj-time "0.8.0"]
                 [prismatic/plumbing "0.3.5"]
                 [org.clojure/tools.cli "0.3.1"]
                 ]
  :plugins [[ragtime/ragtime.lein "0.3.7"]]
  :main logistan.core
  )
