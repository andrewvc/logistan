(ns logistan.core
  (:import (java.io PipedInputStream))
  (:require
    [clj-ssh.ssh :refer [ssh-agent session ssh with-connection]]
    [logistan.parsers :as parsers]))

(def newline-int (int \newline))

(defn read-pipe-line [^PipedInputStream is]
  (let [strb (StringBuilder.)]
    (loop [res (int (.read is))]
      (cond
        (= res -1) [(.toString strb) false]
        (= res newline-int) [(.toString strb) true]
        :else (do (.append strb (char res))
                  (recur (int (.read is))))))))

; According to the PipedInputStream docs, you should read off it with a separate
; thread or it may deadlock. We should probably figure out a different API Here.
(defn input-stream->line-seq
  ([^PipedInputStream input-stream]
    (input-stream->line-seq input-stream
                            (read-pipe-line input-stream)))
  ([^PipedInputStream input-stream [last-line more-data]]
    (if more-data
      (cons last-line
            (lazy-seq (input-stream->line-seq input-stream (read-pipe-line input-stream))))
      nil)) )

(defn ssh-tail [username host logfile]
  (let [agent (ssh-agent {})
        sess (session agent host {:username username :strict-host-key-checking :no})]
    (with-connection
      sess
      (let [result (ssh sess {:cmd (str "tail -f " logfile) :out :stream})]
        (input-stream->line-seq (:out-stream result))))))

(defn -main
  "I don't do a whole lot."
  [& argv]
  (ssh-tail "webmaster" "lux.andrewvc.com" "/home/webmaster/fakelog"))