(ns me.lomin.spectree
  (:require [com.rpl.specter :as specter])
  #?(:cljs (:require-macros me.lomin.spectree)))

(defn- each* [arg]
  (cond
    (keyword? arg) (list 'me.lomin.spectree.keyword/each arg)
    (vector? arg) (mapv each* arg)
    :else arg))

(def each
  (specter/recursive-path [path]
                          p
                          (specter/cond-path sequential?
                                             (specter/if-path path
                                                              (specter/continue-then-stay specter/ALL p)
                                                              [specter/ALL p])
                                             map?
                                             (specter/if-path path
                                                              (specter/continue-then-stay specter/MAP-VALS p)
                                                              [specter/MAP-VALS p]))))



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
                                          (selector-wrapper selector)
                                          transformation))]
         (if coll
           (concat (list '->> coll) selector+transformer)
           (let [sym (gensym)]
             (list 'fn [sym] (+>>* selector-wrapper f (concat args [sym])))))))

     (defmacro each+>> [f & args]
       (+>>* each* f args))

     (defmacro +>> [f & args]
       (+>>* identity f args))))