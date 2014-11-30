(ns logistan.config
  (:require
    [plumbing.core :refer [safe-get]]
    [schema.core :as s]
    [cheshire.core :as json]))

(def LogConfig {(s/required-key :path) s/Str
                 (s/required-key :parser) s/Str})

(def HostConfig
  {(s/required-key :hostname) s/Str
   (s/required-key :user) s/Str
   (s/required-key :logs) [LogConfig]
   (s/optional-key :sudo) s/Bool})

(def Config
  {(s/required-key :hosts) [HostConfig]
   (s/optional-key :started) s/Num})

(def config {:started (System/currentTimeMillis)})

(s/defn ^:always-validate load-config :- Config
  [new-config :- Config]
  (alter-var-root (var config) (fn [c] (merge c new-config))))