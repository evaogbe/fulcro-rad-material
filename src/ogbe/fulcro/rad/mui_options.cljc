(ns ogbe.fulcro.rad.mui-options
  "Documented option keys for setting rendering-specific customization options when using Material
   UI Plugin as your DOM renderer.

  ALL options MUST appear under the rendering options key:

  ```
  (ns ...
    (:require
       [ogbe.fulcro.rad.mui-options :as mo]
       ...))

  (defsc-report Report [this props]
    {mo/rendering-options { ... }})
  ```

  Most of the options in this file can be given a global default using

  ```
  (set-global-rendering-options! fulcro-app options)
  ```

  where the `options` is a map of option keys/values."
  (:require
   [com.fulcrologic.fulcro.components :as comp]))

(def rendering-options
  "Top-level key for specifying rendering options. All
   SUI customization options MUST appear under this key."
  :ogbe.fulcro.rad.mui/rendering-options)

(def action-button-render
  "Overrides the rendering of action button controls.
   
   A `(fn [instance {:keys [key control disabled? loading?]}] dom-element)`.

   * `key` - The key you used to add it to the controls list
   * `control` - The map of options you gave for the control
   * `disabled?`-  `true` if the calculation of your `disabled?` option is `true`
   * `loading?` - `true` if the component is loading data
   
   You must return a DOM element to render for the control. If you return nil then
   the default (button) will be rendered."
  :ogbe.fulcro.rad.mui/action-button-render)

(def body-class
  "The CSS class of the div that holds the actual body of the page (e.g. form or report).

   A string or `(fn [instance] string?)`."
  :ogbe.fulcro.rad.mui/body-class)

(def controls-class
  "The CSS class of the div that holds the controls on layouts that have a control section.

   A string or `(fn [instance] string?)`."
  :ogbe.fulcro.rad.mui/controls-class)

(def layout-class
  "The CSS class of the div that holds the top-level layout of the report or form.

   A string or `(fn [instance] string?)`."
  :ogbe.fulcro.rad.mui/layout-class)

(defn get-rendering-options
  ([c & ks]
   (let [app (comp/any->app c)
         global-options (some-> app
                                :com.fulcrologic.fulcro.application/runtime-atom
                                deref
                                :ogbe.fulcro.rad.mui/rendering-options)
         options (merge
                  global-options
                  (comp/component-options c :ogbe.fulcro.rad.mui/rendering-options))]
     (if (seq ks)
       (get-in options ks)
       options))))

(defn set-global-rendering-options!
  [app options]
  (swap! (:com.fulcrologic.fulcro.application/runtime-atom app)
         assoc
         :ogbe.fulcro.rad.mui/rendering-options
         options))
