(ns dora.util
  "Various utility functions"
  (:require [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.java.shell :refer :all]
            [clojure.string :as s]
            [clojure.tools.analyzer.passes.trim :as str]
            [formaterr.core :refer :all]
            [mongerr.core :refer :all]))

(defn distinct-by
  "Returns a lazy sequence of the elements of coll, removing any elements that
  return duplicate values when passed to a function f."
  [f coll]
  (let [step (fn step [xs seen]
               (lazy-seq
                ((fn [[x :as xs] seen]
                   (when-let [s (seq xs)]
                     (let [fx (f x)]
                       (if (contains? seen fx)
                         (recur (rest s) seen)
                         (cons x (step (rest s) (conj seen fx)))))))
                 xs seen)))]
    (step coll #{})))

(defn shs [& args]
  (let [result (apply sh args)]
    (println (:err result))
    ;(println (:exit result))
    (s/trim (:out result))))

(defn clone [repo route]
  (let [result (sh "git" "clone" (str "https://github.com/" repo) route)]
    (if (re-find #"not found" (:err result))
      (throw (ex-info "Repository not found" {:status :non-existant}))
      (if (re-find #"already exists and is not an empty directory" (:err result))
        (throw (ex-info "Repository already exists" {:status :existant}))))
    (:err result)))

(defn pull
  ([]
   (shs "git" "pull"))
  ([dir]
   (shs "git" "pull" :dir dir))
  ([dir origin branch]
   (shs "git" "pull" origin branch)))

(defn push
  ([]
   (shs "git" "push"))
  ([dir]
   (shs "git" "push" :dir dir))
  ([dir remote branch]
   (shs "git" "push" remote branch :dir dir)))

(defn push-force
  ([]
   (shs "git" "push" "--force"))
  ([dir]
   (shs "git" "push" "--force" :dir dir))
  ([dir remote branch]
   (shs "git" "push" remote branch "--force" :dir dir)))
(defn adda
  ([]
   (shs "git" "add" "-A"))
  ([dir]
   (shs "git" "add" "-A" :dir dir)))

(defn commit
  ([] (commit "save"))
  ([msg] (shs "git" "commit" "-m" msg))
  ([msg dir]
   (shs "git" "commit" "-m" msg :dir dir)))

(defn ggg []
  (adda)
  (commit)
  (push))

(defn gs []
  (shs "git" "status"))

(defn branch [] (-> (sh "git" "rev-parse" "--abbrev-ref" "HEAD") :out str/trim))

(defn checkout
  ([dir branch]
   (sh "git" "checkout" branch :dir dir)))

(defn checkout-B
  ([dir branch]
   (sh "git" "checkout" "-B" branch :dir dir)))

(defn git-merge []
  (shs "git" "merge" "master"))

(defn dir-with-slash [dir]
  (if (= "/" (last dir))
    dir
    (str dir "/")))

(defn ls&
  ([] (ls& "."))
  ([dir]
   (rest (remove #(re-find #"git"%) (map #(.getPath %)
                                         (file-seq (clojure.java.io/file dir)))))))

(defn ls
  ([] (ls "."))
  ([dir]
    (.list (io/file dir))))

(defn shell-wrapper [cmd & args]
  (let [response (apply shs cmd args)]
    (if (empty? response)
      :ok
      response)))

(defn mv
  "move a file from a to b"
  [a b]
  (let [response (shs "mv" a b)]
    (if (empty? response)
      :ok
      response)))

(defn rm [& files]
  (apply shell-wrapper "rm" files))

(defn ends-in-dash? [s]
  (= \/ (last s)))

(defn normalize-dir [dir]
  (if-not (ends-in-dash? dir)
    (str dir "/")
    dir))

(defn ls-fr
  "ls with full route"
  [dir]
  (map #(str (normalize-dir dir) %) (ls dir)))

(defn ls-fr-r
  "ls with full route recursive"
  [dir]
  (let [content (ls-fr dir)
        inside (mapcat ls-fr content)]
    (concat content inside)))

(defn remove-str [s match]
  (s/replace s match ""))

(defn is-directory?
  "Predicado para checar si el archivo es directorio"
  [route]
  (try
    (.isDirectory (io/file route))
    (catch Exception e false)))

(defn remove-transparencia-string
  [s]
  (remove-str (remove-str s  "=\" ") "\""))

(defn trim-transparencia-map
  [m]
  (zipmap (keys m)
          (map remove-transparencia-string (vals m))))

(defn trim-transparencia-csv [maps]
  (map trim-transparencia-map maps))

(defn feval                            ;TODO: security issue
  [s]
  (eval (read-string s)))

(defn clean-get
  [url]
  (:body (http/get url)))

(defn find-rel
  "Query kv from rel"
  [k v rel]
  (keep #(if (= (k %) v) %) rel))

(defn or*
  "Apply 'or' to a list of predicates"  ;Because u cant do
  [coll]                                ; (apply or [true true])
  (true? (some true? coll)))

(defn rel?
  "Is this a rel?"
  [o]
  (try (or* (map map? o))
       (catch Exception e false)))

(defn truthy?
  "Is this truthy or falsley"
  [o]
  (if o
    true
    false))

(defn map-vals
  "Apply f to the values of m"
  [f m]
  (zipmap (keys m) (map f (vals m))))

(defn insert-csv
  "insert NAME.csv into collection NAME"
  [name]
  (db-insert name (csv (str name ".csv"))))
