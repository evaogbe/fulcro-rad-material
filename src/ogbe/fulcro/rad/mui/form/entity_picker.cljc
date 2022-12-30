(ns ogbe.fulcro.rad.mui.form.entity-picker
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro-i18n.i18n :refer [tr]]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [com.fulcrologic.rad.picker-options :as picker-options]
   [ogbe.fulcro.mui.inputs.form-control :refer [ui-form-control]]
   [ogbe.fulcro.mui.inputs.form-control-label :refer [ui-form-control-label]]
   [ogbe.fulcro.mui.inputs.form-group :refer [ui-form-group]]
   [ogbe.fulcro.mui.inputs.form-label :refer [ui-form-label]]
   [ogbe.fulcro.mui.inputs.switch :refer [ui-switch]]
   [ogbe.fulcro.mui.inputs.text-field :refer [ui-text-field]]
   [taoensso.timbre :as log]
   [ogbe.fulcro.rad.mui.components.select :as select]
   [ogbe.fulcro.rad.mui.form.field :as field]
   [ogbe.fulcro.rad.mui.form-options :as mfo]))

(defsc ToOnePicker [_this {:keys [env attr]}]
  {:componentDidMount (fn [this]
                        (let [{:keys [env attr]} (comp/props this)
                              form-instance (::form/form-instance env)
                              props (comp/props form-instance)
                              form-class (comp/react-type form-instance)]
                          (picker-options/load-options! form-instance form-class props attr)))}
  (let [{::form/keys [master-form form-instance]} env
        visible? (form/field-visible? form-instance attr)]
    (when visible?
      (let [{::form/keys [attributes field-options]} (comp/component-options form-instance)
            {::attr/keys [qualified-key required?]} attr
            field-options (get field-options qualified-key)
            target-id-key (some (fn [{k ::attr/qualified-key ::attr/keys [target]}]
                                  (when (= k qualified-key) target))
                                attributes)
            {::picker-options/keys [cache-key query-key]} (merge attr field-options)
            cache-key (or (?! cache-key (comp/react-type form-instance) (comp/props form-instance))
                          query-key
                          (log/error "Ref field MUST have either a ::picker-options/cache-key or ::picker-options/query-key in attribute "
                                     qualified-key))
            props (comp/props form-instance)
            options (get-in props [::picker-options/options-cache cache-key :options])
            value [target-id-key (get-in props [qualified-key target-id-key])]
            field-label (or (form/field-label env attr) (field/default-field-label qualified-key))
            read-only? (or (form/read-only? master-form attr) (form/read-only? form-instance attr))
            invalid? (and (not read-only?) (form/invalid-attribute-value? env attr))
            extra-props (?! (form/field-style-config env attr :input/props) env)
            top-class (mfo/top-class form-instance attr)
            onSelect #(form/input-changed! env qualified-key %)]
        (if read-only?
          (let [value (->> options (some #(when (= value (:value %)) %)) :text)]
            (ui-text-field
             {:className top-class
              :defaultValue value
              :error (boolean invalid?)
              :helperText (when invalid? (tr "Required"))
              :InputProps {:readOnly true}
              :required (boolean required?)}))
          (select/ui-wrapped-select
           (assoc extra-props
                  :className top-class
                  :error (boolean invalid?)
                  :helperText (when invalid? (tr "Required"))
                  :label field-label
                  :onChange onSelect
                  :options options
                  :required (boolean required?)
                  :value value)))))))

(let [ui-to-one-picker (comp/factory ToOnePicker
                                     {:keyfn (fn [{:keys [attr]}] (::attr/qualified-key attr))})]
  (defn to-one-picker [env attribute]
    (ui-to-one-picker {:attr attribute
                       :env env})))

(defsc ToManyPicker [_this {:keys [env attr]}]
  {:componentDidMount (fn [this]
                        (let [{:keys [env attr]} (comp/props this)
                              form-instance (::form/form-instance env)
                              props (comp/props form-instance)
                              form-class (comp/react-type form-instance)]
                          (picker-options/load-options! form-instance form-class props attr)))}
  (let [{::form/keys [form-instance]} env
        visible? (form/field-visible? form-instance attr)]
    (when visible?
      (let [{::form/keys [attributes field-options]} (comp/component-options form-instance)
            {attr-field-options ::form/field-options
             ::attr/keys [qualified-key]} attr
            field-options (get field-options qualified-key)
            extra-props (?! (form/field-style-config env attr :input/props) env)
            target-id-key (first (keep (fn [{k ::attr/qualified-key ::attr/keys [target]}]
                                         (when (= k qualified-key) target)) attributes))
            {:keys [style]
             ::picker-options/keys [cache-key query-key]} (merge attr-field-options field-options)
            cache-key (or (?! cache-key (comp/react-type form-instance) (comp/props form-instance))
                          query-key
                          (log/error "Ref field MUST have either a ::picker-options/cache-key or ::picker-options/query-key in attribute "
                                     qualified-key))
            props (comp/props form-instance)
            options (get-in props [::picker-options/options-cache cache-key :options])
            current-selection (into #{}
                                    (keep (fn [entity]
                                            (when-let [id (get entity target-id-key)]
                                              [target-id-key id])))
                                    (get props qualified-key))
            field-label (or (form/field-label env attr) (field/default-field-label qualified-key))
            invalid? (form/invalid-attribute-value? env attr)
            read-only? (form/read-only? form-instance attr)
            top-class (mfo/top-class form-instance attr)
            validation-message (when invalid? (form/validation-error-message env attr))]
        (if (= style :dropdown)
          (select/ui-wrapped-select
           (assoc extra-props
                  :className top-class
                  :disabled read-only?
                  :error (boolean invalid?)
                  :helperText (when invalid? validation-message)
                  :label field-label
                  :multiple true
                  :onChange #(form/input-changed! env qualified-key %)
                  :options options
                  :value current-selection))
          (ui-form-control
           {:component "fieldset"
            :variant "standard"}
           (ui-form-label {:component "legend"} field-label)
           (ui-form-group
            {}
            (mapv
             (fn [{:keys [text value]}]
               (let [checked? (contains? current-selection value)]
                 (ui-form-control-label
                  {:control (ui-switch
                             (assoc extra-props
                                    :checked checked?
                                    :onChange (fn []
                                                (form/input-changed!
                                                 env
                                                 qualified-key
                                                 (vec (if checked?
                                                        (disj current-selection value)
                                                        (conj current-selection value)))))))
                   :key value
                   :label text})))
             options))))))))

(let [ui-to-many-picker (comp/factory ToManyPicker
                                      {:keyfn (fn [{:keys [attr]}] (::attr/qualified-key attr))})]
  (defn to-many-picker [env attribute]
    (ui-to-many-picker {:attr attribute
                        :env env})))
