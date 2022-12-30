(ns ogbe.fulcro.rad.mui.report.instant-formatter
  (:require
   [com.fulcrologic.rad.type-support.date-time :as dt]
   [ogbe.fulcro.mui.data-display.typography :refer [ui-typography]]))

(defn instant-formatter
  [_report-instance value]
  (ui-typography
   {:component "time"
    :dateTime (dt/inst->html-date value)}
   (dt/inst->human-readable-date value)))

(defn mk-instant-formatter
  [->date-time format]
  (fn [_report-instance value]
    (ui-typography
     {:component "time"
      :dateTime (->date-time value)}
     (dt/tformat format value))))

(def short-timestamp-formatter (mk-instant-formatter dt/inst->html-datetime-string "MMM d, h:mma"))
(def timestamp-formatter (mk-instant-formatter dt/inst->html-datetime-string "MMM d, yyyy h:mma"))
(def date-formatter (mk-instant-formatter dt/inst->html-date "MMM d, yyyy"))
(def month-day-formatter (mk-instant-formatter #(dt/tformat "M-d" %) "MMM d"))
(def time-formatter (mk-instant-formatter #(dt/tformat "H:mm" %) "h:mma"))
