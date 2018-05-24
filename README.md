# spectree

This library for both Clojure and ClojureScript provides an 'each' selector to use in combination with specter and tree-like data structures.
It also provides the macros '+>>' and 'each+>>' to chain specter transformations.

## Leiningen

*spectree* is available from Clojars. Add the following dependency to your *project.clj*:

[![Clojars Project](https://img.shields.io/clojars/v/me.lomin.spectree.svg)](https://clojars.org/me.lomin.spectree)

## Usage

```clojure
...
(require '[me.lomin.spectree.hiccup])

(deftest hiccup-test
  (is (= ["bash" "top"]
         (specter/select (spectree/each [:hiccup/CMD :hiccup/word 1])
                         [:PS
                          [:HEADER [:word "header"]]
                          [:LINE
                           [:UID [:number "501"]]
                           [:PID [:number "45427"]]
                           [:CMD [:word "bash"]]]
                          [:LINE
                           [:UID [:number "502"]]
                           [:PID [:number "45428"]]
                           [:CMD [:word "top"]]]]))))
                           
(deftest generic-examples-test
  (is (= {:a [:x [:c 2]] :e "add-if-not-here"}
           (each+>> specter/transform
                    :hiccup/b (constantly :x)
                    :e (constantly "add-if-not-here")
                    :must/f (constantly "not-here")
                    {:a [[:b 1] [:c 2]]})))

  (is (= {:a 6 :b 4}
         (+>> specter/transform
              [:a] inc
              [:b] dec
              {:a 5 :b 5})))

  (is (= {:a 6 :b 4 :c {:d 6 :e 4 :f [0 2 2]}}
         ((+>> specter/transform
               [:a] inc
               [:c] (+>> specter/transform
                         [:d] inc
                         [:e] dec
                         [:f] (+>> specter/transform
                                   1 inc))
               [:b] dec)
           {:a 5 :b 5 :c {:d 5 :e 5 :f [0 1 2]}}))))       
                                                          
```

## License

Copyright © 2018 Steven Collins

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
