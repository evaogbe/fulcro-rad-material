(ns ogbe.fulcro.rad.mui.report-options
  "Documented option keys for setting rendering-specific customization options for reports when
   using Material UI Plugin as your DOM renderer.

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
   [com.fulcrologic.rad.attributes :as attr]
   [ogbe.fulcro.rad.mui-options :as mo]))

(def action-button-grouping
  "CSS class(es) to put in the div that surrounds the action buttons.
   
   A string or `(fn [report-instance] string?)`."
  :ogbe.fulcro.rad.mui.report/action-button-grouping)

(def column-width
  "Attribute option. A string that defines the CSS width of the column in a table-style report."
  :ogbe.fulcro.rad.mui.report/column-width)

(def column-widths
  "A map from qualified key to CSS widths of the columns in a table-style report. OVERRIDES the
   `column-width` option."
  :ogbe.fulcro.rad.mui.report/column-widths)

(def controls-class
  "The CSS class of the control section of the report.

   A string or `(fn [instance] string?)`."
  :ogbe.fulcro.rad.mui.report/controls-class)

(def controls-cell-class
  "A function that returns the CSS class for the given cell of the report controls input form.
   
   A string or `(fn [report-instance zero-based-row-idx] string?)`.
   
   The `zero-based-row-idx` is the row being rendered."
  :ogbe.fulcro.rad.mui.report/controls-cell-class)

(def rotated-table-class
  "The CSS class of generated report tables that are rotated.

  A string or `(fn [report-instance] string?)`."
  :ogbe.fulcro.rad.mui.report/rotated-table-class)

(def row-button-grouping
  "CSS class(es) to put in the div that surrounds the action buttons on a table row.
   
   A string or `(fn [report-instance] string?)`."
  :ogbe.fulcro.rad.mui.report/row-button-grouping)

(def row-button-renderer
  "Overrides the rendering of action button controls.
   
   A `(fn [instance row-props {:keys [key disabled?]}] dom-element)`.

   * `instance` - The report instance
   * `row-props` - The data props of the row
   * `key` - A unique key that can be used for react on the element
   * `onClick` - A generated function according to the buton's action setting
   * `disabled?`- `true` if the calculation of your `disabled?` option is true
   
   You must return a DOM element to render for the control. If you return `nil` then the default
   (button) will be rendered."
  :ogbe.fulcro.rad.mui.report/row-button-renderer)

(def table-class
  "The CSS class of generated report tables.

  A string or `(fn [report-instance] string?)`."
  :ogbe.fulcro.rad.mui.report/table-class)

(def table-cell-class
  "The CSS class of cells in a table-style report.

   A `(fn [report-instance zero-based-column-index] string?)`.
    
   NOTE: Action buttons are added and have a column index."
  :ogbe.fulcro.rad.mui.report/table-cell-class)

(def table-header-class
  "The CSS class of headers in a table-style report.

   A `(fn [report-instance zero-based-column-index] string?)`.

   NOTE: Action buttons are added and have a column index."
  :ogbe.fulcro.rad.mui.report/table-header-class)

(def selectable-table-rows?
  "A boolean. When `true` the table will support click on a row to affix a highlight to that row."
  :ogbe.fulcro.rad.mui.report/selectable-table-rows?)

(defn get-column-width
  "Looks for the column with on the given attribute or table-style report instance. See
   `column-widths` and `column-width`."
  [report-instance {::attr/keys [qualified-key] :as attr}]
  (or
   (mo/get-rendering-options report-instance column-widths qualified-key)
   (get attr column-width)))
