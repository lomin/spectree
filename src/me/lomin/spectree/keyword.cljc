(ns me.lomin.spectree.keyword
  (:require [me.lomin.spectree :as tree-search]
            [com.rpl.specter :as specter]))

(defmulti selector (fn [ns k ns+k] ns) :default ::none)
(defmethod selector ::none [_ _ ns+k] ns+k)
(defmethod selector :must [_ k _] (tree-search/each (specter/must k)))

(defn each [k]
  (selector (keyword (namespace k))
            (keyword (name k))
            k))