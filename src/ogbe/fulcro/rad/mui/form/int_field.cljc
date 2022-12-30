(ns ogbe.fulcro.rad.mui.form.int-field
  (:require
   [com.fulcrologic.fulcro.dom.events :as evt]
   [com.fulcrologic.fulcro.dom.inputs :as inputs]
   [ogbe.fulcro.rad.mui.form.field :as field]))

(defn to-int
  [evt]
  (->> evt evt/target-value (re-find #"^-?\d+") inputs/to-int))

(def render-field
  (field/render-field-factory {:evt->model to-int}
                              {:type "number"}))
