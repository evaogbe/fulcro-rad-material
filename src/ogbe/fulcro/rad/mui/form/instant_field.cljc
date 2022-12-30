(ns ogbe.fulcro.rad.mui.form.instant-field
  (:require
   [cljc.java-time.local-date-time :as ldt]
   [cljc.java-time.local-time :as lt]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.type-support.date-time :as dt]
   [ogbe.fulcro.mui.date-pickers.date-picker :refer [ui-date-picker]]
   [ogbe.fulcro.mui.date-pickers.date-time-picker :refer [ui-date-time-picker]]
   [ogbe.fulcro.mui.inputs.text-field :refer [ui-text-field]]
   [ogbe.fulcro.rad.mui.form.field :as field]
   [ogbe.fulcro.rad.mui.form-options :as mfo]))

#_{:clj-kondo/ignore #?(:clj [:unused-binding] :cljs [])}
(defn valid-date?
  [inst]
  #?(:clj  true
     :cljs (not (js/isNaN inst))))

(defn render-field-factory
  [ui-picker format-value]
  (fn [{::form/keys [form-instance] :as env} {::attr/keys [qualified-key required?] :as attribute}]
    (form/with-field-context
      [{:keys [field-label
               field-style-config
               invalid?
               read-only?
               validation-message
               value
               visible?]} (form/field-context env attribute)
       {:keys [InputProps inputProps TextFieldProps]} field-style-config
       InputProps (cond-> InputProps
                    read-only? (assoc :readOnly true))
       field-style-config (dissoc field-style-config :InputProps :inputProps :TextFieldProps)
       TextFieldProps (assoc TextFieldProps
                             :error (boolean invalid?)
                             :helperText (when invalid? validation-message)
                             :InputProps InputProps
                             :inputProps inputProps
                             :required (boolean required?))
       label (or field-label (field/default-field-label qualified-key))
       top-class (mfo/top-class form-instance attribute)
       render-input (fn [props]
                      (let [props #?(:clj  props
                                     :cljs (js->clj props))]
                        (ui-text-field (merge-with merge TextFieldProps props))))]
      (when visible?
        (ui-picker
         (assoc field-style-config
                :className top-class
                :key (str qualified-key)
                :label label
                :onBlur #(form/input-blur! env qualified-key %)
                :onChange (fn [v]
                            (form/input-changed! env qualified-key (when (valid-date? v)
                                                                     (format-value v))))
                :renderInput render-input
                :value value))))))

(def render-field
  (render-field-factory ui-date-time-picker identity))

(defn mk-value-formatter
  [local-time]
  (fn [inst]
    (when inst
      (let [dt (dt/inst->local-datetime inst)
            year (ldt/get-year dt)]
        (when (-> year (> 1000))
          (dt/local-datetime->inst (ldt/with dt local-time)))))))

(def render-midnight-on-date-field
  (render-field-factory ui-date-picker (mk-value-formatter lt/midnight)))

(def render-midnight-next-date-field
  (render-field-factory ui-date-picker (mk-value-formatter lt/max)))

(def render-date-at-noon-field
  (render-field-factory ui-date-picker (mk-value-formatter lt/noon)))
