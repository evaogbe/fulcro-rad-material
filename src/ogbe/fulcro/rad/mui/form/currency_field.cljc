(ns ogbe.fulcro.rad.mui.form.currency-field
  (:require
   [clojure.string :as str]
   [com.fulcrologic.fulcro.dom.events :as evt]
   [com.fulcrologic.rad.type-support.decimal :as math]
   [ogbe.fulcro.rad.mui.form.field :as field]))

(defn to-currency
  [evt]
  (-> evt evt/target-value (str/replace #"[$,]" "") math/numeric))

(def render-field
  (field/render-field-factory {:evt->model to-currency
                               :model->str math/numeric->currency-str}))
