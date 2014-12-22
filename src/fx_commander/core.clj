(ns fx-commander.core
  (:import (java.util ArrayList)
           (javafx.collections FXCollections)
           (javafx.scene.control TableView))
  (:require [fx-clj.core :as fx])
  (:require [juxt.dirwatch :refer [watch-dir]])
  (:require [me.raynes.fs :refer [list-dir]])
  (:require [clojure.core.async :refer [chan go <! >! put!]]))

(def fs-list
  (FXCollections/observableArrayList
    (let [list (ArrayList.)]
      (doseq [item (map (fn [file] {:filename (.getPath file) :filesize (.length file)}) (list-dir "test-dir"))] (.add list item))
      list)))

(def table-view (fx/table-view
                  {:columns [(fx/table-column {:text               "Filename"
                                               :cell-value-factory (fn [i] (fx/observable-property (.getValue i) :filename))
                                               })
                             (fx/table-column {:text               "Size"
                                               :cell-value-factory (fn [i] (fx/observable-property (.getValue i) :filesize))
                                               })]
                   :items              fs-list
                   :columnResizePolicy TableView/UNCONSTRAINED_RESIZE_POLICY}))

(defn create-view []

  (let [click-ch (chan)
        watch-ch (chan)
        btn (fx/button :#my-btn {:on-action click-ch        ; You can bind a core.async channel directly to an event
                                 :text      "Copy Path"})
        txt (fx/text)
        view (fx/v-box {:prefWidth 800} txt btn table-view)]

    (watch-dir #(put! watch-ch %) (clojure.java.io/file "test-dir"))

    (go
      (while true
        (let [click-event (<! click-ch)
              sel-item (-> (.getSelectionModel table-view) (.getSelectedItem))]
          (fx/run<! (fx/pset! txt (:filename sel-item))))))

    (go
      (while true
        (let [fs-event (<! watch-ch)
              file (:file fs-event)]
          (println fs-event)
          (condp = (:action fs-event)
            :create (fx/run<! (.add fs-list {:filename (.getPath file) :filesize (.length file)}))
            :delete (println "todo: remove")
            :modify (println "todo: modify")))))

    view))

(fx/sandbox #'create-view)
;; Creates a "sandbox" JavaFX window to
;; show the view. Clicking F5 in this
;; window will refresh the view allowing the
;; create-view function to be updated at the REPL

