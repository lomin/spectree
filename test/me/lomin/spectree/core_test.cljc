(ns me.lomin.spectree.core-test
  (:require [me.lomin.spectree :as s :refer [+>> each+>>]]
            [me.lomin.spectree.hiccup]
            [me.lomin.spectree.keyword :as spectree-keyword]
            [com.rpl.specter :as specter]
            #?(:cljs [cljs.test :refer-macros [deftest is testing]])
            #?(:clj
               [clojure.test :refer [deftest is testing]])))

(def hiccup [:PS
             [:HEADER [:word "header"]]
             [:LINE
              [:UID [:number "501"]]
              [:PID [:number "45427"]]
              [:CMD [:word "bash"]]]
             [:LINE
              [:UID [:number "502"]]
              [:PID [:number "45428"]]
              [:CMD [:word "top"]]]])

(deftest similarities-to-specter
  (is (= (specter/select [:CMD] hiccup)
         (specter/select #each/key :CMD hiccup))))

(deftest differences-with-specter
  (is (not= (specter/select [:CMD] hiccup)
            (specter/select #each/key :hiccup/CMD hiccup)))

  (is (not=
        (specter/select [specter/FIRST] hiccup)
        (specter/select [(s/each specter/FIRST)] hiccup))))

(defn- tagged?
  ([tag] (partial tagged? tag))
  ([tag x] (and (vector? x) (= tag (first x)))))

(defn- some-tagged?
  ([tags] (partial some-tagged? tags))
  ([tags x] (some #(tagged? % x) tags)))

(defn- mapify-hiccup [selectors [tag & args :as tagged-value]]
  (let [not-uniques (->> tagged-value
                         (specter/select-one [(specter/filterer (some-tagged? selectors))])
                         (group-by first))]
    {tag (merge (reduce (fn [m [k v]] (assoc m k v)) not-uniques args)
                (specter/transform [specter/MAP-VALS specter/ALL]
                                   (fn [[_ & args]] (into {} args))
                                   not-uniques))}))

(deftest ^:unit hiccup-test
  (is (= ["bash" "top"]
         (specter/select #each/key [:hiccup/CMD :hiccup/word 1]
                         hiccup)))

  (is (= [[[:UID [:number "501"]] [:PID [:number "45427"]] [:CMD [:word "bash"]]]
          [[:UID [:number "502"]] [:PID [:number "45428"]] [:CMD [:word "top"]]]]
         (specter/select #each/key [:hiccup/LINE (specter/srange-dynamic (constantly 1) count)]
                         hiccup)))

  (is (= {:PS {:HEADER [:word "header"],
               :LINE   [{:CMD [:word "bash"], :PID [:number "45427"], :UID [:number "501"]}
                        {:CMD [:word "top"], :PID [:number "45428"], :UID [:number "502"]}]}}
         (specter/transform [] (partial mapify-hiccup #{:LINE})
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

(deftest sayang-example-test
  (testing "finds and removes sayang spec information"
    (let [no-sayang-spec-code '(f-name {}
                                       ([x y] (str x y))
                                       ([x] (str x)))
          sayang-spec-code '(f-name
                              {}
                              ([[x :- number?]
                                [y :- ::test-type]]
                                (str x y))
                              ([[x :- number?]]
                                (str x)))
          sayang-spec-selector #(and (vector? %)
                                     (= (count %) 3)
                                     (let [[_ sep _] %]
                                       (= :- sep)))
          sayang-spec-remover (fn [[k]] k)]

      (is (= no-sayang-spec-code
             (specter/transform (s/each sayang-spec-selector)
                                sayang-spec-remover
                                sayang-spec-code))))))

(deftest ^:unit specter-select-each-keyword-equivalence-test
         (is (= (specter/select
                  [(spectree-keyword/each :hiccup/li) (specter/selected? #(= 2 (second %)))]
                  [:section [:ul [:li 1] [:li 2]]])
                (specter/select
                  #each/key [:hiccup/li (specter/selected? #(= 2 (second %)))]
                  [:section [:ul [:li 1] [:li 2]]])))

         (is (= (specter/select
                  [(spectree-keyword/each :hiccup/li) (specter/filterer #(= % [:li 2]))]
                  [:section [:ul [:li 1] [:li 2]]])
                (specter/select
                  #each/key [:hiccup/li (specter/filterer #(= % [:li 2]))]
                  [:section [:ul [:li 1] [:li 2]]]))))
