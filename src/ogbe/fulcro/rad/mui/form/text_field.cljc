(ns ogbe.fulcro.rad.mui.form.text-field
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom.events :as evt]
   [com.fulcrologic.fulcro-i18n.i18n :refer [tr]]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.form-options :as fo]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [ogbe.fulcro.mui.icons.visibility :refer [ui-icon-visibility]]
   [ogbe.fulcro.mui.icons.visibility-off :refer [ui-icon-visibility-off]]
   [ogbe.fulcro.mui.inputs.icon-button :refer [ui-icon-button]]
   [ogbe.fulcro.mui.inputs.input-adornment :refer [ui-input-adornment]]
   [ogbe.fulcro.mui.inputs.text-field :refer [ui-text-field]]
   [ogbe.fulcro.mui.layout.box :refer [ui-box]]
   [taoensso.timbre :as log]
   [ogbe.fulcro.rad.mui.components.select :as select]
   [ogbe.fulcro.rad.mui.form.field :as field]
   [ogbe.fulcro.rad.mui.form-options :as mfo]))

(defn render-dropdown
  [{::form/keys [form-instance] :as env} attribute]
  (let [{k ::attr/qualified-key
         ::attr/keys [required?]} attribute
        values (form/field-style-config env attribute :sorted-set/valid-values)
        input-props (?! (form/field-style-config env attribute :input/props) env)
        options (map (fn [v] {:text v :value v}) values)
        props (comp/props form-instance)
        value (and attribute (get props k))
        invalid? (not (contains? values value))
        validation-message (when invalid? (form/validation-error-message env attribute))
        field-label (or (form/field-label env attribute) (field/default-field-label k))
        top-class (mfo/top-class form-instance attribute)
        read-only? (form/read-only? form-instance attribute)]
    (select/ui-wrapped-select
     (merge {:className top-class
             :disabled read-only?
             :error (boolean invalid?)
             :helperText (when invalid? validation-message)
             :label field-label
             :onChange #(form/input-changed! env k %)
             :options options
             :react-key (str k)
             :required (boolean required?)
             :value value}
            input-props))))

(defn- render-text-field-factory
  [type]
  (field/render-field-factory {:evt->model evt/target-value}
                              {:type type}))

(def render-field
  (render-text-field-factory "text"))

(def render-email
  (render-text-field-factory "email"))

(def render-multi-line
  (field/render-field-factory {:evt->model evt/target-value}
                              {:multiline true}))

(def render-password
  (render-text-field-factory "password"))

(def render-url
  (render-text-field-factory "url"))

(defn field-option
  [{::form/keys [form-instance] :as env} {::attr/keys [qualified-key] :as attr} k]
  (or (comp/component-options form-instance fo/field-options qualified-key k)
      (form/field-style-config env attr k)))

(defn render-counter
  [{::form/keys [form-instance] :as env} {::attr/keys [qualified-key required?] :as attr}]
  (let [max-length (field-option env attr :counter/max-length)
        warning-length (field-option env attr :counter/warning-length)]
    (if max-length
      (form/with-field-context
        [{:keys [field-label
                 field-style-config
                 invalid?
                 read-only?
                 validation-message
                 value
                 visible?]} (form/field-context env attr)
         InputProps (cond-> (:InputProps field-style-config)
                      read-only? (assoc :readOnly true))
         inputProps (assoc (:inputProps field-style-config) :maxLength max-length)
         label (or field-label (field/default-field-label qualified-key))
         addl-props field-style-config
         top-class (mfo/top-class form-instance attr)
         value (or value "")
         text-length (count value)
         too-long? (-> max-length (< text-length))
         warn? (and warning-length (-> warning-length (< text-length)))
         helper-text (cond
                       too-long? (str text-length " / " max-length)
                       invalid? validation-message
                       warn? (ui-box
                              {:component "span"
                               :sx {:color "warning.main"}}
                              (str text-length " / " max-length))
                       :else (str text-length " / " max-length))]
        (when visible?
          (ui-text-field
           (assoc addl-props
                  :className top-class
                  :error (boolean (or too-long? invalid?))
                  :fullWidth true
                  :helperText helper-text
                  :InputProps InputProps
                  :inputProps inputProps
                  :key (str qualified-key)
                  :label label
                  :multiline true
                  :onBlur #(form/input-blur! env qualified-key (evt/target-value %))
                  :onChange #(form/input-changed! env qualified-key (evt/target-value %))
                  :required (boolean required?)
                  :value value))))
      (log/error
       "Must have :counter/max-length in fo/field-options or ao/field-style-config for attribute"
       qualified-key))))

(defsc ViewablePasswordField [this {:keys [env attr]}]
  {:initLocalState (fn [_this] {:hidden? true})}
  (form/with-field-context
    [{:keys [field-label
             field-style-config
             invalid?
             read-only?
             validation-message
             value
             visible?]} (form/field-context env attr)
     {::form/keys [form-instance]} env
     {::attr/keys [qualified-key required?]} attr
     hidden? (comp/get-state this :hidden?)
     ui-visibility-toggle (ui-input-adornment
                           {:position "end"}
                           (ui-icon-button
                            {:aria-label (tr "Toggle password visibility")
                             :edge "end"
                             :onClick #(comp/update-state! this update :hidden? not)
                             :onMouseDown #(evt/prevent-default! %)}
                            (if hidden?
                              (ui-icon-visibility {})
                              (ui-icon-visibility-off {}))))
     InputProps (-> (:InputProps field-style-config)
                    (assoc :endAdornment ui-visibility-toggle)
                    (cond-> read-only? (assoc :readOnly true)))
     label (or field-label (field/default-field-label qualified-key))
     addl-props field-style-config
     top-class (mfo/top-class form-instance attr)]
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
              :onBlur #(form/input-blur! env qualified-key (evt/target-value %))
              :onChange #(form/input-changed! env qualified-key (evt/target-value %))
              :required (boolean required?)
              :type (if hidden? "password" "text")
              :value (or value ""))))))

(let [ui-viewable-password (comp/factory ViewablePasswordField
                                         {:keyfn (fn [{:keys [attr]}]
                                                   (::attr/qualified-key attr))})]
  (defn render-viewable-password
    [env attribute]
    (ui-viewable-password {:attr attribute
                           :env env})))
