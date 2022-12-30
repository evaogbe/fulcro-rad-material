(ns ogbe.fulcro.rad.mui.control.text-control
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom.events :as evt]
   [com.fulcrologic.rad.control :as control]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [ogbe.fulcro.mui.inputs.input-adornment :refer [ui-input-adornment]]
   [ogbe.fulcro.mui.inputs.text-field :refer [ui-text-field]]))

(defn- internal-store-name [control-key]
  (keyword (str "ogbe.fulcro.rad.mui.control.text-control_" (namespace control-key))
           (name control-key)))

(defsc TextControl [_this {:keys [control-key instance]}]
  {:shouldComponentUpdate (fn [_ _ _] true)}
  (let [controls (control/component-controls instance)
        {:keys [disabled? icon label onChange placeholder style user-props visible?]
         :as control} (get controls control-key)]
    (when control
      (let [label (?! label instance)
            disabled? (?! disabled? instance)
            placeholder (?! placeholder)
            visible? (or (nil? visible?) (?! visible? instance))
            value (control/current-value instance control-key)
            {:keys [last-sent-value]} (control/current-value instance
                                                             (internal-store-name control-key))
            chg! #(control/set-parameter! instance control-key (evt/target-value %))
            run! (fn [run-if-unchanged? evt]
                   (let [v (evt/target-value evt)
                         actually-changed? (not= v last-sent-value)]
                     (when (and onChange (or run-if-unchanged? actually-changed?))
                       (control/set-parameter! instance control-key v)
                       (control/set-parameter! instance
                                               (internal-store-name control-key)
                                               {:last-sent-value v})
                       ;; Change the URL parameter
                       (onChange instance v))))
            InputProps (cond-> (:InputProps user-props)
                         icon (assoc :startAdornment
                                     (ui-input-adornment {:position "start"} icon))
                         disabled? (assoc :readOnly true))]
        (when visible?
          (ui-text-field
           (assoc user-props
                  :InputProps InputProps
                  :key (str control-key)
                  :label label
                  :onBlur #(run! false %)
                  :onChange chg!
                  :onKeyDown (fn [evt] (when (evt/enter? evt) (run! true evt)))
                  :placeholder placeholder
                  :type (if (= style :search) "search" "text")
                  :value (or value ""))))))))

(def render-control (comp/factory TextControl {:keyfn :control-key}))
