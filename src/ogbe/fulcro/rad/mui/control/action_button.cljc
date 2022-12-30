(ns ogbe.fulcro.rad.mui.control.action-button
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [com.fulcrologic.rad.control :as control]
   [ogbe.fulcro.mui.inputs.button :refer [ui-button]]
   [ogbe.fulcro.mui.inputs.icon-button :refer [ui-icon-button]]
   [ogbe.fulcro.rad.mui-options :as mo]))

(defsc ActionButton [_this {:keys [control-key instance]}]
  {:shouldComponentUpdate (fn [_ _ _] true)}
  (let [controls (control/component-controls instance)
        render (mo/get-rendering-options instance mo/action-button-render)
        props (comp/props instance)
        {:keys [action class disabled? htmlStyle icon label visible?]
         :as control} (get controls control-key)]
    (when control
      (let [label (?! label instance)
            class (?! class instance)
            loading? (df/loading? (get-in props [df/marker-table (comp/get-ident instance)]))
            disabled? (or loading? (?! disabled? instance))
            visible? (or (nil? visible?) (?! visible? instance))
            onClick #(when action (action instance control-key))
            props {:className class
                   :disabled (boolean disabled?)
                   :key (str control-key)
                   :onClick onClick
                   :size "small"
                   :style htmlStyle
                   :variant "contained"}]
        (when visible?
          (or
           (?! render instance (assoc control
                                      :key control-key
                                      :label label
                                      :class class
                                      :onClick onClick
                                      :disabled? disabled?
                                      :loading? loading?))
           (if label
             (ui-button (assoc props :startIcon icon) label)
             (ui-icon-button props icon))))))))

(def render-control (comp/factory ActionButton {:keyfn :control-key}))
