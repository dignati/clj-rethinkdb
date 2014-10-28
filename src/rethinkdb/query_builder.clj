(ns rethinkdb.query-builder
  (:require [clojure.data.json :as json]
            [clj-time.coerce :as c]
            [rethinkdb.types :refer [tt->int qt->int]]
            [rethinkdb.utils :refer [snake-case]]))

(declare parse-term)

(defn snake-case-keys [m]
  (into {}
    (for [[k v] m]
      [(snake-case k) v])))

(defn term [term args & [optargs]]
  {::term term
   ::args args
   ::optargs optargs})

(defmulti parse-arg
  (fn [arg]
    (cond
      (::term arg) :query
      (or (sequential? arg) (seq? arg)) :seq
      (map? arg) :map)))

(defmethod parse-arg :query [arg]
  (parse-term arg))

(defmethod parse-arg :seq [arg]
  (parse-term (term :MAKE_ARRAY arg)))

(defmethod parse-arg :map [arg]
  (zipmap (keys arg) (map parse-arg (vals arg))))

(defmethod parse-arg :default [arg]
  arg)

(defn parse-term [{term ::term args ::args optargs ::optargs}]
  (filter identity
          [(tt->int term)
           (map parse-arg (seq args))
           (if optargs (snake-case-keys optargs))]))

(defn parse-query
  ([type]
   [(qt->int type)])
  ([type term]
   [(qt->int type) (parse-term term)]))
