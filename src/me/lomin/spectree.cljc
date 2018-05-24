(ns me.lomin.spectree
  (:require [me.lomin.spectree.tree-search :as tree-search]
            [me.lomin.spectree.keyword :as keyword])
  #?(:cljs (:require-macros me.lomin.spectree)))

(defprotocol Each
  (each [self]))

(extend-type #?(:clj clojure.lang.IPersistentVector :cljs cljs.core/PersistentVector)
  Each
  (each [self]
    (mapv #(if (satisfies? Each %) (each %) %) self)))

(extend-type #?(:clj clojure.lang.Keyword :cljs cljs.core/Keyword)
  Each
  (each [self]
    (keyword/selector (keyword (namespace self))
                      (keyword (name self))
                      self)))

(extend-type #?(:clj clojure.lang.AFunction :cljs function)
  Each
  (each [self]
    (tree-search/selector self)))

#?(:clj
   (do

     (defn- pairs
       ([args] (pairs [] nil args))
       ([pairs spare args]
        (if-let [x0 (first args)]
          (if-let [x1 (second args)]
            (recur (conj pairs [x0 x1]) spare (nnext args))
            [pairs x0])
          [pairs spare])))

     (defn- +>>* [selector-wrapper f args]
       (let [[selector-transformation-pair coll] (pairs args)
             selector+transformer (for [[selector transformation] selector-transformation-pair]
                                    (list f
                                          (list selector-wrapper selector)
                                          transformation))]
         (if coll
           (concat (list '->> coll) selector+transformer)
           (let [sym (gensym)]
             (list 'fn [sym] (+>>* selector-wrapper f (concat args [sym])))))))

     (defmacro each+>> [f & args]
       (+>>* 'me.lomin.spectree/each f args))

     (defmacro +>> [f & args]
       (+>>* 'do f args))))