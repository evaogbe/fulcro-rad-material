(ns ogbe.fulcro.rad.mui.container
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.rad.container :as container]
   [com.fulcrologic.rad.container-options :as co]
   [com.fulcrologic.rad.control :as control]
   [com.fulcrologic.rad.ids :as ids]
   [com.fulcrologic.rad.options-util :refer [?!]]
   [ogbe.fulcro.mui.data-display.typography :refer [ui-typography]]
   [ogbe.fulcro.mui.layout.box :refer [ui-box]]
   [ogbe.fulcro.mui.layout.grid :refer [ui-grid]]
   [ogbe.fulcro.mui.layout.stack :refer [ui-stack]]
   [ogbe.fulcro.mui.navigation.tab :refer [ui-tab]]
   [ogbe.fulcro.mui.navigation.tabs :refer [ui-tabs]]
   [ogbe.fulcro.mui.surfaces.paper :refer [ui-paper]]
   [taoensso.timbre :as log]
   [ogbe.fulcro.rad.mui.utils :as utils]
   [ogbe.fulcro.rad.mui-options :as mo]
   #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom]
      :cljs [com.fulcrologic.fulcro.dom :as dom])))

(defsc StandardContainerControls [_this {:keys [instance]}]
  {:shouldComponentUpdate (fn [_ _ _] true)}
  (let [controls (control/component-controls instance)
        {:keys [action-layout input-layout]} (control/standard-control-layout instance)
        title (some-> instance comp/component-options co/title (?! instance))
        action-buttons (keep (fn [k]
                               (let [control (get controls k)]
                                 (when (-> (get control :visible? true) (?! instance))
                                   (control/render-control instance k control))))
                             action-layout)
        inputs (vec (mapcat (fn [row]
                              (let [width (utils/grid-width row)]
                                (map (fn [col]
                                       (ui-grid
                                        {:item true
                                         :key (str col)
                                         :xs width}
                                        (if-let [c (get controls col)]
                                          (when (-> (get c :visible? true) (?! instance))
                                            (control/render-control instance col c))
                                          (ui-box
                                           {:sx {:mb 2}}
                                           ""))))
                                     row)))
                            input-layout))
        show-header? (or (seq title) (seq action-buttons) (seq inputs))]
    (when show-header?
      (ui-paper
       {:sx {:mb 2
             :p 2}}
       (ui-box
        {:sx {:display "flex"
              :justifyContent "space-between"
              :flexWrap "wrap"}}
        (when (seq title)
          (ui-typography
           {:component "h2"
            :variant "h4"}
           title))
        (when (seq action-buttons)
          (ui-stack
           {:direction "row"
            :spacing 1}
           action-buttons)))
       (when (seq inputs)
         (ui-grid
          {:container true
           :spacing 2
           :sx {:mt 1}}
          inputs))))))

(let [ui-standard-container-controls (comp/factory StandardContainerControls)]
  (defn render-standard-controls [instance]
    (ui-standard-container-controls {:instance instance})))

(defn render-layout*
  [container-instance layout]
  (let [{::container/keys [children]} (comp/component-options container-instance)
        container-props (comp/props container-instance)
        render-cls (fn [id cls]
                     (let [factory (comp/computed-factory cls)
                           props (get container-props id {})]
                       (factory props {::container/controlled? true})))]
    (when #?(:clj true :cljs goog.DEBUG)
      (when-not (and (vector? layout) (every? vector? layout))
        (log/error "::container/layout must be a vector of vectors!")))
    (ui-grid
     {:container true
      :spacing 1
      :sx {:alignSelf "center"}}
     (vec (mapcat (fn [row]
                    (let [default-width (utils/grid-width row)]
                      (map (fn [entry]
                             (let [id (if (keyword? entry) entry (:id entry))
                                   width (or
                                          (and (map? entry) (:width entry))
                                          default-width)
                                   cls (get children id)]
                               (ui-grid
                                {:item true
                                 :key (str id)
                                 :xs width}
                                (render-cls id cls))))
                           row)))
                  layout)))))

(defsc TabbedLayout [this {:keys [container-instance tabbed-layout]}]
  {:initLocalState
   (fn [this]
     (try
       {:current-tab 0

        :tab-details
        (memoize (fn [tabbed-layout]
                   (let [tab-labels (filterv string? tabbed-layout)

                         tab-label->layout
                         (into {}
                               (map vec)
                               (partition 2
                                          (map first (partition-by string? tabbed-layout))))]
                     {:tab-label->layout tab-label->layout
                      :tab-labels tab-labels})))}
       (catch #?(:clj Exception :cljs :default) e
         (let [{:keys [container-instance]} (comp/props this)]
           (log/error e
                      "Cannot build tabs for tabbed layout. Check your tabbed-layout options for"
                      (comp/component-name (or container-instance this)))))))
   :shouldComponentUpdate (fn [_ _ _] true)}
  (let [{:keys [tab-details current-tab]} (comp/get-state this)
        {:keys [tab-label->layout tab-labels]} (tab-details tabbed-layout)
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
                           {:aria-controls (str "tabbed-container-tabpanel-" id "-" idx)
                            :id (str "tabbed-container-tab-" id "-" idx)
                            :key idx
                            :label title}))
                        tab-labels)))
     (mapv (fn [idx]
             (ui-box
              {:aria-labelledby (str "tabbed-container-tab-" id "-" idx)
               :hidden (not= idx current-tab)
               :id (str "tabbed-container-tabpanel-" id "-" idx)
               :key idx
               :role "tabpanel"
               :sx {:mt 1}}
              (when (= idx current-tab)
                (render-layout* container-instance active-layout))))
           (range (count tab-labels))))))

(def ui-tabbed-layout (comp/factory TabbedLayout))

(defn render-container-layout
  [container-instance]
  (let [tabbed-layout (mo/get-rendering-options container-instance ::tabbed-layout)
        {::container/keys [layout]} (comp/component-options container-instance)
        container-props (comp/props container-instance)
        render-cls (fn [id cls]
                     (let [factory (comp/computed-factory cls)
                           props (get container-props id {})]
                       (factory props {::container/controlled? true})))]
    (ui-box
     {:sx {:display "flex"
           :flexDirection "column"}}
     (render-standard-controls container-instance)
     (ui-paper
      {:sx {:p 2}}
      (cond
        tabbed-layout (ui-tabbed-layout {:container-instance container-instance
                                         :tabbed-layout tabbed-layout})
        layout (render-layout* container-instance layout)
        :else (mapv (fn [[id cls]]
                      (dom/div
                       {:key (str id)}
                       (render-cls id cls)))
                    (container/id-child-pairs container-instance)))))))
