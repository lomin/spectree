(ns me.lomin.spectree.hiccup
  (:require [me.lomin.spectree :as spectree]
            [me.lomin.spectree.keyword :as keyword]
            [com.rpl.specter :as specter]))

(defmethod keyword/selector :hiccup [ns k ns+k]
  (spectree/each [specter/FIRST #(= k %)]))
