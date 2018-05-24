(defproject me.lomin.spectree "0.1.0"
  :description "Provides an 'each' selector to use in combination with specter and tree-like data structures."
  :url "https://github.com/lomin/spectree"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [com.rpl/specter "1.1.1"]]
  :test-selectors {:default     (constantly true)
                   :unit        :unit
                   :focused     :focused}
  :aliases {"test-all" ["do"
                        ["test"]
                        ["doo" "phantom" "test" "once"]]}
  :doo {:build "test"
        :alias {:default [:phantom]}}
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "target/testable.js"
                                   :output-dir "target"
                                   :main me.lomin.spectree.test-runner
                                   :optimizations :none}}]}
  :profiles {:dev {:dependencies [[pjstadig/humane-test-output "0.8.3"]]
                   :plugins [[lein-doo "0.1.10"]
                             [com.jakemccrary/lein-test-refresh "0.21.1"]
                             [lein-ancient "0.6.14"]
                             [jonase/eastwood "0.2.5"]
                             [lein-cljfmt "0.5.7"]]}})
