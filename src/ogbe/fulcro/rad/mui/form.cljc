(ns ogbe.fulcro.rad.mui.form
  (:require
   [clojure.string :as str]
   [com.fulcrologic.fulcro.algorithms.form-state :as fs]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom.events :as evt]
   [com.fulcrologic.fulcro.dom.html-entities :as ent]
   [com.fulcrologic.fulcro-i18n.i18n :refer [tr trc]]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.blob :as blob]
   [com.fulcrologic.rad.control :as control]
   [com.fulcrologic.rad.control-options :as copt]
   [com.fulcrologic.rad.debugging :as debug]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.form-options :as fo]
   [com.fulcrologic.rad.ids :as ids]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [ogbe.fulcro.mui.data-display.list :refer [ui-list]]
   [ogbe.fulcro.mui.data-display.list-item :refer [ui-list-item]]
   [ogbe.fulcro.mui.data-display.list-item-icon :refer [ui-list-item-icon]]
   [ogbe.fulcro.mui.data-display.list-item-text :refer [ui-list-item-text]]
   [ogbe.fulcro.mui.data-display.typography :refer [ui-typography]]
   [ogbe.fulcro.mui.feedback.alert :refer [ui-alert]]
   [ogbe.fulcro.mui.feedback.alert-title :refer [ui-alert-title]]
   [ogbe.fulcro.mui.feedback.circular-progress :refer [ui-circular-progress]]
   [ogbe.fulcro.mui.feedback.linear-progress :refer [ui-linear-progress]]
   [ogbe.fulcro.mui.icons.add :refer [ui-icon-add]]
   [ogbe.fulcro.mui.icons.close :refer [ui-icon-close]]
   [ogbe.fulcro.mui.icons.error :refer [ui-icon-error]]
   [ogbe.fulcro.mui.icons.error-outline :refer [ui-icon-error-outline]]
   [ogbe.fulcro.mui.icons.insert-drive-file :refer [ui-icon-insert-drive-file]]
   [ogbe.fulcro.mui.inputs.button :refer [ui-button]]
   [ogbe.fulcro.mui.inputs.icon-button :refer [ui-icon-button]]
   [ogbe.fulcro.mui.inputs.text-field :refer [ui-text-field]]
   [ogbe.fulcro.mui.layout.box :refer [ui-box]]
   [ogbe.fulcro.mui.layout.container :refer [ui-container]]
   [ogbe.fulcro.mui.layout.grid :refer [ui-grid]]
   [ogbe.fulcro.mui.layout.stack :refer [ui-stack]]
   [ogbe.fulcro.mui.navigation.link :refer [ui-link]]
   [ogbe.fulcro.mui.navigation.tab :refer [ui-tab]]
   [ogbe.fulcro.mui.navigation.tabs :refer [ui-tabs]]
   [ogbe.fulcro.mui.surfaces.card :refer [ui-card]]
   [ogbe.fulcro.mui.surfaces.card-action-area :refer [ui-card-action-area]]
   [ogbe.fulcro.mui.surfaces.card-content :refer [ui-card-content]]
   [ogbe.fulcro.mui.surfaces.card-header :refer [ui-card-header]]
   [ogbe.fulcro.mui.surfaces.paper :refer [ui-paper]]
   [taoensso.timbre :as log]
   [ogbe.fulcro.rad.mui.form.field :as field]
   [ogbe.fulcro.rad.mui.form-options :as mfo]
   [ogbe.fulcro.rad.mui.utils :as utils]
   [ogbe.fulcro.rad.mui-options :as mo]
   #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom]
      :cljs [com.fulcrologic.fulcro.dom :as dom])))


(defn render-to-many
  [{::form/keys [form-instance] :as env}
   {k ::attr/qualified-key :as attr}
   {::form/keys [subforms]}]
  (let [{::form/keys [added-via-upload? can-add? can-delete? title ui]
         ::keys [add-position ref-container-class]} (get subforms k)
        form-instance-props (comp/props form-instance)
        read-only? (form/read-only? form-instance attr)
        add? (if read-only? false (?! can-add? form-instance attr))
        delete? #(and (not read-only?) (?! can-delete? form-instance %))
        items (get form-instance-props k)
        title (?! (or title (some-> ui (comp/component-options fo/title)) "")
                  form-instance form-instance-props)
        invalid? (form/invalid-attribute-value? env attr)
        visible? (form/field-visible? form-instance attr)
        validation-message (form/validation-error-message env attr)
        add (when (or (nil? add?) add?)
              (let [order (if (keyword? add?) add? :append)]
                (if (?! added-via-upload? env)
                  (ui-text-field
                   {:type "file"
                    :onChange (fn [evt]
                                (let [new-id (tempid/tempid)
                                      js-file (-> evt blob/evt->js-files first)
                                      attributes (comp/component-options ui fo/attributes)
                                      id-attr (comp/component-options ui fo/id)
                                      id-key (::attr/qualified-key id-attr)
                                      {::attr/keys [qualified-key]
                                       :as sha-attr} (some #(when (::blob/store %) %) attributes)
                                      target (conj (comp/get-ident form-instance) k)
                                      new-entity (fs/add-form-config ui
                                                                     {id-key new-id
                                                                      qualified-key ""})]
                                  (merge/merge-component! form-instance ui new-entity order target)
                                  (blob/upload-file!
                                   form-instance sha-attr js-file {:file-ident [id-key new-id]})))})
                  (let [possible-types (if (comp/union-component? ui)
                                         (map comp/query->component (vals (comp/get-query ui)))
                                         [ui])]
                    (vec
                     (map-indexed
                      (fn [idx c]
                        (let [add-child! (fn [_]
                                           (form/add-child! form-instance k c {::form/order order}))
                              add-label  (or
                                          (?! (comp/component-options c fo/add-label) c add-child!)
                                          "")]
                          (comp/fragment
                           {:key idx}
                           (cond
                             (not (string? add-label)) add-label
                             (str/blank? add-label) (ui-icon-button
                                                     {:aria-label (tr "Add")
                                                      :onClick add-child!
                                                      :size "small"}
                                                     (ui-icon-add {}))
                             :else (ui-button
                                    {:color "primary"
                                     :onClick add-child!
                                     :startIcon (ui-icon-add {})
                                     :size "small"
                                     :variant "contained"}
                                    add-label)))))
                      possible-types))))))
        ui-factory (comp/computed-factory ui {:keyfn #(-> ui (comp/get-ident %) second str)})
        top-class (mfo/top-class form-instance attr)]
    (when visible?
      (ui-container
       {:className top-class
        :key (str k)}
       (ui-typography
        {:component "h3"
         :variant "h4"}
        title
        (dom/span ent/nbsp ent/nbsp)
        (when (or (nil? add-position) (= add-position :top)) add))
       (when invalid?
         (ui-alert {:severity "error"} validation-message))
       (if (seq items)
         (dom/div
          {:className (?! ref-container-class env)}
          (mapv
           (fn [props]
             (ui-factory
              props
              (assoc env
                     fo/can-delete? (delete? props)
                     ::form/parent form-instance
                     ::form/parent-relation k)))
           items))
         (ui-paper
          {:sx {:px 3
                :py 2}}
          (ui-typography {} (tr "None."))))
       (when (= add-position :bottom) add)))))

(defn render-to-one
  [{::form/keys [form-instance] :as env}
   {k ::attr/qualified-key :as attr}
   {::form/keys [subforms]}]
  (let [{::form/keys [can-add? can-delete? ref-container-class title ui]} (get subforms k)
        form-props (comp/props form-instance)
        props (get form-props k)
        top-class (or (mfo/top-class form-instance attr) "")]
    (cond
      props
      (let [invalid? (form/invalid-attribute-value? env attr)
            ui-factory (comp/computed-factory ui)
            validation-message (form/validation-error-message env attr)
            visible? (form/field-visible? form-instance attr)
            std-props {::form/nested? true
                       ::form/parent form-instance
                       ::form/parent-relation k
                       fo/can-delete? (or
                                       (?! can-delete? form-instance form-props)
                                       false)}
            ChildForm (if (comp/union-component? ui)
                        (comp/union-child-for-props ui props)
                        ui)
            title (?! (or title (some-> ChildForm (comp/component-options fo/title)) "")
                      form-instance form-props)]
        (when visible?
          (dom/div
           {:className top-class
            :classes [(?! ref-container-class env)]
            :key (str k)}
           (ui-typography
            {:component "h3"
             :variant "h4"}
            title)
           (when invalid?
             (ui-alert {:severity "error"} validation-message))
           (ui-factory props (merge env std-props)))))

      (or (nil? can-add?) (?! can-add? form-instance attr))
      (let [possible-forms (if (comp/union-component? ui)
                             (map comp/query->component (vals (comp/get-query ui)))
                             [ui])]
        (dom/div
         {:className top-class
          :classes [(?! ref-container-class env)]
          :key (str k)}
         (ui-typography
          {:component "h3"
           :variant "h4"}
          title)
         (vec (map-indexed (fn [idx ui]
                             (let [add-child! (fn [] (form/add-child! form-instance k ui))
                                   add-label (or
                                              (?! (comp/component-options ui fo/add-label)
                                                  ui add-child!)
                                              "")]
                               (comp/fragment
                                {:key idx}
                                (cond
                                  (not (string? add-label)) add-label
                                  (str/blank? add-label) (ui-icon-button
                                                          {:aria-label (tr "Create")
                                                           :onClick add-child!
                                                           :size "small"}
                                                          (ui-icon-add {}))
                                  :else (ui-button
                                         {:color "primary"
                                          :onClick add-child!
                                          :startIcon (ui-icon-add {})
                                          :size "small"
                                          :variant "contained"}
                                         add-label)))))
                           possible-forms)))))))

(defn standard-ref-container
  [env {::attr/keys [cardinality] :as attr} options]
  (if (= :many cardinality)
    (render-to-many env attr options)
    (render-to-one env attr options)))

(def attribute-map
  (memoize
   (fn [attributes]
     (reduce
      (fn [m {::attr/keys [qualified-key] :as attr}]
        (assoc m qualified-key attr))
      {}
      attributes))))

(defn render-attribute
  [env attr {::form/keys [subforms] :as options}]
  (let [{k ::attr/qualified-key} attr]
    (if (contains? subforms k)
      (let [render-ref (or (form/ref-container-renderer env attr) standard-ref-container)]
        (render-ref env attr options))
      (form/render-field env attr))))

(defn render-layout*
  [{::form/keys [form-instance] :as env} options k->attribute layout]
  (when #?(:clj true :cljs goog.DEBUG)
    (when-not (and (vector? layout) (every? vector? layout))
      (log/error "::form/layout must be a vector of vectors!")))
  (try
    (ui-grid
     {:container true
      :spacing 2}
     (vec (mapcat (fn [row]
                    (let [width (utils/grid-width row)]
                      (keep (fn [col]
                              (if-let [attr (k->attribute col)]
                                (ui-grid
                                 {:item true
                                  :key (str col)
                                  :xs width}
                                 (render-attribute env attr options))
                                (if (some-> options copt/controls (get col))
                                  (ui-grid
                                   {:item true
                                    :key (str col)
                                    :xs width}
                                   (control/render-control form-instance col))
                                  (log/error "Missing attribute (or lookup) for" col))))
                            row)))
                  layout)))
    (catch #?(:clj Exception :cljs :default) _)))

(defn render-layout
  [env {::form/keys [attributes layout] :as options}]
  (let [k->attribute (attribute-map attributes)]
    (render-layout* env options k->attribute layout)))

(defsc TabbedLayout [this env {::form/keys [attributes tabbed-layout] :as options}]
  {:initLocalState
   (fn [this]
     (try
       {:current-tab 0
        :tab-details (memoize
                      (fn [attributes tabbed-layout]
                        (let [k->attr (attribute-map attributes)
                              tab-labels (filterv string? tabbed-layout)

                              tab-label->layout
                              (into {}
                                    (map vec)
                                    (partition 2
                                               (map first (partition-by string? tabbed-layout))))]
                          {:k->attr k->attr
                           :tab-label->layout tab-label->layout
                           :tab-labels tab-labels})))}
       (catch #?(:clj Exception :cljs :default) e
         (let [{::form/keys [form-instance]} (comp/props this)]
           (log/error e
                      "Cannot build tabs for tabbed layout. Check your tabbed-layout options for"
                      (comp/component-name (or form-instance this)))))))}
  (let [{:keys [tab-details current-tab]} (comp/get-state this)
        {:keys [k->attr tab-label->layout tab-labels]} (tab-details attributes tabbed-layout)
        active-layout (some->> current-tab
                               (get tab-labels)
                               (get tab-label->layout))
        id (ids/new-uuid)]
    (dom/div
     {:key current-tab}
     (ui-tabs
      {:onChange #(comp/set-state! this {:current-tab %2})
       :value current-tab}
      (vec (map-indexed (fn [idx title]
                          (ui-tab
                           {:aria-controls (str "tabbed-form-tabpanel-" id "-" idx)
                            :id (str "tabbed-form-tab-" id "-" idx)
                            :key idx
                            :label title}))
                        tab-labels)))
     (mapv (fn [idx]
             (ui-box
              {:aria-labelledby (str "tabbed-form-tab-" id "-" idx)
               :hidden (not= idx current-tab)
               :id (str "tabbed-form-tabpanel-" id "-" idx)
               :key idx
               :role "tabpanel"
               :sx {:mt 1}}
              (when (= idx current-tab)
                (render-layout* env options k->attr active-layout))))
           (range (count tab-labels))))))

(def ui-tabbed-layout (comp/computed-factory TabbedLayout))

(defn standard-form-layout-renderer
  [{::form/keys [form-instance] :as env}]
  (let [{::form/keys [attributes debug? layout tabbed-layout]
         :as options} (comp/component-options form-instance)
        layout (cond
                 (vector? layout) (render-layout env options)
                 (vector? tabbed-layout) (ui-tabbed-layout env options)
                 :else (ui-stack
                        {:spacing 1}
                        (mapv #(render-attribute env % options) attributes)))]
    (if (and #?(:clj false :cljs goog.DEBUG) debug?)
      (debug/debugger form-instance)
      layout)))

(defsc StandardFormContainer
  [_this {::form/keys [computed-props form-instance master-form props] :as env}]
  {:shouldComponentUpdate (fn [_ _ _] true)}
  (let [{::form/keys [can-delete?]} computed-props
        nested? (not= master-form form-instance)
        read-only-form? (or
                         (?! (comp/component-options form-instance fo/read-only?) form-instance)
                         (?! (comp/component-options master-form fo/read-only?) master-form))
        [_ id :as form-ident] (comp/get-ident form-instance)
        new? (tempid/tempid? id)
        errors (form/server-errors form-instance)
        invalid? (if read-only-form? false (form/invalid? env))
        errors? (or invalid? (seq errors))
        render-fields (or (form/form-layout-renderer env) standard-form-layout-renderer)]
    (when #?(:cljs goog.DEBUG :clj true)
      (let [valid? (if read-only-form? true (form/valid? env))
            dirty? (if read-only-form? false (or new? (fs/dirty? props)))]
        (log/debug "Form" (comp/component-name form-instance) "dirty?" dirty? "valid?" valid?)))
    (if nested?
      (ui-paper
       {:className (?! (mo/get-rendering-options form-instance ::ref-element-class) env)
        :key (str form-ident)
        :sx {:p 2}}
       (when can-delete?
         (ui-icon-button
          {:aria-label (tr "Remove")
           :color "primary"
           :onClick #(form/delete-child! env)
           :sx {:float "right"}}
          (ui-icon-close {})))
       (render-fields env))
      (let [{::form/keys [action-buttons title]} (comp/component-options form-instance)
            title (?! title form-instance props)
            action-buttons (if action-buttons action-buttons form/standard-action-buttons)
            action-buttons (keep #(control/render-control master-form %) action-buttons)
            controls-position (mo/get-rendering-options form-instance ::controls-position)
            top-controls? (or (nil? controls-position) (= controls-position :top))
            show-header? (or (seq title) top-controls?)]
        (ui-container
         {:className (or
                      (?! (mo/get-rendering-options form-instance mo/layout-class) env)
                      (?! (mo/get-rendering-options form-instance ::top-level-class) env))}
         (when show-header?
           (ui-paper
            {:className (or
                         (?! (mo/get-rendering-options form-instance mo/controls-class) env)
                         (?! (mo/get-rendering-options form-instance ::controls-class) env))
             :sx {:mb 2
                  :p 2}}
            (if top-controls?
              (ui-box
               {:sx {:display "flex"
                     :justifyContent "space-between"
                     :flexWrap "wrap"}}
               (when (seq title)
                 (ui-typography
                  {:component "h2"
                   :variant "h4"}
                  title))
               (ui-stack
                {:direction "row"
                 :spacing 1}
                action-buttons))
              (ui-typography
               {:component "h2"
                :variant "h4"}
               title))))
         (dom/div
          {:className (?! (mo/get-rendering-options form-instance ::form-class) env)}
          (when errors?
            (ui-alert
             {:icon false
              :severity "error"}
             (when invalid?
               (ui-alert-title
                {}
                (tr "The form has errors and cannot be saved.")))
             (when (seq errors)
               (comp/fragment
                (ui-list
                 {:disablePadding true}
                 (vec (map-indexed
                       (fn [idx {:keys [message]}]
                         (ui-list-item
                          {:disablePadding true
                           :key idx}
                          (ui-list-item-icon
                           {}
                           (ui-icon-error-outline {:color "error"
                                                   :fontSize "large"}))
                          (ui-list-item-text {:sx {:color "error.main"}} (str message))))
                       errors)))
                (when-not new?
                  (ui-link
                   {:color "error"
                    :component "button"
                    :onClick #(form/undo-via-load! env)}
                   (tr "Reload from server")))))))
          (ui-paper
           {:className (?! (mo/get-rendering-options form-instance mo/body-class) form-instance)
            :sx {:p 2}}
           (render-fields env)))
         (when-not top-controls?
           (ui-paper
            {:className (or
                         (?! (mo/get-rendering-options form-instance mo/controls-class) env)
                         (?! (mo/get-rendering-options form-instance ::controls-class) env))
             :sx {:mt 2
                  :p 2}}
            (ui-stack
             {:direction "row"
              :spacing 1
              :sx {:justifyContent "flex-end"}}
             action-buttons))))))))

(def standard-form-container (comp/factory StandardFormContainer))

(defn render-single-file
  [{::form/keys [form-instance] :as env}
   {k ::attr/qualified-key :as attr}
   {::form/keys [subforms]}]
  (let [{::form/keys [ui can-delete?]} (get subforms k)
        parent (comp/props form-instance)
        form-props (comp/props form-instance)
        props (get form-props k)
        ui-factory (comp/computed-factory ui)
        label (or (form/field-label env attr) (field/default-field-label k))
        visible? (form/field-visible? form-instance attr)
        top-class (mfo/top-class form-instance attr)
        std-props {::form/nested? true
                   ::form/parent form-instance
                   ::form/parent-relation k
                   fo/can-delete? (if can-delete?
                                    (can-delete? parent props)
                                    false)}]
    (when visible?
      (if props
        (dom/div
         {:className top-class
          :key (str k)}
         (ui-typography
          {:component "label"}
          label)
         (ui-factory props (merge env std-props)))
        (dom/div
         {:className top-class
          :key (str k)}
         (dom/div (tr "Upload??? (TODO)")))))))

(defsc ManyFiles
  [this {{::form/keys [form-instance master-form] :as env} :env
         {k ::attr/qualified-key :as attr} :attribute
         {::form/keys [subforms]} :options}]
  {:initLocalState (fn [_this] {:input-key 0})}
  (let [{::form/keys [ui title can-delete? can-add? sort-children]
         ::keys [add-position]} (get subforms k)
        form-instance-props (comp/props form-instance)
        read-only? (or
                    (form/read-only? master-form attr)
                    (form/read-only? form-instance attr))
        add? (if read-only? false (?! can-add? form-instance attr))
        delete? (if read-only? false (fn [item] (?! can-delete? form-instance item)))
        items (-> form-instance comp/props k
                  (cond->
                   sort-children sort-children))
        title (?! (or title (some-> ui (comp/component-options fo/title)) "")
                  form-instance form-instance-props)
        upload-id (str k "-file-upload")
        add (when (or (nil? add?) add?)
              (dom/div
               (ui-button
                {:color "success"
                 :component "label"
                 :htmlFor upload-id
                 :startIcon (ui-icon-add {})
                 :variant "contained"}
                (tr "Add File"))
               (dom/input
                {:type "file"
                 ; trick: changing the key on change clears the input, so a failed upload can be
                 ; retried
                 :key (comp/get-state this :input-key)
                 :id upload-id
                 :style {:zIndex -1
                         :width "1px"
                         :height "1px"
                         :opacity 0}

                 :onChange
                 (fn [evt]
                   (let [new-id (tempid/tempid)
                         js-file (-> evt blob/evt->js-files first)
                         attributes (comp/component-options ui fo/attributes)
                         id-attr (comp/component-options ui fo/id)
                         id-key (::attr/qualified-key id-attr)
                         {::attr/keys [qualified-key]
                          :as sha-attr} (some #(when (::blob/store %) %) attributes)
                         target (conj (comp/get-ident form-instance) k)
                         new-entity (fs/add-form-config ui {id-key new-id qualified-key ""})]
                     (merge/merge-component! form-instance ui new-entity :append target)
                     (blob/upload-file! form-instance sha-attr js-file
                                        {:file-ident [id-key new-id]})
                     (comp/update-state! this update :input-key inc)))})))
        visible? (form/field-visible? form-instance attr)
        top-class (mfo/top-class form-instance attr)
        ui-factory (comp/computed-factory ui {:keyfn (fn [item]
                                                       (-> ui (comp/get-ident item) second str))})]
    (when visible?
      (ui-paper
       {:className top-class
        :key (str k)
        :sx {:p 2}}
       (ui-typography
        {:component "h3"
         :variant "h4"}
        title)
       (when (or (nil? add-position) (= add-position :top)) add)
       (if (seq items)
         (mapv (fn [props]
                 (ui-factory props
                             (assoc env
                                    fo/can-delete? (and delete? (?! delete? props))
                                    ::form/parent form-instance
                                    ::form/parent-relation k)))
               items)
         (ui-paper
          {:sx {:mt 1
                :p 2}}
          (ui-typography
           {}
           (trc "there are no files in a list of uploads" "No files."))))
       (when (= add-position :bottom) add)))))

(def ui-many-files
  (comp/factory ManyFiles {:keyfn (fn [{:keys [attribute]}] (::attr/qualified-key attribute))}))

(defn file-ref-container
  [env {::attr/keys [cardinality] :as attr} options]
  (if (= :many cardinality)
    (ui-many-files {:env env :attribute attr :options options})
    (render-single-file env attr options)))

(defn file-icon-renderer
  [{::form/keys [form-instance] :as env}]
  (let [{::form/keys [attributes]} (comp/component-options form-instance)
        attribute (some #(when (::blob/store %) %) attributes)
        sha-key (::attr/qualified-key attribute)
        file-key (blob/filename-key sha-key)
        url-key (blob/url-key sha-key)
        props (comp/props form-instance)
        filename (get props file-key "File")
        dirty? (fs/dirty? props sha-key)
        failed? (blob/failed-upload? props sha-key)
        pct (blob/upload-percentage props sha-key)
        sha (get props sha-key)
        url (get props url-key)]
    (if (blob/uploading? props sha-key)
      (ui-card
       {}
       (ui-card-header
        {:action (ui-icon-button
                  {:aria-label (tr "Remove")
                   :color "error"
                   :onClick (fn []
                              (app/abort! form-instance sha)
                              (form/delete-child! env))}
                  (ui-icon-close {}))})
       (ui-card-content
        {:sx {:display "flex"}}
        (dom/div
         (ui-icon-insert-drive-file {:fontSize "large"})
         (ui-circular-progress {:color "error"})
         (ui-linear-progress {:value pct
                              :variant "determinate"}))
        (ui-typography {} filename)))
      (ui-card
       {}
       (ui-card-header
        {:action (ui-icon-button
                  {:aria-label (tr "Remove")
                   :color "error"
                   :onClick (fn [evt]
                              (evt/stop-propagation! evt)
                              (evt/prevent-default! evt)
                              (when #?(:clj  true
                                       :cljs (js/confirm (tr "Permanently Delete File?")))
                                (form/delete-child! env)))}
                  (ui-icon-close {}))})
       (ui-card-action-area
        {:href (str url "?filename=" filename)
         :key (str sha)
         :onClick #?(:clj  (fn [])
                     :cljs (fn [evt]
                             (when-not (or (not (blob/blob-downloadable? props sha-key))
                                           (js/confirm (tr "View/download?")))
                               (evt/stop-propagation! evt)
                               (evt/prevent-default! evt))))
         :target "_blank"}
        (ui-card-content
         {}
         (if failed?
           (ui-icon-error {:fontSize "small"})
           (ui-icon-insert-drive-file {:fontSize "small"}))
         (ui-typography
          {}
          (str filename (cond failed? (str " (" (tr "Upload failed. Delete and try again.") ")")
                              dirty? (str " (" (tr "unsaved") ")"))))))))))
