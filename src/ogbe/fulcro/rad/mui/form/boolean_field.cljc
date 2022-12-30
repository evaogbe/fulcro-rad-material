(ns ogbe.fulcro.rad.mui.form.boolean-field
  (:require
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [ogbe.fulcro.mui.inputs.checkbox :refer [ui-checkbox]]
   [ogbe.fulcro.mui.inputs.form-control-label :refer [ui-form-control-label]]
   [ogbe.fulcro.rad.mui.form.field :as field]
   [ogbe.fulcro.rad.mui.form-options :as mfo]))

(defn render-field
  [{::form/keys [form-instance] :as env} attribute]
  (let [k (::attr/qualified-key attribute)
        props (comp/props form-instance)
        user-props  (?! (form/field-style-config env attribute :input/props) env)
        field-label (or (form/field-label env attribute) (field/default-field-label k))
        visible? (form/field-visible? form-instance attribute)
        read-only? (form/read-only? form-instance attribute)
        top-class (mfo/top-class form-instance attribute)
        value (get props k false)]
    (when visible?
      (ui-form-control-label
       {:className top-class
        :control (ui-checkbox (merge {:checked value
                                      :onChange (fn [_evt]
                                                  (let [v (not value)]
                                                    (form/input-blur! env k v)
                                                    (form/input-changed! env k v)))}
                                     user-props))
        :disabled (boolean read-only?)
        :key (str k)
        :label field-label}))))
