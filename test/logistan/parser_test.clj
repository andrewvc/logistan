(ns logistan.parser-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [logistan.parsers :refer :all]))

(use-fixtures :once schema.test/validate-schemas)


(deftest pattern-matching
  (testing "group name extraction"
    (let [regexp-str "(?<what>[A-Z]+) ohai (huh)"]
      (is (= (group-names regexp-str) ["what"]))))
  (testing "Correctly parse out common log format with the pattern"
    (let [clog "127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326 \"http://www.example.com/start.html\" \"Mozilla/4.08 [en] (Win98; I ;Nav)\""
          {:keys [regexp groups]} (get compiled-patterns "COMBINEDAPACHELOG")]
      (is (= {"clientip" "127.0.0.1", "ident" "-", "rawrequest" nil, "agent" "\"Mozilla/4.08 [en] (Win98; I ;Nav)\"",
              "referrer" "\"http://www.example.com/start.html\"", "auth" "frank",
              "verb" "GET", "timestamp" "10/Oct/2000:13:55:36 -0700",
              "request" "/apache_pb.gif", "bytes" "2326", "response" "200",
              "httpversion" "1.0"}
             (match-line regexp groups clog))))))

(deftest stream-parsing
  (testing "Parsing a simple stream"
    (let [lines ["localhost:828" "andrewvc.com:8912"]
          parser (get-parser "HOSTPORT")
          parsed (parser lines)]
      (is (= [{:_source (first lines)} {:_source (second lines)}] parsed)))))