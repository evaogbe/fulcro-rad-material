(ns ogbe.fulcro.rad.mui.form.blob-field
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro-i18n.i18n :refer [tr]]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.blob :as blob]
   [ogbe.fulcro.mui.data-display.typography :refer [ui-typography]]
   [ogbe.fulcro.mui.feedback.linear-progress :refer [ui-linear-progress]]
   [ogbe.fulcro.mui.navigation.link :refer [ui-link]]
   [ogbe.fulcro.rad.mui.form-options :as mfo]
   [ogbe.fulcro.rad.mui.form.field :as field]
   #?@(:cljs [[com.fulcrologic.fulcro.dom :as dom]
              [goog.object :as gobj]]
       :clj  [[com.fulcrologic.fulcro.dom-server :as dom]])))

#?(:cljs (defn evt->js-files
           [evt]
           (let [js-file-list (.. evt -target -files)]
             (map #(.item js-file-list %) (range (.-length js-file-list))))))

(defsc FileUploadField
  [_this {::form/keys [form-instance] :as env} {::attr/keys [qualified-key] :as attribute}]
  {:componentDidMount (fn [_this]
                        (comment "TRIGGER UPLOAD IF CONFIG SAYS TO?"))

   :initLocalState
   #?(:clj  (fn [])
      :cljs (fn [this]
              {:save-ref (fn [r] (gobj/set this "fileinput" r))
               :on-click (fn [_evt] (when-let [i (gobj/get this "fileinput")]
                                      (.click i)))
               :on-change (fn [evt]
                            (let [attribute (comp/get-computed this)
                                  file (-> evt evt->js-files first)]
                              (blob/upload-file! this attribute file {:file-ident []})))}))}
  (let [props (comp/props form-instance)
        url-key (blob/url-key qualified-key)
        name-key (blob/filename-key qualified-key)
        url (get props url-key)
        filename (get props name-key)
        pct (blob/upload-percentage props qualified-key)
        label (or (form/field-label env attribute) (field/default-field-label qualified-key))
        top-class (mfo/top-class form-instance attribute)]
    (dom/div
     {:className top-class
      :key (str qualified-key)}
     (ui-typography {:component "label"} label)
     (cond
       (blob/blob-downloadable? props qualified-key) (ui-link
                                                      {:href (str url "?filename=" filename)}
                                                      (tr "Download"))
       (blob/uploading? props qualified-key) (ui-linear-progress {:value pct
                                                                  :variant "determinate"})))))

(def render-file-upload
  (comp/computed-factory
   FileUploadField
   {:keyfn (fn [props] (some-> props comp/get-computed ::attr/qualified-key))}))
