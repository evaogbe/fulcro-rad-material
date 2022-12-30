(ns ogbe.fulcro.rad.mui.form.field
  (:require
   [clojure.string :as str]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.form :as form]
   [ogbe.fulcro.mui.inputs.text-field :refer [ui-text-field]]
   [ogbe.fulcro.rad.mui.form-options :as mfo]))

(defn default-field-label
  [qualified-key]
  (some-> qualified-key name (str/replace #"-" " ") str/capitalize))

(defn render-field-factory
  ([conversions] (render-field-factory conversions {}))
  ([{:keys [evt->model model->str] :or {model->str str}} addl-props]
   (fn [{::form/keys [form-instance] :as env} {::attr/keys [qualified-key required?] :as attribute}]
     (form/with-field-context
       [{:keys [field-label
                field-style-config
                invalid?
                read-only?
                validation-message
                value
                visible?]} (form/field-context env attribute)
        InputProps (cond-> (:InputProps field-style-config)
                     read-only? (assoc :readOnly true))
        label (or field-label (default-field-label qualified-key))
        addl-props (merge field-style-config addl-props)
        top-class (mfo/top-class form-instance attribute)]
       (when visible?
         (ui-text-field
          (assoc addl-props
                 :className top-class
                 :error (boolean invalid?)
                 :fullWidth true
                 :helperText (when invalid? validation-message)
                 :InputProps InputProps
                 :key (str qualified-key)
                 :label label
                 :onBlur (fn [evt]
                           (when evt->model
                             (form/input-blur! env qualified-key (evt->model evt))))
                 :onChange (fn [evt]
                             (when evt->model
                               (form/input-changed! env qualified-key (evt->model evt))))
                 :required (boolean required?)
                 :value (if value (model->str value) ""))))))))
