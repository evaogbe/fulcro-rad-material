(ns ogbe.fulcro.rad.mui.form.enumerated-field
  (:require
   [clojure.string :as str]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.dom.events :as evt]
   [com.fulcrologic.fulcro-i18n.i18n :refer [tr]]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.form-options :as fo]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [ogbe.fulcro.mui.inputs.text-field :refer [ui-text-field]]
   [ogbe.fulcro.mui.navigation.menu-item :refer [ui-menu-item]]
   [ogbe.fulcro.rad.mui.components.select :as select]
   [ogbe.fulcro.rad.mui.form.field :as field]
   [ogbe.fulcro.rad.mui.form-options :as mfo]))

(defn enumerated-options
  [{::form/keys [form-instance]} {::attr/keys [qualified-key] :as attribute}]
  (let [{::attr/keys [enumerated-values]} attribute
        enumeration-labels (merge
                            (::attr/enumerated-labels attribute)
                            (comp/component-options form-instance
                                                    fo/enumerated-labels qualified-key))]
    (->> enumerated-values
         (map (fn [k]
                {:text  (?! (get enumeration-labels k (name k)))
                 :value k}))
         (sort-by :text))))

(defn- render-to-many
  [{::form/keys [form-instance] :as env}
   {::attr/keys [computed-options qualified-key required?] :as attribute}]
  (when (form/field-visible? form-instance attribute)
    (let [props (comp/props form-instance)
          read-only? (form/read-only? form-instance attribute)
          options (or (?! computed-options env) (enumerated-options env attribute))
          top-class (mfo/top-class form-instance attribute)
          selected-ids (get props qualified-key)]
      (ui-text-field
       {:className top-class
        :fullWidth true
        :key (str qualified-key)
        :label (or (form/field-label env attribute) (field/default-field-label qualified-key))
        :required (boolean required?)
        :select true
        :SelectProps {:multiple true
                      :onChange (fn [evt]
                                  (let [value (evt/target-value evt)
                                        value (cond-> value
                                                (string? value) (str/split #","))]
                                    (form/input-changed! env qualified-key (set value))))
                      :value (vec selected-ids)}}
       (mapv (fn [{:keys [text value]}]
               (ui-menu-item
                {:disabled read-only?
                 :key value
                 :value value}
                text))
             options)))))

(defn- render-to-one
  [{::form/keys [form-instance] :as env}
   {::attr/keys [computed-options qualified-key required?] :as attribute}]
  (when (form/field-visible? form-instance attribute)
    (let [props (comp/props form-instance)
          read-only? (form/read-only? form-instance attribute)
          invalid? (form/invalid-attribute-value? env attribute)
          user-props (?! (form/field-style-config env attribute :input/props) env)
          options (or (?! computed-options env) (enumerated-options env attribute))
          top-class (mfo/top-class form-instance attribute)
          value (get props qualified-key)
          label (or (form/field-label env attribute) (field/default-field-label qualified-key))]
      (if read-only?
        (let [value (some #(when (= value (:value %)) %) options)]
          (ui-text-field
           {:className top-class
            :error (boolean invalid?)
            :fullWidth true
            :helperText (when invalid?
                          (tr "Required"))
            :InputProps {:readOnly true}
            :key (str qualified-key)
            :label label
            :value (:text value)}))
        (select/ui-wrapped-select
         (merge user-props {:className top-class
                            :error (boolean invalid?)
                            :helperText (when invalid?
                                          (tr "Required"))
                            :label label
                            :onChange #(form/input-changed! env qualified-key %)
                            :options options
                            :react-key (str qualified-key)
                            :required (boolean required?)
                            :value value}))))))

(defn render-field
  [env {::attr/keys [cardinality] :as attribute}]
  (if (= :many cardinality)
    (render-to-many env attribute)
    (render-to-one env attribute)))
