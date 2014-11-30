(ns logistan.core
  (:import (java.io PipedInputStream))
  (:require
    [clj-ssh.ssh :refer [ssh-agent session ssh with-connection]]
    [clojure.string :as string]
    [cheshire.core :as json]
    [clojure.tools.cli :as cli]
    [clojure.walk :refer [keywordize-keys]]
    [logistan.config :as config]
    [logistan.parsers :as parsers]
    [logistan.ssh :as ssh])
  (:gen-class))


(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 80
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-c" "--config CONFIG" "Config File"
    :parse-fn (fn [filename] (keywordize-keys (json/parse-string (slurp filename))))
    ]
   ["-h" "--help"]])

(defn usage [options-summary] (println "The simple log aggregator" options-summary))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main
  "I don't do a whole lot."
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    (config/load-config (:config options))
    (println options)

    ))

;;(ssh-tail "webmaster" "lux.andrewvc.com" "/home/webmaster/fakelog"