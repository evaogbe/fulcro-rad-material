(ns ogbe.fulcro.rad.mui.control.picker-control
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.rad.control :as control]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [com.fulcrologic.rad.picker-options :as po]
   [ogbe.fulcro.rad.mui.components.select :as select]))

(defsc SimplePicker [_this {:keys [control-key instance]}]
  {:componentDidMount (fn [this]
                        (let [{:keys [control-key instance] :as props} (comp/props this)
                              controls (control/component-controls instance)
                              {::po/keys [query-key] :as picker-options} (get controls control-key)]
                          (when query-key
                            (po/load-picker-options!
                             instance (comp/react-type instance) props picker-options))))
   :shouldComponentUpdate (fn [_ _ _] true)}
  (let [controls (control/component-controls instance)
        {:keys [action disabled? label onChange options placeholder user-props visible?]
         :as control} (get controls control-key)
        options (or options (po/current-picker-options instance control))]
    (when control
      (let [label (?! label instance)
            disabled? (?! disabled? instance)
            placeholder (?! placeholder instance)
            visible? (or (nil? visible?) (?! visible? instance))
            value (control/current-value instance control-key)]
        (when visible?
          (select/ui-wrapped-select
           (merge user-props
                  {:onChange (fn [v]
                               (control/set-parameter! instance control-key v)
                               (binding [rc/*after-render* true]
                                 (when onChange
                                   (onChange instance v))
                                 (when action
                                   (action instance))))
                   :disabled disabled?
                   :label label
                   :options options
                   :placeholder placeholder
                   :value value})))))))

(def render-control (comp/factory SimplePicker {:keyfn :control-key}))
