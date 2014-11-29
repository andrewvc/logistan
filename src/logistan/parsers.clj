(ns logistan.parsers
  [:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [plumbing.core :refer [safe-get]]
            [schema.core :as s]
            [clojure.pprint :as pp]])

(defn load-patterns [file]
  (with-open [rdr (io/reader file)]
    (reduce (fn [acc line]
              (if-let [[_ name pattern] (re-matches #"^([A-Za-z0-9_]+)\s+(.+)" line)]
                (assoc acc name pattern)
                acc))
            {}
            (line-seq rdr)))
  )

(def default-patterns (load-patterns (io/file (io/resource "default-grok-patterns"))))

(def patterns default-patterns)

(declare compile-pattern)

(defn resolve-name [name]
  (compile-pattern (safe-get patterns name)))

(defn compile-pattern [pattern]
  (string/replace
    pattern
    #"%\{([^\}]+)\}"
    (fn [[_ match]]                                         ; We only need the capture group, not the %{}
      (let [[reference name] (string/split match #"\:")]
        (if name
          (format "(?<%s>%s)" name (resolve-name reference))
          (resolve-name reference))))))

(defn group-names [pattern]
  (if-let [matches (re-seq #"\(\?<([a-zA-Z][a-zA-Z0-9]*)>" pattern)]
    (map second matches)
    []))

(def CompiledPattern
  {(s/required-key :regexp) s/Regex
   :groups [s/Str]
   (s/required-key :source) s/Str
   })

;; Compiled copy of patterns, keys map to regexps with capture groups
(def compiled-patterns
  (into {} (map (fn [[name source]]
                  (let [compiled (compile-pattern source)]
                    [name {:regexp (re-pattern compiled)
                           :groups (group-names (str compiled))
                           :source source}]))
                patterns)))

(s/defn group-vals
  "Takes a seq of group name strings and a Regexp.Matcher and returns a map of group names -> vals"
  [matcher :- java.util.regex.Matcher groups :- [s/Str]]
  (if (.matches matcher)
    (into {} (map (fn [name] [name (.group matcher name)])
                  groups))
    {}))

(s/defn match-line
  [regexp :- s/Regex groups :- (s/maybe [s/Str]) line :- s/Str]
  (group-vals (re-matcher regexp line) groups))

(s/defn ^:always-validate pattern-parser
  "Returns a parser for a given pattern name"
  [{:keys [regexp groups]} :- CompiledPattern]
  (fn [lines]
    (map
      (fn [line]
        (let [matches (match-line regexp groups line)]
          (assoc matches :_source line)))
      lines)))

(def parsers (into {} (map (s/fn [[name compiled-pattern]]
                             [name (pattern-parser compiled-pattern)])
                           compiled-patterns)))

(defn get-parser [name] (safe-get parsers name))