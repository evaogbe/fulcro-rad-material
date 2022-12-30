(ns ogbe.fulcro.rad.mui.form-options
  "Documented option keys for setting rendering-specific customization options for forms when using
   Material UI Plugin as your DOM renderer.

  ALL options MUST appear under the rendering options key:

  ```
  (ns ...
    (:require
       [ogbe.fulcro.rad.mui-options :as mo]
       ...))

  (defsc-form Form [this props]
    {mo/rendering-options { ... }})
  ```

  Most of the options in this file can be given a global default using

  ```
  (set-global-rendering-options! fulcro-app options)
  ```

  where the `options` is a map of option keys/values."
  (:require
   [com.fulcrologic.rad.attributes :as attr]
   [ogbe.fulcro.rad.mui-options :as mo]))

(def add-position
  :ogbe.fulcro.rad.mui.form/add-position)

(def controls-class
  "The CSS class of the control section of the top-level form.

   A string or `(fn [form-env] string?)`."
  :ogbe.fulcro.rad.mui.form/controls-class)

(def controls-position
  :ogbe.fulcro.rad.mui.form/controls-position)

(def element-class
  "Attribute option. A string that defines a CSS class for the outermost DOM element of a form field
   that renders this attribute. OVERRIDES the class name of the element containing the form field."
  :ogbe.fulcro.rad.mui.form/element-class)

(def element-classes
  "A map from qualified key to class names. OVERRIDES the class name of the element containing the
   form field."
  :ogbe.fulcro.rad.mui.form/element-classes)

(def form-class
  "Specifies the CSS class(es) for the div containing the form inputs.
   
   A string or a `(fn [form-env] string?)`."
  :ogbe.fulcro.rad.mui.form/form-class)

(def input-props
  "ALIAS of `fo/input-props`. This option can be placed on `fo/field-style-config(s)`.
   The value can be a map, or a `(fn [form-env] map?)`.

   Many, but not all, MUI input controls support this option.

   See also `fo/input-props`."
  :input/props)

(def ref-container-class
  "This option can be used in the ::fo/subforms entries to indicate what class(es) should be set on
   the element that wraps the list of elements.
   
   A string or `(fn [form-env] string?)`."
  :ogbe.fulcro.rad.mui.form/ref-container-class)

(def ref-element-class
  "This option can be used in a form's component options to indicate the class to set on the
   (generated) element itself when used as a subform.

   A string or a `(fn [form-env] string?)`."
  :ogbe.fulcro.rad.mui.form/ref-element-class)

(def top-level-class
  "Used in a form's component-options. Specifies the class of the overall form container when it is
   the master (top-level) form.
   
   A string or a `(fn [form-env] string?)`."
  :ogbe.fulcro.rad.mui.form/top-level-class)

(defn top-class
  "Looks for the top-level form element class on the given attribute or form instance. See
   `element-classes` and `element-class`."
  [form-instance {::attr/keys [qualified-key] :as attribute}]
  (or
   (mo/get-rendering-options form-instance element-classes qualified-key)
   (get attribute element-class)))
