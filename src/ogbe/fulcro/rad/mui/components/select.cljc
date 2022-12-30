(ns ogbe.fulcro.rad.mui.components.select
  (:require
   [com.fulcrologic.fulcro.algorithms.transit :as ftransit]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   #_{:clj-kondo/ignore #?(:clj [:unused-namespace] :cljs [])}
   [com.fulcrologic.fulcro.dom.events :as evt]
   [edn-query-language.core :as eql]
   [ogbe.fulcro.mui.navigation.menu-item :refer [ui-menu-item]]
   #_{:clj-kondo/ignore #?(:clj [] :cljs [:unused-namespace :unused-referred-var])}
   [ogbe.fulcro.mui.inputs.select :refer [ui-select]]
   #_{:clj-kondo/ignore #?(:clj [:unused-namespace :unused-referred-var] :cljs [])}
   [ogbe.fulcro.mui.inputs.text-field :refer [ui-text-field]]
   #_{:clj-kondo/ignore #?(:clj [:unused-namespace] :cljs [])}
   [taoensso.timbre :as log]))

(defn user-format->mui-format
  [{:keys [multiple]} value]
  (if multiple
    (if value
      (map #(ftransit/transit-clj->str % {:metadata? false}) value)
      [])
    (cond
      (= value "") ""
      (and (eql/ident? value) (nil? (second value))) ""
      (or value (boolean? value)) (ftransit/transit-clj->str value {:metadata? false})
      :else "")))

#_{:clj-kondo/ignore #?(:clj [:unused-binding] :cljs [])}
(defsc WrappedSelect [this {:keys [multiple onChange value] :as props}]
  {:initLocalState
   (fn [_this]
     #?(:cljs (let [xform-options (memoize
                                   (fn [options]
                                     (map
                                      (fn [option]
                                        (update option :value
                                                ftransit/transit-clj->str {:metadata? false}))
                                      options)))
                    xform-value (fn [multiple? value]
                                  (user-format->mui-format {:multiple multiple?} value))]
                {:get-options (fn [props] (xform-options (:options props)))
                 :format-value (fn [props value] (xform-value (:multiple props) value))})))}
  #?(:clj
     (ui-select
      {}
      (ui-menu-item {:value ""} ""))

     :cljs
     (let [{:keys [get-options format-value]} (comp/get-state this)
           userOnChange onChange
           options (get-options props)
           value (format-value props value)
           props (merge-with
                  merge
                  (dissoc props :value)
                  {:fullWidth true
                   :select true

                   :SelectProps
                   {:multiple (boolean multiple)
                    :onChange (fn [evt]
                                (try
                                  (let [string-value (evt/target-value evt)
                                        value  (if multiple
                                                 (mapv #(when (seq %)
                                                          (ftransit/transit-str->clj %))
                                                       string-value)
                                                 (when (seq string-value)
                                                   (ftransit/transit-str->clj string-value)))]
                                    (when userOnChange
                                      (userOnChange value)))
                                  (catch :default e
                                    (log/error e "Unable to read dropdown value" evt))))
                    :value value}})]
       (ui-text-field
        props
        (mapv (fn [{:keys [text value]}]
                (ui-menu-item
                 {:key (str value)
                  :value value}
                 text))
              options)))))

(def ui-wrapped-select (comp/factory WrappedSelect))
