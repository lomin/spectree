(ns me.lomin.spectree.core-test
  (:require [me.lomin.spectree :as s :refer [+>> each+>>]]
            [me.lomin.spectree.hiccup]
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
  (is (= [:PS]
         (specter/select [specter/FIRST] hiccup)
         (specter/select specter/FIRST hiccup)
         (specter/select (s/each [specter/FIRST]) hiccup)))

  (is (= (specter/select [:CMD] hiccup)
         (specter/select (s/each :CMD) hiccup))))

(deftest differences-with-specter
  (is (not= (specter/select [:CMD] hiccup)
            (specter/select (s/each :hiccup/CMD) hiccup))))

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

(deftest hiccup-test
  (is (= ["bash" "top"]
         (specter/select (s/each [:hiccup/CMD :hiccup/word 1])
                         hiccup)))

  (is (= [[[:UID [:number "501"]] [:PID [:number "45427"]] [:CMD [:word "bash"]]]
          [[:UID [:number "502"]] [:PID [:number "45428"]] [:CMD [:word "top"]]]]
         (specter/select (s/each [:hiccup/LINE (specter/srange-dynamic (constantly 1) count)])
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
                  (tagged? :b) (constantly :x)
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
