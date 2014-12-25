(ns fx-commander.core
  (:import (java.util ArrayList)
           (javafx.collections FXCollections)
           (javafx.scene.control TableView)
           (javafx.beans.value ChangeListener))
  (:require [fx-clj.core :as fx])
  (:require [juxt.dirwatch :refer [watch-dir]])
  (:require [me.raynes.fs :refer [list-dir]])
  (:require [clojure.core.async :refer [chan go <! >! put!]]))

(def fs-list
  (FXCollections/observableArrayList
    (let [list (ArrayList.)]
      (doseq [item (list-dir "test-dir")]
        (.add list item))
      list)))

(def table-view (fx/table-view
                  {:columns [(fx/table-column {:text               "Filename"
                                               :cell-value-factory (fn [i] (fx/observable-property (.getValue i) (fn [item] (.getName item))))
                                               })
                             (fx/table-column {:text               "Size"
                                               :cell-value-factory (fn [i] (fx/observable-property (.getValue i) (fn [item] (.length item))))
                                               })]
                   :items              fs-list
                   :columnResizePolicy TableView/UNCONSTRAINED_RESIZE_POLICY}))

(def label-show-path (fx/label))

(defn delete-handler [e]
  (let [file (-> table-view .getSelectionModel .getSelectedItem)
        i (-> table-view .getSelectionModel .getSelectedIndex)]
    (when (.delete file)
      (println "deleted " file)
      (.remove fs-list i (inc i)))))

(defn- bind-selection-listener! [vc]
  (let [l (reify ChangeListener
            (changed [_ prop ov nv]
              (when nv
                (fx/pset! label-show-path (.getCanonicalPath nv)))))]
    (-> vc .getSelectionModel .selectedItemProperty (.addListener l))))

(defn create-view []

  (let [watch-ch (chan)
        btn (fx/button :#my-btn {:on-action delete-handler
                                 :text      "Delete"})
        view (fx/v-box {:prefWidth 800} label-show-path btn table-view)]

    (bind-selection-listener! table-view)

    (watch-dir #(put! watch-ch %) (clojure.java.io/file "test-dir"))

    (go
      (while true
        (let [fs-event (<! watch-ch)
              file (:file fs-event)]
          (println fs-event)
          (condp = (:action fs-event)
            :create (fx/run<! (.add fs-list file))
            :delete (println "todo: remove")
            :modify (println "todo: modify")))))

    view))

(fx/sandbox #'create-view)
;; Creates a "sandbox" JavaFX window to
;; show the view. Clicking F5 in this
;; window will refresh the view allowing the
;; create-view function to be updated at the REPL

