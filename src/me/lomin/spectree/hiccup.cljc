(ns me.lomin.spectree.hiccup
  (:require [me.lomin.spectree.tree-search :as tree-search]
            [me.lomin.spectree.keyword :as keyword]
            [com.rpl.specter :as specter]))

(defmethod keyword/selector :hiccup [_ k _]
  (tree-search/selector [specter/FIRST #(= k %)]))
