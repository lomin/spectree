(ns me.lomin.spectree.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [pjstadig.humane-test-output]
            [me.lomin.spectree.core-test]))

(doo-tests 'me.lomin.spectree.core-test)
