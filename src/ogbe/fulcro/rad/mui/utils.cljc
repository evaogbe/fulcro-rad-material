(ns ogbe.fulcro.rad.mui.utils)

(defn grid-width
  [row]
  (max (-> 12 (quot (count row))) 1))
