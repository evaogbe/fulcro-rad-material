(ns ogbe.fulcro.rad.mui.form.double-field
  (:require
   [clojure.string :as str]
   [com.fulcrologic.fulcro.dom.events :as evt]
   [ogbe.fulcro.rad.mui.form.field :as field]))

(defn to-numeric [s]
  #?(:clj  (try
             (Double/parseDouble s)
             (catch Exception _ nil))
     :cljs (let [n (js/parseFloat s)]
             (when-not (js/isNaN n) n))))

(let [digits (into #{"." "-"} (map str) (range 10))]
  (defn just-decimal
    [s]
    (str/join (filter digits (seq s)))))

(def render-field
  (field/render-field-factory {:evt->model #(-> % evt/target-value just-decimal to-numeric)}
                              {:type "number"}))
