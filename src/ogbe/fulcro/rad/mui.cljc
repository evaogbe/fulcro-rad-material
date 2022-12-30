(ns ogbe.fulcro.rad.mui
  (:require
   [com.fulcrologic.rad.blob :as blob]
   [com.fulcrologic.rad.container :as container]
   [com.fulcrologic.rad.control :as control]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.report :as report]
   [ogbe.fulcro.rad.mui.container :as mat-container]
   [ogbe.fulcro.rad.mui.control.action-button :as action-button]
   [ogbe.fulcro.rad.mui.control.boolean-control :as boolean-control]
   [ogbe.fulcro.rad.mui.control.instant-control :as instant-control]
   [ogbe.fulcro.rad.mui.control.picker-control :as picker-control]
   [ogbe.fulcro.rad.mui.control.text-control :as text-control]
   [ogbe.fulcro.rad.mui.form :as mat-form]
   [ogbe.fulcro.rad.mui.form.autocomplete :as autocomplete]
   [ogbe.fulcro.rad.mui.form.blob-field :as blob-field]
   [ogbe.fulcro.rad.mui.form.boolean-field :as boolean-field]
   [ogbe.fulcro.rad.mui.form.currency-field :as currency-field]
   [ogbe.fulcro.rad.mui.form.decimal-field :as decimal-field]
   [ogbe.fulcro.rad.mui.form.double-field :as double-field]
   [ogbe.fulcro.rad.mui.form.entity-picker :as entity-picker]
   [ogbe.fulcro.rad.mui.form.enumerated-field :as enumerated-field]
   [ogbe.fulcro.rad.mui.form.instant-field :as instant-field]
   [ogbe.fulcro.rad.mui.form.int-field :as int-field]
   [ogbe.fulcro.rad.mui.form.text-field :as text-field]
   [ogbe.fulcro.rad.mui.report :as mat-report]
   [ogbe.fulcro.rad.mui.report.instant-formatter :as instant-formatter]
   [ogbe.fulcro.rad.mui.report.text-formatter :as text-formatter]))

(def all-controls
  {::container/style->layout {:default mat-container/render-container-layout}

   ::control/type->style->control
   {:boolean {:default boolean-control/render-control
              :toggle  boolean-control/render-control}
    :button {:default action-button/render-control}
    :instant {:date-at-noon instant-control/date-at-noon-control
              :default instant-control/date-time-control
              :ending-date instant-control/midnight-next-date-control
              :starting-date instant-control/midnight-on-date-control}
    :picker {:default picker-control/render-control}
    :string {:default text-control/render-control
             :search text-control/render-control}}

   ::form/element->style->layout
   {:form-body-container {:default mat-form/standard-form-layout-renderer}
    :form-container {:default mat-form/standard-form-container
                     :file-as-icon mat-form/file-icon-renderer}
    :ref-container {:default mat-form/standard-ref-container
                    :file mat-form/file-ref-container}}

   ::form/type->style->control
   {:boolean {:default boolean-field/render-field}
    :decimal {:default decimal-field/render-field
              :USD currency-field/render-field}
    :double {:default double-field/render-field}
    :enum {:autocomplete autocomplete/render-autocomplete-field
           :default enumerated-field/render-field}
    :int {:default int-field/render-field
          :picker enumerated-field/render-field}
    :instant {:date-at-noon instant-field/render-date-at-noon-field
              :default instant-field/render-field
              :ending-date instant-field/render-midnight-next-date-field
              :picker enumerated-field/render-field
              :starting-date instant-field/render-midnight-on-date-field}
    :keyword {:default enumerated-field/render-field}
    :long {:default int-field/render-field
           :picker enumerated-field/render-field}
    :string {::blob/file-upload blob-field/render-file-upload
             :autocomplete autocomplete/render-autocomplete-field
             :counter text-field/render-counter
             :default text-field/render-field
             :email text-field/render-email
             :multi-line text-field/render-multi-line
             :password text-field/render-password
             :picker enumerated-field/render-field
             :sorted-set text-field/render-dropdown
             :url text-field/render-url
             :viewable-password text-field/render-viewable-password}
    :ref {:pick-many entity-picker/to-many-picker
          :pick-one entity-picker/to-one-picker}
    :text {:default text-field/render-field}}

   ::report/control-style->control {:default mat-report/render-standard-controls}
   ::report/row-style->row-layout {:default mat-report/render-table-row
                                   :list mat-report/render-list-row}
   ::report/style->layout {:default mat-report/render-table-report-layout
                           :list mat-report/render-list-report-layout}

   ::report/type->style->formatter
   {:instant {:date instant-formatter/date-formatter
              :default instant-formatter/instant-formatter
              :month-day instant-formatter/month-day-formatter
              :short-timestamp instant-formatter/short-timestamp-formatter
              :time instant-formatter/time-formatter
              :timestamp instant-formatter/timestamp-formatter}
    :string {:default text-formatter/text-formatter
             :multi-line text-formatter/text-formatter}}})
