(defproject fx-commander "0.1.0-SNAPSHOT"
            :description "FIXME: write description"
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.7.0-alpha4"]
                           [fx-clj "0.2.0-SNAPSHOT"]
                           [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                           [juxt/dirwatch "0.2.2"]
                           [me.raynes/fs "1.4.6"]]
            :target-path "target/%s"
            :profiles {:uberjar {:aot :all}})
