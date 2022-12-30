(ns ogbe.fulcro.rad.mui.report
  (:require
   [clojure.string :as str]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom.events :as evt]
   [com.fulcrologic.fulcro.dom-common :as cdom]
   [com.fulcrologic.fulcro-i18n.i18n :refer [trc]]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.container :as container]
   [com.fulcrologic.rad.control :as control]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [com.fulcrologic.rad.report :as report]
   [com.fulcrologic.rad.report-options :as ro]
   [ogbe.fulcro.mui.data-display.list :refer [ui-list]]
   [ogbe.fulcro.mui.data-display.list-item :refer [ui-list-item]]
   [ogbe.fulcro.mui.data-display.list-item-button :refer [ui-list-item-button]]
   [ogbe.fulcro.mui.data-display.list-item-text :refer [ui-list-item-text]]
   [ogbe.fulcro.mui.data-display.table :refer [ui-table]]
   [ogbe.fulcro.mui.data-display.table-body :refer [ui-table-body]]
   [ogbe.fulcro.mui.data-display.table-cell :refer [ui-table-cell]]
   [ogbe.fulcro.mui.data-display.table-container :refer [ui-table-container]]
   [ogbe.fulcro.mui.data-display.table-head :refer [ui-table-head]]
   [ogbe.fulcro.mui.data-display.table-pagination :refer [ui-table-pagination]]
   [ogbe.fulcro.mui.data-display.table-row :refer [ui-table-row]]
   [ogbe.fulcro.mui.data-display.table-sort-label :refer [ui-table-sort-label]]
   [ogbe.fulcro.mui.data-display.tooltip :refer [ui-tooltip]]
   [ogbe.fulcro.mui.data-display.typography :refer [ui-typography]]
   [ogbe.fulcro.mui.feedback.skeleton :refer [ui-skeleton]]
   [ogbe.fulcro.mui.inputs.button :refer [ui-button]]
   [ogbe.fulcro.mui.layout.box :refer [ui-box]]
   [ogbe.fulcro.mui.layout.grid :refer [ui-grid]]
   [ogbe.fulcro.mui.layout.stack :refer [ui-stack]]
   [ogbe.fulcro.mui.navigation.link :refer [ui-link]]
   [ogbe.fulcro.mui.surfaces.paper :refer [ui-paper]]
   [ogbe.fulcro.mui.utils.fade :refer [ui-fade]]
   [ogbe.fulcro.rad.mui.report-options :as mro]
   [ogbe.fulcro.rad.mui.utils :as utils]
   [ogbe.fulcro.rad.mui-options :as mo]
   #?@(:clj  [[com.fulcrologic.fulcro.dom-server :as dom]]
       :cljs [["@mui/utils" :refer [visuallyHidden]]
              [com.fulcrologic.fulcro.dom :as dom]])))

(defn row-action-buttons
  [report-instance row-props]
  (let [{::report/keys [row-actions]} (comp/component-options report-instance)
        {::keys [row-button-grouping
                 row-button-renderer]} (mo/get-rendering-options report-instance)]
    (when (seq row-actions)
      (ui-stack
       {:className (?! row-button-grouping report-instance)
        :direction "row"
        :spacing 1}
       (keep-indexed
        (fn [idx {:keys [action disabled? label reload? visible?] :as control}]
          (let [disabled? (boolean (?! disabled? report-instance row-props))
                onClick (fn [evt]
                          (evt/stop-propagation! evt)
                          (when action
                            (action report-instance row-props)
                            (when reload?
                              (control/run! report-instance))))
                control-props (merge control
                                     {:label label
                                      :key idx
                                      :onClick onClick
                                      :disabled? disabled?})
                label (?! label report-instance row-props control-props)]
            (when (or (nil? visible?) (?! visible? report-instance row-props))
              (if row-button-renderer
                (row-button-renderer report-instance row-props control-props)
                (if (string? label)
                  (ui-button
                   {:disabled disabled?
                    :key idx
                    :onClick onClick
                    :variant "contained"}
                   label)
                  label)))))
        row-actions)))))

(defsc TableRowLayout [_this {:keys [props report-instance]}]
  (let [{::report/keys [columns link links on-select-row]} (comp/component-options report-instance)
        links (or links link)
        action-buttons (row-action-buttons report-instance props)
        {:keys [highlighted?]
         ::report/keys [idx]} (comp/get-computed props)
        mat-cell-class (mo/get-rendering-options report-instance ::table-cell-class)
        selectable? (not (false?
                          (mo/get-rendering-options report-instance ::selectable-table-rows?)))]
    (ui-table-row
     {:aria-checked (str highlighted?)
      :hover true
      :onClick (fn [evt]
                 (when selectable?
                   (evt/stop-propagation! evt)
                   (?! on-select-row report-instance props)
                   (report/select-row! report-instance idx)))
      :role (when selectable? "checkbox")
      :selected (boolean highlighted?)
      :tabIndex -1}
     (vec (map-indexed
           (fn [idx {::attr/keys [qualified-key] :as column}]
             (let [alignment (?! (get column ro/column-alignment) report-instance column)
                   column-classes (report/column-classes report-instance column)
                   cell-class (?! mat-cell-class report-instance idx)
                   className (cdom/classes->str [cell-class column-classes])
                   width (mro/get-column-width report-instance column)]
               (ui-table-cell
                (cond-> {:className className
                         :key (str "col-" qualified-key)}
                  alignment (assoc :align (str alignment))
                  width (assoc :sx {:width width}))
                (let [{:keys [edit-form entity-id]} (report/form-link
                                                     report-instance props qualified-key)
                      link-fn (get links qualified-key)
                      label (report/formatted-column-value report-instance props column)]
                  (cond
                    edit-form (ui-link
                               {:onClick (fn [evt]
                                           (evt/stop-propagation! evt)
                                           (form/edit! report-instance edit-form entity-id))
                                :sx {:cursor "pointer"}}
                               label)
                    (fn? link-fn) (ui-link
                                   {:onClick (fn [evt]
                                               (evt/stop-propagation! evt)
                                               (link-fn report-instance props))
                                    :sx {:cursor "pointer"}}
                                   label)
                    :else label)))))
           columns))
     (when action-buttons
       (ui-table-cell
        {:className (?! mat-cell-class report-instance (count columns))
         :key "actions"}
        action-buttons)))))

(let [ui-table-row-layout (comp/factory TableRowLayout)]
  (defn render-table-row [report-instance row-class row-props]
    (ui-table-row-layout {:report-instance report-instance
                          :row-class row-class
                          :props row-props})))

(defn ui-table-loading
  [report-instance rotate?]
  (let [{::report/keys [columns]} (comp/component-options report-instance)
        column-count (cond-> (count columns)
                       rotate? inc)]
    (ui-table-container
     {:aria-busy "true"
      :aria-label "Loading"
      :role "status"}
     (ui-table
      {:component "div"}
      (ui-table-head
       {:component "div"}
       (ui-table-row
        {:component "div"}
        (mapv #(ui-table-cell
                {:component "div"
                 :key %}
                (ui-skeleton {}))
              (range column-count))))
      (ui-table-body
       {:component "div"}
       (mapv (fn [row]
               (ui-table-row
                {:component "div"
                 :key row}
                (mapv #(ui-table-cell
                        {:component "div"
                         :key %}
                        (ui-skeleton {}))
                      (range column-count))))
             (range 7)))))))

(defn get-title
  [report-instance]
  (or (some-> report-instance comp/component-options ro/title (?! report-instance))
      (trc "a table that shows a list of rows" "Report")))

(defsc StandardReportControls [_this {:keys [report-instance]}]
  {:shouldComponentUpdate (fn [_ _ _] true)}
  (let [controls (control/component-controls report-instance)
        {::report/keys [paginate?]} (comp/component-options report-instance)
        action-button-grouping (mo/get-rendering-options report-instance ::action-button-grouping)
        {:keys [action-layout input-layout]} (control/standard-control-layout report-instance)
        {::container/keys [controlled?]} (comp/get-computed report-instance)
        title (get-title report-instance)
        controls-class (or
                        (?! (mo/get-rendering-options report-instance ::controls-class))
                        (?! (mo/get-rendering-options report-instance mo/controls-class)))
        controls-cell-class (mo/get-rendering-options report-instance ::controls-cell-class)
        action-buttons (keep (fn [k]
                               (let [control (get controls k)]
                                 (when (and (or (not controlled?) (:local? control))
                                            (-> (get control :visible? true) (?! report-instance)))
                                   (control/render-control report-instance k control))))
                             action-layout)
        inputs (vec
                (concat
                 (map-indexed
                  (fn [row-idx row]
                    (let [cell-class (?! controls-cell-class report-instance row-idx)
                          width (utils/grid-width
                                 (filter #(or (not controlled?) (:local? (get controls %))) row))]
                      (keep (fn [k]
                              (let [control (get controls k)]
                                (when (and (or (not controlled?) (:local? control))
                                           (-> (get control :visible? true) (?! report-instance)))
                                  (ui-grid
                                   {:className cell-class
                                    :item true
                                    :key (str k)
                                    :xs width}
                                   (control/render-control report-instance k control)))))
                            row)))
                  input-layout)))
        show-header? (or (seq title) (seq action-buttons) (seq inputs) paginate?)]
    (when show-header?
      (ui-paper
       {:className controls-class
        :sx {:mb 2
             :p 2}}
       (ui-box
        {:sx {:display "flex"
              :justifyContent "space-between"
              :flexWrap "wrap"}}
        (when (seq title)
          (ui-typography
           {:component (if controlled? "h3" "h2")
            :variant "h4"}
           title))
        (when (seq action-buttons)
          (ui-stack
           {:className (?! action-button-grouping report-instance)
            :direction "row"
            :spacing 1}
           action-buttons)))
       (when (seq inputs)
         (ui-grid
          {:container true
           :spacing 2
           :sx {:mt 1}}
          inputs))
       (when paginate?
         (let [page-count (report/page-count report-instance)
               page-size (comp/component-options report-instance ro/page-size)
               page-size (if (int? page-size) page-size 20)]
           (when (-> 1 (< page-count))
             (ui-table-pagination
              {:component "div"
               :count page-count
               :onPageChange #(report/goto-page! report-instance (inc %2))
               :page (dec (report/current-page report-instance))
               :rowsPerPage page-size
               :rowsPerPageOptions [-1]}))))))))

(let [ui-standard-report-controls (comp/factory StandardReportControls)]
  (defn render-standard-controls [report-instance]
    (ui-standard-report-controls {:report-instance report-instance})))

(defn render-standard-table
  [this {:keys [report-instance]}]
  (let [{report-column-headings ::report/column-headings
         report-column-infos ::report/column-infos
         ::report/keys [BodyItem columns compare-rows row-actions table-class]}
        (comp/component-options report-instance)

        render-report-body-item ((comp/get-state this :row-factory) BodyItem)
        column-headings (map (fn [{::attr/keys [qualified-key]
                                   ::report/keys [column-heading column-info] :as attr}]
                               {:column attr
                                :help (or
                                       (?! (get report-column-infos qualified-key) report-instance)
                                       (?! column-info report-instance))
                                :label (or
                                        (?! (get report-column-headings qualified-key)
                                            report-instance)
                                        (?! column-heading report-instance)
                                        (some-> qualified-key
                                                name
                                                (str/replace #"-" " ")
                                                str/capitalize)
                                        "")})
                             columns)
        rows (report/current-rows report-instance)
        highlighted-row-idx (report/currently-selected-row report-instance)
        extra-parent-query (comp/component-options report-instance ro/query-inclusions)
        query-inclusion-props (select-keys (comp/props report-instance) extra-parent-query)
        props (comp/props report-instance)
        sort-params (-> props :ui/parameters ::report/sort)
        sortable? (if compare-rows
                    (if-let [sortable-columns (some-> sort-params :sortable-columns set)]
                      (fn [{::attr/keys [qualified-key]}]
                        (contains? sortable-columns qualified-key))
                      (constantly true))
                    (constantly false))
        ascending? (and sortable? (:ascending? sort-params))
        sorting-by (and sortable? (:sort-by sort-params))
        has-row-actions? (seq row-actions)
        mat-header-class (mo/get-rendering-options report-instance ::table-header-class)
        mat-table-class (?! (mo/get-rendering-options report-instance ::table-class)
                            report-instance)]
    (ui-table-container
     {}
     (ui-table
      {:aria-label (get-title report-instance)
       :className (cdom/classes->str [mat-table-class table-class])}
      (ui-table-head
       {}
       (ui-table-row
        {}
        (vec (map-indexed
              (fn [idx {:keys [column help label]}]
                (let [alignment (?! (get column ro/column-alignment) report-instance column)
                      sorting-by? (= sorting-by (::attr/qualified-key column))
                      order (cond
                              (not sorting-by?) nil
                              ascending? "asc"
                              (sortable? column) "desc")
                      ui-cell (if (sortable? column)
                                (ui-table-sort-label
                                 (cond-> {:active (boolean sorting-by?)
                                          :onClick #(report/sort-rows! report-instance column)}
                                   order (assoc :direction order))
                                 label
                                 (when sorting-by?
                                   (ui-box
                                    {:component "span"
                                     :sx #?(:clj {} :cljs visuallyHidden)}
                                    (if (= order "asc") "sorted ascending" "sorted descending"))))
                                label)]
                  (ui-table-cell
                   (cond-> {:className (?! mat-header-class report-instance idx)
                            :component "th"
                            :key idx
                            :scope "col"
                            :sortDirection order}
                     alignment (assoc :align (str alignment)))
                   (if #?(:clj false :cljs help)
                     (ui-tooltip {:title help} ui-cell)
                     ui-cell))))
              column-headings))
        (when has-row-actions?
          (ui-table-cell
           {:className (?! mat-header-class report-instance (count column-headings))
            :component "th"
            :scope "col"}
           ""))))
      (when (seq rows)
        (ui-table-body
         {}
         (vec (map-indexed
               (fn [idx row]
                 (render-report-body-item row (merge query-inclusion-props
                                                     {::report/idx  idx
                                                      :highlighted? (= idx highlighted-row-idx)
                                                      :report-instance report-instance
                                                      :row-class BodyItem})))
               rows))))))))

(defn render-rotated-table
  [_this {:keys [report-instance]}]
  (let [{report-column-headings ::report/column-headings
         ::report/keys [columns
                        compare-rows
                        row-actions
                        table-class]} (comp/component-options report-instance)
        props (comp/props report-instance)
        sort-params (-> props :ui/parameters ::report/sort)
        sortable? (if compare-rows
                    (if-let [sortable-columns (some-> sort-params :sortable-columns set)]
                      (fn [{::attr/keys [qualified-key]}]
                        (contains? sortable-columns qualified-key))
                      (constantly true))
                    (constantly false))
        ascending? (and sortable? (:ascending? sort-params))
        sorting-by (and sortable? (:sort-by sort-params))
        row-headings (mapv (fn [{::report/keys [column-heading]
                                 ::attr/keys [qualified-key] :as attr}]
                             (let [label (or
                                          (?! (get report-column-headings qualified-key)
                                              report-instance)
                                          (?! column-heading report-instance)
                                          (some-> qualified-key
                                                  name
                                                  (str/replace #"-" " ")
                                                  str/capitalize)
                                          "")
                                   sorting-by? (= sorting-by qualified-key)
                                   order (cond
                                           (not sorting-by?) nil
                                           ascending? "asc"
                                           (sortable? attr) "desc")]
                               (if (sortable? attr)
                                 (ui-table-sort-label
                                  (cond-> {:active (boolean sorting-by?)
                                           :onClick (fn [evt]
                                                      (evt/stop-propagation! evt)
                                                      (report/sort-rows! report-instance attr))}
                                    order (assoc :direction order))
                                  label
                                  (when sorting-by?
                                    (ui-box
                                     {:component "span"
                                      :sx #?(:clj {} :cljs visuallyHidden)}
                                     (if (= order "asc") "sorted ascending" "sorted descending"))))
                                 label)))
                           columns)
        rows (report/current-rows report-instance)
        has-row-actions? (seq row-actions)
        mat-table-class (?! (mo/get-rendering-options report-instance ::rotated-table-class)
                            report-instance)
        mat-cell-class (mo/get-rendering-options report-instance ::table-cell-class)]
    (ui-table-container
     {}
     (ui-table
      {:aria-label (get-title report-instance)
       :className (cdom/classes->str [mat-table-class table-class])}
      (when (seq rows)
        (comp/fragment
         (ui-table-head
          {}
          (let [col (first columns)]
            (ui-table-row
             {:key "hrow"}
             (ui-table-cell
              {:component "th"
               :scope "row"}
              (get row-headings 0))
             (vec (map-indexed (fn [idx row]
                                 (ui-table-cell
                                  {:component "th"
                                   :key idx
                                   :scope "col"}
                                  (report/formatted-column-value report-instance row col)))
                               rows))
             (when has-row-actions?
               (ui-table-cell
                {:key "actions"}
                (row-action-buttons report-instance col))))))
         (ui-table-body
          {}
          (vec
           (map-indexed
            (fn [row-idx col]
              (let [width (mro/get-column-width report-instance col)]
                (ui-table-row
                 {:key row-idx}
                 (ui-table-cell
                  {:component "th"
                   :scope "row"
                   :variant "head"}
                  (get row-headings (inc row-idx)))
                 (vec (map-indexed
                       (fn [col-idx row]
                         (let [user-defined-cell-class (?! mat-cell-class report-instance col-idx)]
                           (ui-table-cell
                            {:align "right"
                             :className user-defined-cell-class
                             :key col-idx
                             :sx {:width width}}
                            (report/formatted-column-value report-instance row col))))
                       rows))
                 (when has-row-actions?
                   (let [user-defined-cell-class
                         (when mat-cell-class
                           (?! mat-cell-class report-instance (count rows)))]
                     (ui-table-cell
                      {:className user-defined-cell-class
                       :key "actions"}
                      (row-action-buttons report-instance col)))))))
            (rest columns))))))))))

(defsc TableReportLayout [this {:keys [report-instance] :as env}]
  {:initLocalState (fn [_this]
                     {:row-factory (memoize
                                    (fn [cls]
                                      (comp/computed-factory
                                       cls
                                       {:keyfn #(some-> % (comp/get-computed ::report/idx))})))})
   :shouldComponentUpdate (fn [_ _ _] true)}
  (let [{::report/keys [rotate?]} (comp/component-options report-instance)
        rotate? (?! rotate? report-instance)
        render-controls (report/control-renderer report-instance)
        loading? (report/loading? report-instance)
        props (comp/props report-instance)
        busy? (:ui/busy? props)
        layout-class (?! (mo/get-rendering-options report-instance mo/layout-class) report-instance)
        body-class (?! (mo/get-rendering-options report-instance mo/body-class) report-instance)]
    (dom/div
     {:className layout-class}
     (when render-controls
       (render-controls report-instance))
     (ui-paper
      {:className body-class
       :sx {:p 2}}
      (if rotate?
        (render-rotated-table this env)
        (render-standard-table this env))
      (ui-fade
       {:in (or busy? loading?)
        :unmountOnExit true}
       (ui-table-loading report-instance rotate?))))))

(let [ui-table-report-layout (comp/factory TableReportLayout {:keyfn ::report/idx})]
  (defn render-table-report-layout [this]
    (ui-table-report-layout {:report-instance this})))

(defsc ListRowLayout [_this {:keys [report-instance props]}]
  (let [{::report/keys [columns]} (comp/component-options report-instance)
        header-column (first columns)
        description-column (second columns)
        {:keys [edit-form entity-id]} (some->> header-column
                                               ::attr/qualified-key
                                               (report/form-link report-instance props))
        header-label (some->> header-column (report/formatted-column-value report-instance props))
        description-label (some->> description-column
                                   (report/formatted-column-value report-instance props))
        action-buttons (row-action-buttons report-instance props)]
    (ui-list-item
     {:disablePadding true
      :divider true
      :secondaryAction action-buttons}
     (ui-list-item-button
      {:onClick (when edit-form
                  #(form/edit! report-instance edit-form entity-id))}
      (ui-list-item-text
       {:primary header-label
        :secondary description-label})))))

(let [ui-list-row-layout (comp/factory ListRowLayout {:keyfn ::report/idx})]
  (defn render-list-row [report-instance row-class row-props]
    (ui-list-row-layout {:report-instance report-instance
                         :row-class row-class
                         :props row-props})))

(defn ui-list-loading
  []
  (ui-list
   {:aria-busy "true"
    :aria-label "Loading"
    :component "div"
    :role "status"
    :sx {:width "100%"}}
   (mapv #(ui-list-item
           {:component "div"
            :divider true
            :key %}
           (ui-list-item-text
            {}
            (ui-skeleton {})))
         (range 7))))

(defsc ListReportLayout [this {:keys [report-instance]}]
  {:shouldComponentUpdate (fn [_ _ _] true)
   :initLocalState (fn [_this]
                     {:row-factory
                      (memoize
                       (fn [cls]
                         (comp/computed-factory
                          cls
                          {:keyfn (fn [props]
                                    (some-> props (comp/get-computed ::report/idx)))})))})}
  (let [{::report/keys [BodyItem]} (comp/component-options report-instance)
        render-report-body-item ((comp/get-state this :row-factory) BodyItem)
        render-controls (report/control-renderer report-instance)
        extra-parent-query (comp/component-options report-instance ro/query-inclusions)
        query-inclusion-props (select-keys (comp/props report-instance) extra-parent-query)
        rows (report/current-rows report-instance)
        loading? (report/loading? report-instance)
        layout-class (?! (mo/get-rendering-options report-instance mo/layout-class) report-instance)
        body-class (?! (mo/get-rendering-options report-instance mo/body-class) report-instance)]
    (dom/div
     {:className layout-class}
     (when render-controls
       (render-controls report-instance))
     (ui-paper
      {:className body-class
       :sx {:p 2}}
      (when (seq rows)
        (ui-list
         {}
         (vec (map-indexed (fn [idx row]
                             (render-report-body-item row (merge query-inclusion-props
                                                                 {::report/idx idx
                                                                  :report-instance report-instance
                                                                  :row-class BodyItem})))
                           rows))))
      (ui-fade
       {:in loading?
        :unmountOnExit true}
       (ui-list-loading))))))

(let [ui-list-report-layout (comp/factory ListReportLayout {:keyfn ::report/idx})]
  (defn render-list-report-layout [report-instance]
    (ui-list-report-layout {:report-instance report-instance})))
