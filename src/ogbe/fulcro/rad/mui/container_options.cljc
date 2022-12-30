(ns ogbe.fulcro.rad.mui.container-options
  "Documented option keys for setting rendering-specific customization options for containers when
   using Material UI Plugin as your DOM renderer.

  ALL options MUST appear under the rendering options key:

  ```
  (ns ...
    (:require
       [ogbe.fulcro.rad.mui-options :as mo]
       ...))

  (defsc-container Container [this props]
    {mo/rendering-options { ... }})
  ```

  Most of the options in this file can be given a global default using

  ```
  (set-global-rendering-options! fulcro-app options)
  ```

  where the `options` is a map of option keys/values.")

(def tabbed-layout
  "A description of a layout that will place the children in tabs to reduce visual clutter. The
   layout specification is:
   
   ```
  [\"Tab 1\"
   [[::report-a]
    [::report-b ::report-c]]
   \"Tab 2\"
   [[:report-d]]]
  ```
  
   Where the top-level vector is a sequence of strings interposed with child layouts.
   
   If this is specified, it will be used instead of co/layout."
  :ogbe.fulcro.rad.mui.container/tabbed-layout)
