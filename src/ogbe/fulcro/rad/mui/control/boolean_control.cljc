(ns ogbe.fulcro.rad.mui.control.boolean-control
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.rad.control :as control]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [ogbe.fulcro.mui.inputs.checkbox :refer [ui-checkbox]]
   [ogbe.fulcro.mui.inputs.form-control-label :refer [ui-form-control-label]]
   [ogbe.fulcro.mui.inputs.switch :refer [ui-switch]]
   [taoensso.timbre :as log]))

(defsc BooleanControl [_this {:keys [control control-key instance]}]
  {:shouldComponentUpdate (fn [_ _ _] true)}
  (let [controls (control/component-controls instance)
        {:keys [disabled? label label-top? onChange style toggle? user-props visible?]
         :or {toggle? true} :as control} (or control (get controls control-key))
        toggle? (cond
                  (boolean? toggle?) toggle?
                  (= :toggle style) true
                  :else false)]
    (if control
      (when (or (nil? visible?) (?! visible? instance))
        (let [label (?! label instance)
              disabled? (?! disabled? instance)
              value (control/current-value instance control-key)
              inp-attr (assoc user-props
                              :onChange (fn [_]
                                          (control/set-parameter! instance control-key (not value))
                                          (when onChange
                                            (onChange instance (not value))))
                              :checked (boolean value))]
          (ui-form-control-label
           {:control (if toggle? (ui-switch inp-attr) (ui-checkbox inp-attr))
            :disabled disabled?
            :key (str control-key)
            :label label
            :labelPlacement (if label-top? "top" "end")})))
      (log/error "Could not find control definition for " control-key))))

(def render-control (comp/factory BooleanControl {:keyfn :control-key}))
