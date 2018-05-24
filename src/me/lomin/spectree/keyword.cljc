(ns me.lomin.spectree.keyword
  (:require [me.lomin.spectree.tree-search :as tree-search]
            [com.rpl.specter :as specter]))

(defmulti selector (fn [ns k ns+k] ns) :default ::none)
(defmethod selector ::none [_ _ ns+k] ns+k)
(defmethod selector :must [_ k _] (tree-search/selector (specter/must k)))

