(ns logistan.config-test
  (:require [logistan.config :refer :all]
            [clojure.test :refer :all]))

(def sample-config {:hosts [
                            {:user "webmaster"
                             :hostname "example.net"
                             :logs [{:path "/var/log/syslog" :parser "SYSLOG"}]}
                            ]})

(deftest config-load
  (testing "merge" ;; Actually alters the var root
    (let [expected (merge config sample-config)]
      (load-config sample-config)
      (is (= expected config)))))


