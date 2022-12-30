(ns ogbe.fulcro.rad.mui.control.instant-control
  (:require
   [cljc.java-time.local-date-time :as ldt]
   [cljc.java-time.local-time :as lt]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.rad.control :as control]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [com.fulcrologic.rad.type-support.date-time :as dt]
   [ogbe.fulcro.mui.date-pickers.date-picker :refer [ui-date-picker]]
   [ogbe.fulcro.mui.date-pickers.date-time-picker :refer [ui-date-time-picker]]
   [ogbe.fulcro.mui.inputs.text-field :refer [ui-text-field]]
   [taoensso.timbre :as log]))

(defsc InstantControl [_this {:keys [control control-key picker-factory instance]}]
  {:shouldComponentUpdate (fn [_ _ _] true)}
  (let [controls (control/component-controls instance)
        {:keys [action disabled? label onChange user-props visible?]
         :as control} (get controls control-key control)]
    (if (and control picker-factory)
      (let [label (?! label instance)
            disabled? (?! disabled? instance)
            visible? (or (nil? visible?) (?! visible? instance))
            value (control/current-value instance control-key)
            onChange (fn [new-value]
                       (control/set-parameter! instance control-key new-value)
                       (when onChange
                         (onChange instance new-value))
                       (when action
                         (action instance)))
            InputProps (cond-> (:InputProps user-props)
                         disabled? (assoc :readOnly true))
            TextFieldProps (assoc (:TextFieldProps user-props)
                                  :InputProps InputProps
                                  :inputProps (:inputProps user-props))
            render-input (fn [props]
                           (let [props #?(:clj  props
                                          :cljs (js->clj props))]
                             (ui-text-field (merge TextFieldProps props))))]
        (when visible?
          (picker-factory (-> user-props
                              (dissoc :InputProps :inputProps :TextFieldProps)
                              (assoc
                               :key (str control-key)
                               :label label
                               :onChange onChange
                               :renderInput render-input
                               :value value)))))
      (log/error "Cannot render control. Missing picker factory or control definition."))))

(def ui-instant-control (comp/factory InstantControl {:keyfn :control-key}))

(defn mk-date-picker
  [default-local-time]
  (fn [{:keys [onChange] :as props}]
    (ui-date-picker
     (assoc props :onChange (fn [value]
                              (when onChange
                                (let [date (dt/inst->local-date value)
                                      dt (ldt/of date default-local-time)
                                      inst (dt/local-datetime->inst dt)]
                                  (onChange inst))))))))

(defn ui-ending-date-instant-picker
  [{:keys [onChange] :as props}]
  (ui-date-picker
   (assoc props :onChange (fn [value]
                            (when onChange
                              (onChange (dt/end-of-day value)))))))

(defn date-time-control
  [render-env]
  (ui-instant-control (assoc render-env :picker-factory ui-date-time-picker)))

(defn midnight-on-date-control
  [render-env]
  (ui-instant-control (assoc render-env :picker-factory (mk-date-picker lt/midnight))))

(defn midnight-next-date-control
  [render-env]
  (ui-instant-control (assoc render-env :picker-factory ui-ending-date-instant-picker)))

(defn date-at-noon-control
  [render-env]
  (ui-instant-control (assoc render-env :picker-factory (mk-date-picker lt/noon))))
