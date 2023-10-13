(ns scarlet.anyver
  "Parse and compare generic version strings.

   Does not assume any versioning scheme such as Semantic Versioning, but
   simply interprets sequences of digits and sequences of letters as parts,
   ignoring punctuation and whitespace.

   Compares parts lexicographically, filling shorter versions with `nil`.
   Types are compared as `int > nil > string`.  That means that `2.1.0` is
   greater than `2.1`, and `2.1` is greater than `2.1-alpha2`."
  (:require [clojure.string :as str]))

(defn- compare-parts [x y]
  (cond
    (int? x) (if (int? y) (compare x y) 1)
    (int? y) -1
    (nil? x) (if (nil? y) 0 1)
    (nil? y) -1
    :else (compare x y)))

(defn- compare-vectors [x y]
  (let [dlen (compare (count x) (count y))
        [x y] (if (zero? dlen)
                [x y]
                (if (neg? dlen)
                  [(concat x (repeat nil)) y]
                  [x (concat y (repeat nil))]))]
    (->> (map compare-parts x y)
         (reduce (fn [a c] (if (zero? c) a (reduced c))) dlen))))

(declare compare-versions)

(deftype Version [v s]
  Object
  (toString [_] (or s (str/join "." v)))

  #?@(:clj
      [(equals [_ other] (and (instance? Version other)
                              (= v (.-v ^Version other))))
       (hashCode [_] (hash v))

       clojure.lang.IHashEq
       (hasheq [_] (hash v))

       clojure.lang.Counted
       (count [_] 2)

       clojure.lang.Seqable
       (seq [_] (seq v))

       java.io.Serializable

       java.lang.Comparable
       (compareTo [this other] (compare-versions this other))]

      :cljs
      [IEquiv
       (-equiv [_ other] (and (instance? Version other)
                              (= v (.-v ^Version other))))

       IHash
       (-hash [_] (hash v))

       ICounted
       (-count [_] 2)

       ISeqable
       (-seq [_] (seq v))

       IComparable
       (-compare [this other] (compare-versions this other))

       IPrintWithWriter
       (-pr-writer [this writer opts]
                   (-write writer "#version ")
                   (-pr-writer writer (.toString this) opts))]))

#?(:clj
   (defmethod print-dup Version [^Version version ^java.io.Writer w]
     (.write w "#version ")
     (print-dup (.toString version) w)))

#?(:clj
   (defmethod print-method Version [version w]
     (print-dup version w)))

(defn- compare-versions
  [^Version version1 ^Version version2]
  (compare-vectors (.-v version1) (.-v version2)))

(defn version?
  "Returns true if `x` is an instance of `Version`."
  [x]
  (instance? Version x))

(defn parse
  "Parse `s` as version."
  ^Version [s]
  (let [v (->> (re-seq #"([0-9]+)|[a-zA-Z]+" s)
               (mapv (fn [[s n]] (if n (parse-long n) s))))]
    (Version. (cond-> v (= "v" (first v)) (subvec 1)) s)))

(def empty-version (Version. [] nil))

(defn version
  "Coerce `x` to version."
  ^Version [x]
  (cond
    (version? x) x
    (string? x) (parse x)
    (vector? x) (Version. x nil)
    (integer? x) (Version. [x] nil)
    (nil? x) empty-version
    :else (throw (ex-info (str "Cannot coerce " x " to Version") {}))))

(defn version-compare
  "Coerces and compares two versions `x` and `y`."
  [x y]
  (compare-versions (version x) (version y)))

(defn earlier?
  "Returns true if `x` is earlier than `y`, false otherwise."
  {:arglists '([x] [x y] [x y & more])}
  ([_] true)
  ([x y]
   (neg? (version-compare x y)))
  ([x y & more]
   (if (earlier? x y)
     (if (next more)
       (recur y (first more) (next more))
       (earlier? y (first more)))
     false)))

(defn later?
  "Returns true if `x` is later than `y`, false otherwise."
  {:arglists '([x] [x y] [x y & more])}
  ([_] true)
  ([x y]
   (pos? (version-compare x y)))
  ([x y & more]
   (if (later? x y)
     (if (next more)
       (recur y (first more) (next more))
       (later? y (first more)))
     false)))

(defn latest
  "Returns the latest version in `coll`."
  [coll]
  (first (reduce (fn [latest x]
                   (let [ver (version x)]
                     (if (later? ver (second latest))
                       [x ver]
                       latest)))
                 [nil empty-version]
                 coll)))

(defn version-sort
  "Sort a collection of versions."
  [coll]
  (sort-by version compare-versions coll))
