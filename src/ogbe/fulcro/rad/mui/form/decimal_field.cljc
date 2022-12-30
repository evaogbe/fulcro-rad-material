(ns ogbe.fulcro.rad.mui.form.decimal-field
  (:require
   [com.fulcrologic.fulcro.dom.events :as evt]
   [com.fulcrologic.rad.type-support.decimal :as math]
   [ogbe.fulcro.rad.mui.form.field :as field]))

(defn to-decimal
  [evt]
  (->> evt evt/target-value (re-find #"^-?\d*(\.\d*)?") first math/numeric))

(def render-field
  (field/render-field-factory {:evt->model to-decimal
                               :model->str math/numeric->str}
                              {:type "number"}))
