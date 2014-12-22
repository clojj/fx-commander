(ns fx-commander.core
  (:import (java.util ArrayList)
           (javafx.collections FXCollections))
  (:require [fx-clj.core :as fx])
  (:require [juxt.dirwatch :refer [watch-dir]])
  (:require [me.raynes.fs :refer [list-dir]])
  (:require [clojure.core.async :refer [chan go <! >! put!]]))

(def fs-list
  (FXCollections/observableArrayList
    (let [list (ArrayList.)]
      (doseq [f (map (fn [f] {:filename (.getPath f) :filesize (.length f)}) (list-dir "./test-dir"))] (.add list f))
      list)))

(def table-view (fx/table-view
                  {:columns [(fx/table-column {:text               "Filename"
                                               :cell-value-factory (fn [i] (fx/observable-property (.getValue i) :filename))
                                               })
                             (fx/table-column {:text               "Size"
                                               :cell-value-factory (fn [i] (fx/observable-property (.getValue i) :filesize))
                                               })]
                   :items   fs-list}))

(defn create-view []

  (let [click-ch (chan)
        watch-ch (chan)
        btn (fx/button :#my-btn {:on-action click-ch        ; You can bind a core.async channel directly to an event
                                 :text      "Copy Path"})
        txt (fx/text "Initial text")
        view (fx/v-box txt btn table-view)]

    (watch-dir #(put! watch-ch %) (clojure.java.io/file "/Users/jwin/ClojureProjects/fx-commander"))

    (go
      (while true
        (let [click-event (<! click-ch)
              sel-item (-> (.getSelectionModel table-view) (.getSelectedItem))]
          (fx/run<! (fx/pset! txt (:filename sel-item))))))

    (go
      (while true
        (let [fs-event (<! watch-ch)]
          (println fs-event)
          (when-let [f (when (= (:action fs-event) :create) (:file fs-event))]
            (.add fs-list {:filename (.getPath f) :filesize (.length f)})))))

    view))

(fx/sandbox #'create-view)
;; Creates a "sandbox" JavaFX window to
;; show the view. Clicking F5 in this
;; window will refresh the view allowing the
;; create-view function to be updated at the REPL

