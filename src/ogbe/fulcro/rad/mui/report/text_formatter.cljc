(ns ogbe.fulcro.rad.mui.report.text-formatter
  (:require
   [ogbe.fulcro.mui.data-display.typography :refer [ui-typography]]))

(defn text-formatter
  [_report-instance value]
  (ui-typography {} value))
