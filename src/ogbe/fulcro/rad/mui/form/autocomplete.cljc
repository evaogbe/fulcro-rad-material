(ns ogbe.fulcro.rad.mui.form.autocomplete
  (:require
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.normalized-state :as fns]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.rendering.multiple-roots-renderer :as mroot]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.ids :as ids]
   [com.fulcrologic.rad.options-util :as opts]
   #_{:clj-kondo/ignore #?(:clj [:unused-namespace :unused-referred-var] :cljs [])}
   [ogbe.fulcro.mui.inputs.autocomplete :refer [ui-autocomplete]]
   #_{:clj-kondo/ignore #?(:clj [:unused-namespace :unused-referred-var] :cljs [])}
   [ogbe.fulcro.mui.inputs.text-field :refer [ui-text-field]]
   [taoensso.timbre :as log]
   [ogbe.fulcro.rad.mui.form.field :as field]
   [ogbe.fulcro.rad.mui.form-options :as mfo]
   #?@(:clj  [[com.fulcrologic.fulcro.dom-server :as dom]]
       :cljs [[cljs.reader :refer [read-string]]
              [goog.object :as gobj]])))

(defsc AutocompleteQuery [_this _props]
  {:query [:text :value]})

#_{:clj-kondo/ignore #?(:clj [:unused-binding] :cljs [])}
(defmutation normalize-options [{:keys [source target]}]
  (action [{:keys [state]}]
          #?(:clj (do)
             :cljs
             (let [options (get @state source)
                   normalized-options (map (fn [{:keys [text value]}]
                                             {:label text :value (pr-str value)})
                                           options)]
               (fns/swap!-> state
                            (dissoc source)
                            (assoc-in target normalized-options))))))

(defsc AutocompleteField
  #_{:clj-kondo/ignore #?(:clj [:unused-binding] :cljs [])}
  [this
   {:ui/keys [options]}
   {:keys [className invalid? label onChange read-only? validation-message value]}]
  {:query [::autocomplete-id
           :autocomplete/debounce-ms
           :autocomplete/minimum-input
           :autocomplete/search-key
           :ui/options
           :ui/search-string]
   :ident ::autocomplete-id

   :initLocalState
   (fn [this]
     (let [{:autocomplete/keys [debounce-ms]} (comp/props this)]
       {:load!
        (opts/debounce
         (fn [s]
           (let [{id ::autocomplete-id
                  :autocomplete/keys [search-key]} (comp/props this)]
             (df/load! this search-key AutocompleteQuery
                       {:params {:search-string s}
                        :post-mutation `normalize-options
                        :post-mutation-params {:source search-key
                                               :target [::autocomplete-id id :ui/options]}})))
         (or debounce-ms 200))}))

   :componentDidMount
   (fn [this]
     (let [{id ::autocomplete-id
            :autocomplete/keys [preload? search-key]} (comp/props this)
           value (comp/get-computed this :value)]
       (cond
         preload?
         (df/load! this search-key AutocompleteQuery
                   {:post-mutation `normalize-options
                    :post-mutation-params {:source search-key
                                           :target [::autocomplete-id id :ui/options]}})

         (and search-key value)
         (df/load! this search-key AutocompleteQuery
                   {:params {:only value}
                    :post-mutation `normalize-options
                    :post-mutation-params {:source search-key
                                           :target [::autocomplete-id id :ui/options]}}))))}
  #?(:clj  (dom/div "")
     :cljs (let [load! (comp/get-state this :load!)]
             (if read-only?
               (ui-text-field
                {:className className
                 :error (boolean invalid?)
                 :helperText (when invalid? validation-message)
                 :InputProps {:readOnly true}
                 :label label
                 :defaultValue (get-in options [0 :label])})
               (ui-autocomplete
                {:autoSelect true
                 :filterOptions identity
                 :getOptionLabel (fn [v]
                                   (or (some #(when (= (:value %) v) (:label %)) options) ""))
                 :onChange #(onChange (some-> %2 read-string))
                 :onInputChange #(when (= %3 "input") (load! %2))
                 :openOnFocus true
                 :options (mapv :value options)
                 :renderInput (fn [params]
                                (gobj/set params "className" className)
                                (gobj/set params "error" (boolean invalid?))
                                (gobj/set params "helperText" (when invalid? validation-message))
                                (gobj/set params "label" label)
                                (ui-text-field params))
                 :value (some-> value pr-str)})))))

(def ui-autocomplete-field (comp/computed-factory AutocompleteField {:keyfn ::autocomplete-id}))

(defmutation gc-autocomplete [{:keys [id]}]
  (action [{:keys [state]}]
          (when id
            (swap! state fns/remove-entity [::autocomplete-id id]))))

(defsc AutocompleteFieldRoot [this props {:keys [env attribute]}]
  {:query [::autocomplete-id]
   :initial-state {::autocomplete-id {}}
   :initLocalState (fn [_this] {:field-id (ids/new-uuid)})
   :componentDidMount (fn [this]
                        (let [id (comp/get-state this :field-id)
                              {:keys [attribute]} (comp/get-computed this)

                              {:autocomplete/keys [debounce-ms minimum-input search-key]}
                              (::form/field-options attribute)]
                          (merge/merge-component! this AutocompleteField
                                                  {::autocomplete-id id
                                                   :autocomplete/search-key search-key
                                                   :autocomplete/debounce-ms debounce-ms
                                                   :autocomplete/minimum-input minimum-input
                                                   :ui/search-string ""
                                                   :ui/options []}))
                        (mroot/register-root! this {:initialize? true}))
   :shouldComponentUpdate (fn [_ _] true)
   :componentWillUnmount  (fn [this]
                            (comp/transact!
                             this
                             [(gc-autocomplete {:id (comp/get-state this :field-id)})])
                            (mroot/deregister-root! this))}
  (let [{:autocomplete/keys [debounce-ms preload? search-key]} (::form/field-options attribute)
        k (::attr/qualified-key attribute)
        {::form/keys [form-instance]} env
        top-class (mfo/top-class form-instance attribute)
        value (-> (comp/props form-instance) (get k))
        id (comp/get-state this :field-id)
        label (or (form/field-label env attribute) (field/default-field-label k))
        read-only? (form/read-only? form-instance attribute)
        invalid? (form/invalid-attribute-value? env attribute)
        validation-message (when invalid? (form/validation-error-message env attribute))
        field (get-in props [::autocomplete-id id])]
    ;; Have to pass the id and debounce early since the merge in mount won't happen until after,
    ;; which is too late for initial state
    (ui-autocomplete-field
     (assoc field
            ::autocomplete-id id
            :autocomplete/search-key search-key
            :autocomplete/preload? preload?
            :autocomplete/debounce-ms debounce-ms)
     {:className top-class
      :invalid? invalid?
      :label label
      :onChange #?(:clj  (fn [])
                   :cljs (fn [normalized-value]
                           (when normalized-value
                             (form/input-changed! env k normalized-value))))
      :read-only? read-only?
      :validation-message validation-message
      :value value})))

(def ui-autocomplete-field-root
  (mroot/floating-root-factory AutocompleteFieldRoot
                               {:keyfn (fn [props] (-> props :attribute ::attr/qualified-key))}))

(defn render-autocomplete-field
  [env {::attr/keys [cardinality] :or {cardinality :one} :as attribute}]
  (if (= :many cardinality)
    (log/error "Cannot autocomplete to-many attributes with renderer" `render-autocomplete-field)
    (ui-autocomplete-field-root {:env env :attribute attribute})))
